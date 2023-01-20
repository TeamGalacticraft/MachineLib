package dev.galacticraft.machinelib.api.storage;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public
interface ResourceFilter<Resource> {
    @Contract(pure = true)
    boolean test(@Nullable Resource resource, @Nullable CompoundTag tag);
}
