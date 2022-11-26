package dev.galacticraft.machinelib.forge.platform;

import com.mojang.serialization.Lifecycle;
import dev.galacticraft.machinelib.api.gas.GasFluid;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.storage.FluidInfo;
import dev.galacticraft.machinelib.forge.mixin.RegistryAccessor;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.platform.PlatformHelper;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ForgePlatformHelper implements PlatformHelper {
    @Override
    public Registry<MachineStatus> createStatusRegistry() {
        return RegistryAccessor.callRegisterDefaulted(ResourceKey.<MachineStatus>createRegistryKey(Constant.id("machine_status")), "machinelib:invalid", Lifecycle.stable(), null);
    }

    @Override
    public GasFluid createGasFluid(@NotNull Component name, @NotNull ResourceLocation texture, @NotNull String symbol, int tint, @NotNull Object2IntFunction<FluidInfo> luminance, @NotNull Object2IntFunction<FluidInfo> viscosity, @NotNull Optional<SoundEvent> fillSound, @NotNull Optional<SoundEvent> emptySound) {

        return new GasFluid(name, texture, symbol, tint, luminance, viscosity, fillSound, emptySound) {
            private final FluidType type = new FluidType(FluidType.Properties.create().viscosity(getViscosity(new FluidInfo() {})));
            @Override
            public FluidType getFluidType() {
                return type;
            }
        };
    }
}
