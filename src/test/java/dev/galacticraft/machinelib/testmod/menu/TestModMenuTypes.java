package dev.galacticraft.machinelib.testmod.menu;

import dev.galacticraft.machinelib.api.menu.SimpleMachineMenu;
import dev.galacticraft.machinelib.testmod.Constant;
import dev.galacticraft.machinelib.testmod.block.TestModMachineTypes;
import dev.galacticraft.machinelib.testmod.block.entity.SimpleMachineBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public class TestModMenuTypes {
    public static final MenuType<SimpleMachineMenu<SimpleMachineBlockEntity>> SIMPLE_MACHINE = SimpleMachineMenu.createType(() -> TestModMachineTypes.SIMPLE_MACHINE, 8, 84);

    public static void register() {
        Registry.register(BuiltInRegistries.MENU, Constant.id(Constant.SIMPLE_MACHINE), SIMPLE_MACHINE);
    }
}
