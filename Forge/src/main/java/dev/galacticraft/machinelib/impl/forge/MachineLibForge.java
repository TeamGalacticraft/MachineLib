package dev.galacticraft.machinelib.impl.forge;

import dev.galacticraft.machinelib.api.gas.Gases;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraftforge.fml.common.Mod;

@Mod(Constant.MOD_ID)
public class MachineLibForge {
    public MachineLibForge() {
        Gases.init();
        MachineStatuses.init();
    }
}
