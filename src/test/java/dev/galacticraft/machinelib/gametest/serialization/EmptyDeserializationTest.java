package dev.galacticraft.machinelib.gametest.serialization;

import dev.galacticraft.machinelib.api.gametest.GameUnitTest;
import dev.galacticraft.machinelib.api.gametest.annotation.UnitTest;
import dev.galacticraft.machinelib.impl.machine.MachineConfigurationImpl;
import dev.galacticraft.machinelib.impl.machine.MachineIOConfigImpl;
import dev.galacticraft.machinelib.impl.machine.SecuritySettingsImpl;
import net.minecraft.nbt.CompoundTag;

public class EmptyDeserializationTest extends GameUnitTest<Void> {
    public EmptyDeserializationTest() {
        super("empty_deserialization", null);
    }

    @UnitTest
    public void securitySettings() {
        SecuritySettingsImpl securitySettings = new SecuritySettingsImpl();
        securitySettings.readTag(new CompoundTag());
    }

    @UnitTest
    public void ioConfiguration() {
        MachineIOConfigImpl ioConfig = new MachineIOConfigImpl();
        ioConfig.readTag(new CompoundTag());
    }

    @UnitTest
    public void machineConfiguration() {
        MachineConfigurationImpl machineConfiguration = new MachineConfigurationImpl();
        machineConfiguration.readTag(new CompoundTag());
    }
}
