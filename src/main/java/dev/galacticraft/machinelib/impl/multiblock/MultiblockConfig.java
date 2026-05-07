package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockValidationMode;
import dev.galacticraft.machinelib.impl.MachineLib;

public final class MultiblockConfig {

    private MultiblockConfig() {

    }

    public static MultiblockValidationMode validationMode() {
        return MachineLib.CONFIG.multiblockValidationMode();
    }

    public static int validationBudgetPerTick() {
        return MachineLib.CONFIG.multiblockValidationBudgetPerTick();
    }

}