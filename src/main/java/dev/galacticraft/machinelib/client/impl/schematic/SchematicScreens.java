package dev.galacticraft.machinelib.client.impl.schematic;

import dev.galacticraft.machinelib.impl.schematic.MachineLibSchematicContent;
import net.minecraft.client.gui.screens.MenuScreens;

public class SchematicScreens {
    public static void register() {
        if (MachineLibSchematicContent.isRegistered()) {
            MenuScreens.register(
                    MachineLibSchematicContent.schematicWorkbenchMenu(),
                    SchematicWorkbenchScreen::new
            );
        }
    }
}
