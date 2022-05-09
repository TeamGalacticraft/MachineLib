package dev.galacticraft.machinelib.gametest.storage.item;

import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.gametest.MachineLibGametest;
import dev.galacticraft.machinelib.testmod.TestMod;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.jetbrains.annotations.NotNull;

import static dev.galacticraft.machinelib.gametest.Assertions.assertEquals;
import static dev.galacticraft.machinelib.gametest.Assertions.assertThrows;

public final class ItemStorageBuilderTest implements MachineLibGametest {
    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void size(@NotNull TestContext context) {
        assertEquals(0, MachineItemStorage.empty().size());
        assertEquals(1, MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, new ItemSlotDisplay(0, 0)).build().size());
        assertEquals(2, MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, new ItemSlotDisplay(0, 0)).addSlot(TestMod.NO_DIAMOND_SLOT, new ItemSlotDisplay(0, 0)).build().size());
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void create_empty(@NotNull TestContext context) {
        assertEquals(MachineItemStorage.empty(), MachineItemStorage.Builder.create().build());
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "item_storage", tickLimit = 0)
    void create_slot_size(@NotNull TestContext context) {
        assertEquals(64, MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, new ItemSlotDisplay(0, 0)).build().getCapacity(0));
        assertEquals(16, MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, 16, new ItemSlotDisplay(0, 0)).build().getCapacity(0));
        assertEquals(64, MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, 5000, new ItemSlotDisplay(0, 0)).build().getCapacity(0));
        assertThrows(() -> MachineItemStorage.Builder.create().addSlot(TestMod.NO_DIAMOND_SLOT, -1, new ItemSlotDisplay(0, 0)));
    }
}
