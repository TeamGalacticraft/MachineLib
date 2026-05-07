package dev.galacticraft.machinelib.impl;

import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuOpeningData;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.impl.multiblock.TestIronCubeMultiblockMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public final class TestMenuTypeRegistry {

    public static final StorageSpec TEST_IRON_CUBE_STORAGE =
            StorageSpec.empty();

    public static final MenuType<TestIronCubeMultiblockMenu> TEST_IRON_CUBE_MULTIBLOCK =
            new ExtendedScreenHandlerType<>(
                    (syncId, inventory, openingData) ->
                            new TestIronCubeMultiblockMenu(
                                    syncId,
                                    inventory,
                                    openingData,
                                    TEST_IRON_CUBE_STORAGE
                            ),
                    MultiblockMenuOpeningData.STREAM_CODEC
            );

    private TestMenuTypeRegistry() {

    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.MENU,
                Constant.id("test_iron_cube_multiblock"),
                TEST_IRON_CUBE_MULTIBLOCK
        );
    }

}