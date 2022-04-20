package dev.galacticraft.api.transfer;

import dev.galacticraft.impl.machine.storage.slot.ResourceSlot;
import dev.galacticraft.impl.transfer.StateCachingStorageProviderImpl;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface StateCachingStorageProvider<T> {
    @Contract("_, _ -> new")
    static <T> @NotNull StateCachingStorageProvider<T> create(@NotNull ResourceSlot<Item, ItemVariant, ItemStack> slot, @NotNull ItemApiLookup<T, ContainerItemContext> lookup) {
        return new StateCachingStorageProviderImpl<>(slot, lookup);
    }

    @Nullable T getStorage();

    @Nullable T getStorageTransactionally(@NotNull TransactionContext context);
}
