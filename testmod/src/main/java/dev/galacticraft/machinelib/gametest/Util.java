package dev.galacticraft.machinelib.gametest;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Util {
    @Contract("-> new")
    public static @NotNull NbtCompound generateNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("id", UUID.randomUUID());
        nbt.putLong("timestamp", System.currentTimeMillis());
        return nbt;
    }
}
