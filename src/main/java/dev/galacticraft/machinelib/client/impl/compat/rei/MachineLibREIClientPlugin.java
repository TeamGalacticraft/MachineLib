package dev.galacticraft.machinelib.client.impl.compat.rei;

import dev.galacticraft.machinelib.client.api.screen.MachineScreen;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;

public class MachineLibREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerFocusedStack(MachineFocusedStackProvider.INSTANCE);
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(MachineScreen.class, ConfigPanelExclusionZonesProvider.INSTANCE);
    }
}
