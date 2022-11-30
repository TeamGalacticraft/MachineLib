package dev.galacticraft.machinelib.impl;

import dev.galacticraft.machinelib.api.machine.MachineStatuses;

public final class MachineLib {
    public static void initialize() {
        MachineStatuses.init();
    }
}
