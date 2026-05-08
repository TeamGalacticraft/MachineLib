package dev.galacticraft.machinelib.impl;

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenuSpec;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuOpeningData;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.impl.multiblock.TestIronCubeMultiblockMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

/**
 * Registers test menu types used by MachineLib's development/test content.
 */
public final class TestMenuTypeRegistry {

    public static final StorageSpec TEST_IRON_CUBE_STORAGE = StorageSpec.of(
            MachineItemStorage.spec(
                    ItemResourceSlot.builder(TransferType.INPUT)
                            .pos(8, 62)
                            .filter(ResourceFilters.CAN_INSERT_ENERGY)
                            .capacity(32)
                            .id(Constant.id("charge_slot"))
                            .group(Constant.id("item_inputs")),
                    ItemResourceSlot.builder(TransferType.INPUT)
                            .pos(80, 49)
                            .filter((item, tag) -> {
                                final Integer time = 10000;
                                return time != null && time > 0;
                            })
                            .id(Constant.id("process_input"))
                            .group(Constant.id("item_inputs")),
                    ItemResourceSlot.builder(TransferType.STORAGE)
                            .pos(134, 49)
                            .id(Constant.id("process_output"))
                            .group(Constant.id("item_outputs"))
            ),
            MachineEnergyStorage.spec(30000, 100, 150)
                    .id(Constant.id("main_energy"))
                    .group(Constant.id("energy")),
            MachineFluidStorage.spec(
                    FluidResourceSlot.builder(TransferType.INPUT)
                            .pos(152, 18)
                            .capacity(FluidConstants.BUCKET * 4)
                            .id(Constant.id("fluid_input"))
                            .group(Constant.id("fluid_inputs"))
            )
    );

    public static final MultiblockMachineMenuSpec<TestIronCubeMultiblockMenu> TEST_IRON_CUBE =
            new MultiblockMachineMenuSpec<>(
                    TEST_IRON_CUBE_STORAGE,
                    new ExtendedScreenHandlerType<>(
                            (syncId, inventory, openingData) ->
                                    new TestIronCubeMultiblockMenu(
                                            syncId,
                                            inventory,
                                            openingData,
                                            TEST_IRON_CUBE_STORAGE
                                    ),
                            MultiblockMenuOpeningData.STREAM_CODEC
                    ),
                    TestIronCubeMultiblockMenu::new,
                    Component.literal("Test Iron Cube")
            );

    private TestMenuTypeRegistry() {

    }

    /**
     * Registers MachineLib's test menu types.
     */
    public static void register() {
        Registry.register(
                BuiltInRegistries.MENU,
                Constant.id("test_iron_cube_multiblock"),
                TEST_IRON_CUBE.menuType()
        );
    }

}