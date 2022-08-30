package dev.galacticraft.impl.machine;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ModCount {
    @Contract(value = " -> new", pure = true)
    static @NotNull ModCount root() {
        return new RootModCount();
    }

    @Contract("_ -> new")
    static @NotNull ModCount parented(@NotNull ModCount parent) {
        return new ParentedModCount(parent);
    }

    /**
     * Warning: Do not call during a transaction.
     * @return the modification count of this storage.
     */
    long getModCount();

    /**
     * Warning: Modification count can go down if the transaction fails.
     * Do not utilize the resulting mod count in a wider scope than the transaction, unless you are certain that the transaction (and all parents) succeeded
     *
     * @param context the transaction context
     */
    void increment(@NotNull TransactionContext context);

    /**
     * Warning: modification count CAN GO DOWN if a transaction is cancelled.
     * Do not trust that an equal mod count is representative of the inventory's state if you are unsure about the transaction status of the current thread.
     * @return the modification count of this storage.
     */
    long getModCountUnsafe();

    /**
     * Increments the counter without creating a snapshot.
     */
    void incrementUnsafe();
}
