package dev.galacticraft.machinelib.gametest.storage.item;

import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.impl.machine.storage.MachineItemStorageImpl;
import dev.galacticraft.machinelib.gametest.MachineLibGametest;
import dev.galacticraft.machinelib.gametest.Util;
import dev.galacticraft.machinelib.testmod.TestMod;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.TestContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.galacticraft.machinelib.gametest.Assertions.assertEquals;
import static net.minecraft.item.Items.GOLD_INGOT;
import static net.minecraft.item.Items.IRON_INGOT;

public final class SingleSlotItemStorageExtractionTest implements MachineLibGametest {
    private MachineItemStorageImpl storage;

    @Override
    public void beforeEach(@NotNull TestContext context) {
        this.storage = (MachineItemStorageImpl) MachineItemStorage.Builder.create()
                .addSlot(TestMod.NO_DIAMOND_SLOT, 64, new ItemSlotDisplay(0, 0))
                .build();
    }

    @Override
    public void afterEach(@NotNull TestContext context) {
        this.storage = null;
    }

    private void extract$empty_fail() {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(variant(GOLD_INGOT), 1, transaction));
        }
    }

    private void extract$single() {
        this.storage.setSlot(0, variant(GOLD_INGOT), 1);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(1, this.storage.extract(variant(GOLD_INGOT), 1, transaction));
        }
    }

    private void extract$multiple() {
        this.storage.setSlot(0, variant(GOLD_INGOT), 32);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(16, this.storage.extract(variant(GOLD_INGOT), 16, transaction));
        }
    }

    private void extract$multiple_exact() {
        this.storage.setSlot(0, variant(GOLD_INGOT), 16);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(16, this.storage.extract(variant(GOLD_INGOT), 16, transaction));
        }
    }

    private void extract$type_fail() {
        this.storage.setSlot(0, variant(IRON_INGOT), 32);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(variant(GOLD_INGOT), 16, transaction));
        }
    }

    private void extract$type_fail_nbt() {
        this.storage.setSlot(0, variant(GOLD_INGOT), 32);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(variant(GOLD_INGOT, Util.generateNbt()), 16, transaction));
        }
    }

    private void extract$nbt_type_fail() {
        this.storage.setSlot(0, variant(GOLD_INGOT, Util.generateNbt()), 32);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(variant(GOLD_INGOT), 16, transaction));
        }
    }

    private void extract$different_nbt_type_fail() {
        this.storage.setSlot(0, variant(GOLD_INGOT, Util.generateNbt()), 32);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(variant(GOLD_INGOT, Util.generateNbt()), 16, transaction));
        }
    }

    private void extract$overflow() {
        this.storage.setSlot(0, variant(GOLD_INGOT), 5);
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(5, this.storage.extract(variant(GOLD_INGOT), 10, transaction));
        }
    }

    private static @NotNull ItemVariant variant(@NotNull Item item, @Nullable NbtCompound nbt) {
        return ItemVariant.of(item, nbt);
    }
    
    private static @NotNull ItemVariant variant(@NotNull Item item) {
        return ItemVariant.of(item);
    }
}
