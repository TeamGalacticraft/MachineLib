/*
 * Copyright (c) 2021-2023 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import dev.galacticraft.machinelib.impl.Utils;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ItemResourceSlotImpl extends ResourceSlotImpl<Item> implements ItemResourceSlot {
    private final @NotNull ItemSlotDisplay display;
    private long cachedExpiry = -1;
    private SingleSlotStorage<ItemVariant> cachedStorage = null;
    private ItemApiLookup<?, ContainerItemContext> cachedLookup = null;
    private Object cachedApi = null;

    public ItemResourceSlotImpl(@NotNull InputType inputType, @NotNull ItemSlotDisplay display, @NotNull ResourceFilter<Item> externalFilter, int capacity) {
        super(inputType, externalFilter, capacity);
        assert capacity > 0 && capacity <= 64;
        this.display = display;
    }

    @Override
    public long getRealCapacity() {
        assert this.isSane();
        return Math.min(this.capacity, this.resource == null ? 64 : this.resource.getMaxStackSize());
    }

    @Override
    public long getCapacityFor(@NotNull Item item) {
        return Math.min(this.capacity, item.getMaxStackSize());
    }

    @Override
    public @Nullable Item consumeOne() {
        CompoundTag tag = this.tag;
        Item resource = this.extractOne();
        if (resource == null) return null;
        if (resource.hasCraftingRemainingItem()) {
            this.insertRemainder(resource, tag, 1);
        }
        return resource;
    }

    @Override
    public boolean consumeOne(@NotNull Item resource) {
        CompoundTag tag = this.tag;
        if (this.extractOne(resource)) {
            this.insertRemainder(resource, tag, 1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean consumeOne(@NotNull Item resource, @Nullable CompoundTag tag) {
        if (this.extractOne(resource, tag)) {
            this.insertRemainder(resource, tag, 1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long consume(long amount) {
        Item item = this.resource;
        if (item == null) return 0;
        CompoundTag tag = this.tag;
        long consumed = this.extract(amount);
        if (consumed > 0) {
            this.insertRemainder(item, tag, 1);
            return consumed;
        }
        return consumed;
    }

    @Override
    public long consume(@NotNull Item resource, long amount) {
        CompoundTag tag = this.tag;
        long consumed = this.extract(resource, amount);
        if (consumed > 0) {
            this.insertRemainder(resource, tag, (int) consumed);
        }
        return consumed;
    }

    @Override
    public long consume(@NotNull Item resource, @Nullable CompoundTag tag, long amount) {
        long consumed = this.extract(resource, tag, amount);
        if (consumed > 0) {
            this.insertRemainder(resource, tag, (int) consumed);
        }
        return consumed;
    }

    private void insertRemainder(@NotNull Item resource, @Nullable CompoundTag tag, int extracted) {
        if (resource.hasCraftingRemainingItem()) {
            if (this.isEmpty()) {
                ItemStack remainder = resource.getRecipeRemainder(ItemStackUtil.of(resource, tag, extracted));
                if (!remainder.isEmpty()) {
                    this.insert(remainder.getItem(), remainder.getTag(), remainder.getCount());
                }
            }
        }
    }

    @Override
    public @NotNull ItemSlotDisplay getDisplay() {
        return this.display;
    }

    @Override
    public @NotNull CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        if (this.isEmpty()) return tag;
        tag.putString(RESOURCE_KEY, BuiltInRegistries.ITEM.getKey(this.resource).toString());
        tag.putInt(AMOUNT_KEY, (int) this.amount);
        if (this.tag != null && !this.tag.isEmpty()) tag.put(TAG_KEY, this.tag);
        return tag;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        if (tag.isEmpty()) {
            this.setEmpty();
        } else {
            this.set(BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString(RESOURCE_KEY))), tag.contains(TAG_KEY, Tag.TAG_COMPOUND) ? tag.getCompound(TAG_KEY) : null, tag.getInt(AMOUNT_KEY));
        }
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        if (this.amount > 0) {
            buf.writeInt((int) this.amount);
            buf.writeUtf(BuiltInRegistries.ITEM.getKey(this.resource).toString());
            buf.writeNbt(this.tag);
        } else {
            buf.writeInt(0);
        }
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        int amount = buf.readInt();
        if (amount == 0) {
            this.setEmpty();
        } else {
            Item resource = BuiltInRegistries.ITEM.get(new ResourceLocation(buf.readUtf()));
            CompoundTag tag = buf.readNbt();
            this.set(resource, tag, amount);
        }
    }

    @Override
    public <A> @Nullable A find(ItemApiLookup<A, ContainerItemContext> lookup) {
        if (this.cachedExpiry != this.getModifications() || this.cachedLookup != lookup) {
            this.cachedExpiry = this.getModifications();
            this.cachedApi = ItemResourceSlot.super.find(lookup);
            this.cachedLookup = lookup;
        }
        return (A) this.cachedApi;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        if (this.cachedStorage == null) {
            this.cachedStorage = new SingleSlotStorage<>() {
                @Override
                public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                    return ItemResourceSlotImpl.this.insert(resource.getItem(), resource.getNbt(), maxAmount, transaction);
                }

                @Override
                public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
                    return ItemResourceSlotImpl.this.extract(resource.getItem(), resource.getNbt(), maxAmount, transaction);
                }

                @Override
                public boolean isResourceBlank() {
                    return ItemResourceSlotImpl.this.isEmpty();
                }

                @Override
                public ItemVariant getResource() {
                    return ItemResourceSlotImpl.this.isEmpty() ? ItemVariant.blank() : ItemVariant.of(Objects.requireNonNull(ItemResourceSlotImpl.this.resource), ItemResourceSlotImpl.this.tag);
                }

                @Override
                public long simulateInsert(ItemVariant resource, long maxAmount, @Nullable TransactionContext transaction) {
                    return ItemResourceSlotImpl.this.tryInsert(resource.getItem(), resource.getNbt(), maxAmount);
                }

                @Override
                public long simulateExtract(ItemVariant resource, long maxAmount, @Nullable TransactionContext transaction) {
                    return ItemResourceSlotImpl.this.tryExtract(resource.getItem(), resource.getNbt(), maxAmount);
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
            };
        }
        return this.cachedStorage;
    }

    @Override
    public ItemVariant getItemVariant() {
        return ItemResourceSlotImpl.this.isEmpty() ? ItemVariant.blank() : ItemVariant.of(Objects.requireNonNull(ItemResourceSlotImpl.this.resource), ItemResourceSlotImpl.this.tag);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.extract(resource.getItem(), resource.getNbt(), maxAmount, transaction);
    }

    @Override
    public long exchange(ItemVariant newVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(newVariant, maxAmount);

        if (newVariant.getItem() == this.resource && Utils.tagsEqual(this.tag, newVariant.getNbt())) {
            return Math.min(this.amount, maxAmount);
        }

        if (this.amount == maxAmount && this.getCapacityFor(newVariant.getItem()) >= maxAmount) {
            this.updateSnapshots(transaction);
            this.set(newVariant.getItem(), newVariant.getNbt(), maxAmount);
            return maxAmount;
        }

        return 0;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.insert(resource.getItem(), resource.getNbt(), maxAmount, transaction);
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
        return 0;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return Collections.emptyList();
    }

    @Override
    public boolean isSane() {
        return super.isSane() && this.resource != Items.AIR;
    }
}
