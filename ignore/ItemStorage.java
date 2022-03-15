package dev.galacticraft.api.machine.storage;

import dev.galacticraft.api.machine.storage.automation.Automatable;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ItemStorage extends Automatable<Item> {
    int size();

    boolean isEmpty();

    @NotNull ItemStack getStack(int slot);

    boolean canExtract(int slot);

    boolean canInsert(int slot);

    boolean canExtractAny();

    boolean canInsertAny();

    default @NotNull ItemStack extractStack(int slot, int amount) {
        return this.extractStack(slot, amount, null);
    }

    default @NotNull ItemStack extractStack(int slot, @NotNull TagKey<Item> tag) {
        return this.extractStack(slot, tag, null);
    }

    default @NotNull ItemStack extractStack(int slot, @NotNull TagKey<Item> tag, int amount) {
        return this.extractStack(slot, tag, amount, null);
    }

    default @NotNull ItemStack extractStack(int slot, @NotNull Item item) {
        return this.extractStack(slot, item, null);
    }

    default @NotNull ItemStack extractStack(int slot, @NotNull Item item, int amount) {
        return this.extractStack(slot, item, amount, null);
    }

    default @NotNull ItemStack extractStack(int slot) {
        return this.extractStack(slot, (TransactionContext) null);
    }

    default @NotNull ItemStack replaceStack(int slot, @NotNull ItemStack stack) {
        return this.replaceStack(slot, stack, null);
    }

    default @NotNull ItemStack insertStack(int slot, @NotNull ItemStack stack) {
        return this.insertStack(slot, stack, null);
    }

    default @NotNull ItemStack insertStack(@NotNull ItemStack stack) {
        return this.insertStack(stack, null);
    }

    @NotNull ItemStack extractStack(int slot, int amount, @Nullable TransactionContext context);

    @NotNull ItemStack extractStack(int slot, @NotNull TagKey<Item> tag, @Nullable TransactionContext context);

    @NotNull ItemStack extractStack(int slot, @NotNull TagKey<Item> tag, int amount, @Nullable TransactionContext context);

    @NotNull ItemStack extractStack(int slot, @NotNull Item item, @Nullable TransactionContext context);

    @NotNull ItemStack extractStack(int slot, @NotNull Item item, int amount, @Nullable TransactionContext context);

    @NotNull ItemStack extractStack(int slot, @Nullable TransactionContext context);

    @NotNull ItemStack replaceStack(int slot, @NotNull ItemStack stack, @Nullable TransactionContext context);

    @NotNull ItemStack insertStack(int slot, @NotNull ItemStack stack, @Nullable TransactionContext context);

    @NotNull ItemStack insertStack(@NotNull ItemStack stack, @Nullable TransactionContext context);

    int getMaxCount(int slot);

    void markDirty();

    boolean canAccess(@NotNull PlayerEntity player);

    boolean canAccept(int slot, @NotNull ItemStack stack);

    int count(@NotNull Item item);

    boolean containsAny(@NotNull Set<Item> items);

    boolean containsAny(@NotNull TagKey<Item> items);

    void clear();

    default ExposedItemStorage getExposedStorage(@NotNull Direction direction) {
        return this.getExposedStorages()[direction.ordinal()];
    }
    ExposedItemStorage[] getExposedStorages();
}
