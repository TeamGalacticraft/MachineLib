package dev.galacticraft.machinelib.fabric.gas;

import dev.galacticraft.machinelib.api.gas.GasFluid;
import dev.galacticraft.machinelib.api.storage.FluidInfo;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class FabricGasAttributeHandler implements FluidVariantAttributeHandler {
    protected final GasFluid gasFluid;

    public FabricGasAttributeHandler(GasFluid gasFluid) {
        this.gasFluid = gasFluid;
    }

    @Override
    public Component getName(FluidVariant fluidVariant) {
        return gasFluid.getName();
    }

    @Override
    public Optional<SoundEvent> getFillSound(FluidVariant variant) {
        return gasFluid.getFillSound();
    }

    @Override
    public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
        return gasFluid.getEmptySound();
    }

    @Override
    public int getLuminance(FluidVariant variant) {
        return gasFluid.getLuminance(new FluidInfo() {});
    }

    @Override
    public int getViscosity(FluidVariant variant, @Nullable Level world) {
        return gasFluid.getViscosity(new FluidInfo() {});
    }

    @Override
    public boolean isLighterThanAir(FluidVariant variant) {
        return true;
    }
}
