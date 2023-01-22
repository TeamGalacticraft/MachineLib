package dev.galacticraft.machinelib.gametest.storage;

import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.ContainerSlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupTypes;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.gametest.MachineLibGametest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static dev.galacticraft.machinelib.gametest.Assertions.assertEquals;
import static dev.galacticraft.machinelib.gametest.Assertions.assertTrue;

public final class SlotGroupBuilderTest implements MachineLibGametest {
    @GameTest(template = EMPTY_STRUCTURE, batch = "slot_group", timeoutTicks = 0)
    void create_empty(GameTestHelper context) {
        assertEquals(null, SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE).build());
        assertEquals(null, SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE).add(null).build());
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "slot_group", timeoutTicks = 0)
    void create(GameTestHelper context) {
        assertEquals(1, SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build().size()
        );
        assertEquals(2, SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build().size()
        );
        assertEquals(5, SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build().size()
        );
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = "slot_group", timeoutTicks = 0)
    void item_container_groups(GameTestHelper context) {
        assertTrue(SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build() instanceof ContainerSlotGroup<ItemResourceSlot>
        );
        assertTrue(SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build() instanceof ContainerSlotGroup<ItemResourceSlot>
        );
        assertTrue(SlotGroup.<Item, ItemStack, ItemResourceSlot>create(SlotGroupTypes.CHARGE)
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .add(ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.always()))
                .build() instanceof ContainerSlotGroup<ItemResourceSlot>
        );
    }
}
