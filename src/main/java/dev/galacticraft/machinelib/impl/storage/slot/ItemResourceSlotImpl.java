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

import dev.galacticraft.machinelib.api.storage.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
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

public class ItemResourceSlotImpl extends ResourceSlotImpl<Item, ItemStack> implements ItemResourceSlot {
    private final @NotNull ItemSlotDisplay display;
    private long cachedExpiry = -1;
    private SingleSlotStorage<ItemVariant> cachedStorage = null;
    private ItemApiLookup<?, ContainerItemContext> cachedLookup = null;
    private Object cachedApi = null;

    public ItemResourceSlotImpl(@NotNull ItemSlotDisplay display, @NotNull ResourceFilter<Item> filter, @NotNull ResourceFilter<Item> externalFilter, int capacity) {
        super(filter, externalFilter, capacity);
        this.display = display;
        assert capacity > 0 && capacity <= 64;
    }

    @Override
    public long getRealCapacity() {
        return Math.min(this.getCapacity(), this.getResource() == null ? 64 : this.getResource().getMaxStackSize());
    }

    @Override
    public long getCapacityFor(@NotNull Item item) {
        return Math.min(this.getCapacity(), item.getMaxStackSize());
    }

    @Override
    public @NotNull ItemStack createStack() {
        if (this.isEmpty()) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(this.getResource(), (int) this.getAmount());
        stack.setTag(this.getTag());
        return stack;
    }

    @Override
    public @NotNull ItemStack copyStack() {
        if (this.isEmpty()) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(this.getResource(), (int) this.getAmount());
        stack.setTag(this.copyTag());
        return stack;
    }

    @Override
    public boolean canInsertStack(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return true;
        assert stack.getItem() != Items.AIR && stack.getCount() > 0;
        return this.canInsert(stack.getItem(), stack.getTag(), stack.getCount());
    }

    @Override
    public long tryInsertStack(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return 0;
        assert stack.getItem() != Items.AIR && stack.getCount() > 0;
        return this.tryInsert(stack.getItem(), stack.getTag(), stack.getCount());
    }

    @Override
    public long insertStack(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return 0;
        assert stack.getItem() != Items.AIR && stack.getCount() > 0;
        return this.insert(stack.getItem(), stack.getTag(), stack.getCount());
    }

    @Override
    public @NotNull ItemSlotDisplay getDisplay() {
        return this.display;
    }

    @Override
    public @NotNull CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        if (this.isEmpty()) return tag;
        tag.putString(RESOURCE_KEY, BuiltInRegistries.ITEM.getKey(this.getResource()).toString());
        tag.putInt(AMOUNT_KEY, (int) this.getAmount());
        if (this.getTag() != null && !this.getTag().isEmpty()) tag.put(TAG_KEY, this.getTag());
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
        if (this.getAmount() > 0) {
            buf.writeInt((int) this.getAmount());
            buf.writeUtf(BuiltInRegistries.ITEM.getKey(this.getResource()).toString());
            buf.writeNbt(this.getTag());
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
                    return ItemResourceSlotImpl.this.isEmpty() ? ItemVariant.blank() : ItemVariant.of(Objects.requireNonNull(ItemResourceSlotImpl.this.getResource()), ItemResourceSlotImpl.this.getTag());
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
        return ItemResourceSlotImpl.this.isEmpty() ? ItemVariant.blank() : ItemVariant.of(Objects.requireNonNull(ItemResourceSlotImpl.this.getResource()), ItemResourceSlotImpl.this.getTag());
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return this.extract(resource.getItem(), resource.getNbt(), maxAmount, transaction);
    }

    @Override
    public long exchange(ItemVariant newVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(newVariant, maxAmount);

        if (newVariant.getItem() == this.getResource() && Utils.tagsEqual(this.getTag(), newVariant.getNbt())) {
            return Math.min(this.getAmount(), maxAmount);
        }

        if (this.getAmount() == maxAmount && this.getCapacityFor(newVariant.getItem()) >= maxAmount) {
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
}
