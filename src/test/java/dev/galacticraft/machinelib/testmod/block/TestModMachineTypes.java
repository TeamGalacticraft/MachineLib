package dev.galacticraft.machinelib.testmod.block;

import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.SimpleMachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupTypes;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.testmod.block.entity.SimpleMachineBlockEntity;
import dev.galacticraft.machinelib.testmod.block.entity.TestModBlockEntityTypes;
import dev.galacticraft.machinelib.testmod.menu.TestModMenuTypes;
import dev.galacticraft.machinelib.testmod.slot.TestModSlotGroupTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TestModMachineTypes {
    public static final MachineType<SimpleMachineBlockEntity, SimpleMachineMenu<SimpleMachineBlockEntity>> SIMPLE_MACHINE = MachineType.create(TestModBlocks.SIMPLE_MACHINE_BLOCK,
            TestModBlockEntityTypes.SIMPLE_MACHINE,
            TestModMenuTypes.SIMPLE_MACHINE,
            () -> MachineEnergyStorage.of(30000, 300, 300, true, false),
            () -> MachineItemStorage.builder()
                    .addGroup(
                            SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                                    .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.CAN_EXTRACT_ENERGY, ResourceFilters.CAN_EXTRACT_ENERGY_STRICT, 32))
                                    .build()
                    )
                    .addGroup(
                            SlotGroup.<Item, ItemStack, ItemResourceSlot>create(TestModSlotGroupTypes.DIRT)
                                    .add(ItemResourceSlot.create(ItemSlotDisplay.create(16, 0), ResourceFilters.matchAnyNbt(Items.DIRT)))
                                    .build()
                    )
                    .addGroup(
                            SlotGroup.<Item, ItemStack, ItemResourceSlot>create(TestModSlotGroupTypes.DIAMONDS)
                                    .add(ItemResourceSlot.create(ItemSlotDisplay.create(32, 0), ResourceFilters.matchAnyNbt(Items.DIAMOND)))
                                    .build()
                    )
                    .build(),
            MachineFluidStorage::empty
    );

    public static void initialize() {
    }
}
