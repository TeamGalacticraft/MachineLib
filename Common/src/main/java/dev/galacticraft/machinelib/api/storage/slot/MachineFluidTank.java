package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.api.world.level.fluid.FluidStack;
import dev.galacticraft.machinelib.impl.storage.slot.InternalChangeTracking;

public interface MachineFluidTank extends FluidTank, InternalChangeTracking {
    void silentSetStack(FluidStack stack);
}
