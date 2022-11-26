package dev.galacticraft.machinelib.fabric.platform;

import com.mojang.serialization.Lifecycle;
import dev.galacticraft.machinelib.api.gas.GasFluid;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.storage.FluidInfo;
import dev.galacticraft.machinelib.fabric.gas.FabricGasAttributeHandler;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.platform.PlatformHelper;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FabricPlatformHelper implements PlatformHelper {
    @Override
    public Registry<MachineStatus> createStatusRegistry() {
        return FabricRegistryBuilder.from(new DefaultedRegistry<>("machinelib:invalid", ResourceKey.<MachineStatus>createRegistryKey(Constant.id("machine_status")), Lifecycle.stable(), null)).buildAndRegister();
    }

    @Override
    public GasFluid createGasFluid(@NotNull Component name, @NotNull ResourceLocation texture, @NotNull String symbol, int tint, @NotNull Object2IntFunction<FluidInfo> luminance, @NotNull Object2IntFunction<FluidInfo> viscosity, @NotNull Optional<SoundEvent> fillSound, @NotNull Optional<SoundEvent> emptySound) {
        GasFluid gas = new GasFluid(name, texture, symbol, tint, luminance, viscosity, fillSound, emptySound);
        FluidVariantAttributes.register(gas, new FabricGasAttributeHandler(gas));
        return gas;
    }
}
