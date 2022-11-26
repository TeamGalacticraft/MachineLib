package dev.galacticraft.machinelib.platform;

import dev.galacticraft.machinelib.api.gas.GasFluid;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.storage.FluidInfo;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.ServiceLoader;

public interface PlatformHelper {
    PlatformHelper INSTANCE = ServiceLoader.load(PlatformHelper.class).findFirst().orElseThrow();

    Registry<MachineStatus> createStatusRegistry();

    GasFluid createGasFluid(@NotNull Component name, @NotNull ResourceLocation texture, @NotNull String symbol, int tint, @NotNull Object2IntFunction<FluidInfo> luminance, @NotNull Object2IntFunction<FluidInfo> viscosity, @NotNull Optional<SoundEvent> fillSound, @NotNull Optional<SoundEvent> emptySound);
}
