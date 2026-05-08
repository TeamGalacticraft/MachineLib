package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import dev.galacticraft.machinelib.impl.util.Utils;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Default item slot implementation.
 */
public class ItemResourceSlotImpl extends ResourceSlotImpl<Item> implements ItemResourceSlot {

    private static final String RECIPES_KEY = "Recipes";

    private final @Nullable ItemSlotDisplay display;
    private final @Nullable ResourceLocation id;
    private final @NotNull Set<ResourceLocation> groups;

    private long cachedExpiry = -1;

    private @Nullable Set<ResourceLocation> recipes = null;
    private @Nullable SingleSlotStorage<ItemVariant> cachedStorage = null;
    private @Nullable ItemApiLookup<?, ContainerItemContext> cachedLookup = null;
    private @Nullable Object cachedApi = null;

    /**
     * Creates an item slot with no logical target metadata.
     *
     * @param transferType transfer mode
     * @param display optional display data
     * @param externalFilter item filter
     * @param capacity slot capacity
     */
    public ItemResourceSlotImpl(
            final @NotNull TransferType transferType,
            final @Nullable ItemSlotDisplay display,
            final @NotNull ResourceFilter<Item> externalFilter,
            final int capacity
    ) {
        this(
                transferType,
                display,
                externalFilter,
                capacity,
                null,
                Set.of()
        );
    }

    /**
     * Creates an item slot with logical target metadata.
     *
     * @param transferType transfer mode
     * @param display optional display data
     * @param externalFilter item filter
     * @param capacity slot capacity
     * @param id optional exact slot id
     * @param groups logical slot groups
     */
    public ItemResourceSlotImpl(
            final @NotNull TransferType transferType,
            final @Nullable ItemSlotDisplay display,
            final @NotNull ResourceFilter<Item> externalFilter,
            final int capacity,
            final @Nullable ResourceLocation id,
            final @NotNull Set<ResourceLocation> groups
    ) {
        super(
                transferType,
                externalFilter,
                capacity
        );

        assert capacity > 0 && capacity <= 64;

        this.display = display;
        this.id = id;
        this.groups = Set.copyOf(groups);
    }

    @Override
    public @Nullable ResourceLocation id() {
        return this.id;
    }

    @Override
    public @NotNull Set<ResourceLocation> groups() {
        return this.groups;
    }

    @Override
    public long getRealCapacity() {
        assert this.isSane();

        return Math.min(
                this.capacity,
                this.resource == null ? 64 : this.resource.getDefaultMaxStackSize()
        );
    }

    @Override
    public long getCapacityFor(
            final @NotNull Item item,
            final @NotNull DataComponentPatch components
    ) {
        final Optional<? extends Integer> optional = components.get(DataComponents.MAX_STACK_SIZE);

        if (optional != null && optional.isPresent()) {
            return optional.get();
        }

        return Math.min(
                this.capacity,
                item.getDefaultMaxStackSize()
        );
    }

    @Override
    public @Nullable Item consumeOne() {
        final DataComponentPatch tag = this.components;
        final Item resource = this.extractOne();

        if (resource == null) {
            return null;
        }

        if (resource.hasCraftingRemainingItem()) {
            this.insertRemainder(
                    resource,
                    tag,
                    1
            );
        }

        return resource;
    }

    @Override
    public boolean consumeOne(
            final @NotNull Item resource,
            final @Nullable DataComponentPatch components
    ) {
        final DataComponentPatch actual = this.components;

        if (this.extractOne(
                resource,
                components
        )) {
            this.insertRemainder(
                    resource,
                    actual,
                    1
            );

            return true;
        }

        return false;
    }

    @Override
    public long consume(final long amount) {
        final Item item = this.resource;

        if (item == null) {
            return 0;
        }

        final DataComponentPatch components = this.components;
        final long consumed = this.extract(amount);

        if (consumed > 0) {
            this.insertRemainder(
                    item,
                    components,
                    1
            );
        }

        return consumed;
    }

    @Override
    public long consume(
            final @NotNull Item resource,
            final @Nullable DataComponentPatch components,
            final long amount
    ) {
        final DataComponentPatch actual = this.components;
        final long consumed = this.extract(
                resource,
                components,
                amount
        );

        if (consumed > 0) {
            this.insertRemainder(
                    resource,
                    actual,
                    (int) consumed
            );
        }

        return consumed;
    }

    /**
     * Inserts crafting remainder items back into this slot when possible.
     *
     * @param resource consumed item type
     * @param tag consumed item components
     * @param extracted number of items consumed
     */
    private void insertRemainder(
            final @NotNull Item resource,
            final @NotNull DataComponentPatch tag,
            final int extracted
    ) {
        if (!resource.hasCraftingRemainingItem() || !this.isEmpty()) {
            return;
        }

        final ItemStack remainder = resource.getRecipeRemainder(ItemStackUtil.of(
                resource,
                tag,
                extracted
        ));

        if (!remainder.isEmpty()) {
            this.insert(
                    remainder.getItem(),
                    remainder.getComponentsPatch(),
                    remainder.getCount()
            );
        }
    }

    @Override
    public @Nullable ItemSlotDisplay getDisplay() {
        return this.display;
    }

    @Override
    public @NotNull CompoundTag createTag() {
        final CompoundTag tag = new CompoundTag();

        if (this.isEmpty()) {
            return tag;
        }

        tag.putString(
                RESOURCE_KEY,
                BuiltInRegistries.ITEM.getKey(this.resource).toString()
        );
        tag.putInt(
                AMOUNT_KEY,
                (int) this.amount
        );

        if (!this.components.isEmpty()) {
            tag.put(
                    COMPONENTS_KEY,
                    DataComponentPatch.CODEC.encodeStart(
                            NbtOps.INSTANCE,
                            this.components
                    ).getOrThrow()
            );
        }

        if (this.transferMode() == TransferType.OUTPUT && this.recipes != null) {
            final ListTag recipeTag = new ListTag();

            for (final ResourceLocation entry : this.recipes) {
                recipeTag.add(StringTag.valueOf(entry.toString()));
            }

            tag.put(
                    RECIPES_KEY,
                    recipeTag
            );
        }

        return tag;
    }

    @Override
    public void readTag(final @NotNull CompoundTag tag) {
        if (tag.isEmpty()) {
            this.setEmpty();
            return;
        }

        this.set(
                BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString(RESOURCE_KEY))),
                tag.contains(COMPONENTS_KEY)
                        ? DataComponentPatch.CODEC.parse(
                        NbtOps.INSTANCE,
                        tag.get(COMPONENTS_KEY)
                ).getOrThrow()
                        : DataComponentPatch.EMPTY,
                tag.getInt(AMOUNT_KEY)
        );

        if (this.transferMode() == TransferType.OUTPUT && tag.contains(RECIPES_KEY, Tag.TAG_LIST)) {
            final ListTag list = tag.getList(
                    RECIPES_KEY,
                    Tag.TAG_STRING
            );

            if (!list.isEmpty()) {
                this.recipes = new HashSet<>(list.size());

                for (int i = 0; i < list.size(); i++) {
                    this.recipes.add(ResourceLocation.parse(list.getString(i)));
                }
            }
        }
    }

    @Override
    public void writePacket(final @NotNull RegistryFriendlyByteBuf buf) {
        if (this.amount > 0) {
            buf.writeInt((int) this.amount);
            buf.writeUtf(Utils.getShortId(BuiltInRegistries.ITEM.getKey(this.resource)));
            DataComponentPatch.STREAM_CODEC.encode(
                    buf,
                    this.components
            );
        } else {
            buf.writeInt(0);
        }
    }

    @Override
    public void readPacket(final @NotNull RegistryFriendlyByteBuf buf) {
        final int amount = buf.readInt();

        if (amount == 0) {
            this.setEmpty();
            return;
        }

        final Item resource = BuiltInRegistries.ITEM.get(ResourceLocation.parse(buf.readUtf()));
        final DataComponentPatch tag = DataComponentPatch.STREAM_CODEC.decode(buf);

        this.set(
                resource,
                tag,
                amount
        );
    }

    @Override
    public <A> @Nullable A find(final ItemApiLookup<A, ContainerItemContext> lookup) {
        if (this.cachedExpiry != this.getModifications() || this.cachedLookup != lookup) {
            this.cachedExpiry = this.getModifications();
            this.cachedLookup = lookup;
            this.cachedApi = ItemResourceSlot.super.find(lookup);
        }

        return (A) this.cachedApi;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        if (this.cachedStorage == null) {
            this.cachedStorage = new InnerSingleSlotStorage();
        }

        return this.cachedStorage;
    }

    @Override
    public ItemVariant getItemVariant() {
        return this.isEmpty()
                ? ItemVariant.blank()
                : ItemVariant.of(
                Objects.requireNonNull(this.resource),
                this.components
        );
    }

    @Override
    public long extract(
            final ItemVariant resource,
            final long maxAmount,
            final TransactionContext transaction
    ) {
        return this.extract(
                resource.getItem(),
                resource.getComponents(),
                maxAmount,
                transaction
        );
    }

    @Override
    public long exchange(
            final ItemVariant newVariant,
            final long maxAmount,
            final TransactionContext transaction
    ) {
        StoragePreconditions.notBlankNotNegative(
                newVariant,
                maxAmount
        );

        if (newVariant.getItem() == this.resource && this.components.equals(newVariant.getComponents())) {
            return Math.min(
                    this.amount,
                    maxAmount
            );
        }

        if (this.amount == maxAmount && this.getCapacityFor(
                newVariant.getItem(),
                newVariant.getComponents()
        ) >= maxAmount) {
            this.updateSnapshots(transaction);
            this.set(
                    newVariant.getItem(),
                    newVariant.getComponents(),
                    maxAmount
            );

            return maxAmount;
        }

        return 0;
    }

    @Override
    public long insert(
            final ItemVariant resource,
            final long maxAmount,
            final TransactionContext transaction
    ) {
        return this.insert(
                resource.getItem(),
                resource.getComponents(),
                maxAmount,
                transaction
        );
    }

    @Override
    public long insertOverflow(
            final ItemVariant itemVariant,
            final long maxAmount,
            final TransactionContext context
    ) {
        return this.parent.insert(
                itemVariant.getItem(),
                itemVariant.getComponents(),
                maxAmount,
                context
        );
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return Collections.emptyList();
    }

    @Override
    public boolean isSane() {
        return super.isSane() && this.resource != Items.AIR && this.amount <= Integer.MAX_VALUE;
    }

    @Override
    public @Nullable Set<ResourceLocation> takeRecipes() {
        final Set<ResourceLocation> recipes = this.recipes;
        this.recipes = null;
        return recipes;
    }

    @Override
    public void recipeCrafted(final @NotNull ResourceLocation id) {
        if (this.recipes == null) {
            if (this.transferMode() == TransferType.OUTPUT) {
                this.recipes = new HashSet<>();
            } else {
                return;
            }
        }

        this.recipes.add(id);
    }

    /**
     * Fabric single-slot wrapper for this item slot.
     */
    private class InnerSingleSlotStorage implements SingleSlotStorage<ItemVariant> {

        @Override
        public long insert(
                final ItemVariant resource,
                final long maxAmount,
                final TransactionContext transaction
        ) {
            return ItemResourceSlotImpl.this.insert(
                    resource.getItem(),
                    resource.getComponents(),
                    maxAmount,
                    transaction
            );
        }

        @Override
        public long extract(
                final ItemVariant resource,
                final long maxAmount,
                final TransactionContext transaction
        ) {
            return ItemResourceSlotImpl.this.extract(
                    resource.getItem(),
                    resource.getComponents(),
                    maxAmount,
                    transaction
            );
        }

        @Override
        public boolean isResourceBlank() {
            return ItemResourceSlotImpl.this.isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemResourceSlotImpl.this.isEmpty()
                    ? ItemVariant.blank()
                    : ItemVariant.of(
                    Objects.requireNonNull(ItemResourceSlotImpl.this.resource),
                    ItemResourceSlotImpl.this.components
            );
        }

        @Override
        public long getAmount() {
            return ItemResourceSlotImpl.this.getAmount();
        }

        @Override
        public long getCapacity() {
            return ItemResourceSlotImpl.this.getRealCapacity();
        }

        @Override
        public long getVersion() {
            return ItemResourceSlotImpl.this.getModifications();
        }
    }
}