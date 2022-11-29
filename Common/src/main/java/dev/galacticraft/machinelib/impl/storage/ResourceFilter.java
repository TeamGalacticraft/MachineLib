package dev.galacticraft.machinelib.impl.storage;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ResourceFilter<T> {
    ResourceFilter<?> ALWAYS = (t, c) -> true;

    boolean matches(@NotNull T type, @Nullable CompoundTag tag);

    static <T> ResourceFilter<T> always() {
        return (ResourceFilter<T>) ALWAYS;
    }
}
