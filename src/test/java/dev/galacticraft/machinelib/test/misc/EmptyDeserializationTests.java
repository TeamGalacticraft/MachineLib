package dev.galacticraft.machinelib.test.misc;

import dev.galacticraft.machinelib.impl.machine.MachineIOConfigImpl;
import dev.galacticraft.machinelib.impl.machine.SecuritySettingsImpl;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmptyDeserializationTests {
    @Test
    public void securitySettings() {
        SecuritySettingsImpl securitySettings = new SecuritySettingsImpl();
        Assertions.assertDoesNotThrow(() -> securitySettings.readTag(new CompoundTag()));
    }

    @Test
    public void ioConfiguration() {
        MachineIOConfigImpl ioConfig = new MachineIOConfigImpl();
        Assertions.assertDoesNotThrow(() -> ioConfig.readTag(new CompoundTag()));
    }
}
