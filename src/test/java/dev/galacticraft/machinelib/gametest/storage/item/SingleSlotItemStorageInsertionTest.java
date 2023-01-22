///*
// * Copyright (c) 2021-2023 Team Galacticraft
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//package dev.galacticraft.machinelib.gametest.storage.item;
//
//import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
//import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
//import dev.galacticraft.machinelib.gametest.MachineLibGametest;
//import dev.galacticraft.machinelib.gametest.annotation.SingleSlotItemStorage;
//import dev.galacticraft.machinelib.gametest.misc.ItemType;
//import dev.galacticraft.machinelib.impl.storage.MachineItemStorageImpl;
//import dev.galacticraft.machinelib.impl.storage.slot.SlotGroupTypeImpl;
//import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
//import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
//import net.minecraft.ChatFormatting;
//import net.minecraft.gametest.framework.GameTest;
//import net.minecraft.gametest.framework.GameTestHelper;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.TextColor;
//import org.jetbrains.annotations.NotNull;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Objects;
//
//import static dev.galacticraft.machinelib.gametest.Assertions.*;
//
//@SuppressWarnings({"UnstableApiUsage", "unused"})
//public final class SingleSlotItemStorageInsertionTest implements MachineLibGametest {
//    @Override
//    public void invokeTestMethod(@NotNull GameTestHelper context, @NotNull Method method) {
//        GameTest gameTest = method.getAnnotation(GameTest.class);
//        SingleSlotItemStorage test = method.getAnnotation(SingleSlotItemStorage.class);
//        if (gameTest == null) throw new AssertionError("Test method without gametest annotation?!");
//        if (gameTest.timeoutTicks() == 0 && test != null) {
//            method.setAccessible(true);
//            MachineItemStorageImpl impl = (MachineItemStorageImpl) MachineItemStorage.builder().addSlot(
//                    new SlotGroupTypeImpl(Objects.requireNonNull(TextColor.fromLegacyFormat(ChatFormatting.WHITE)),
//                            Component.empty().copy(),
//
//                            true), v -> {
//                        if (test.blockNbt()) {
//                            if (v.getNbt() != null && !v.getNbt().isEmpty()) {
//                                return false;
//                            }
//                        }
//                        if (test.block() != ItemType.NONE) {
//                            return !v.isOf(test.block().generateVariant().getItem());
//                        }
//                        return true;
//                    }, true, test.maxCount(), ItemSlotDisplay.create(0, 0)).build();
//            if (test.amount() > 0) {
//                impl.setSlot(0, test.type().generateVariant(0), test.amount());
//            }
//            context.succeedWhen(() -> {
//                try {
//                    method.invoke(this, impl, test.type());
//                } catch (IllegalAccessException e) {
//                    throw new RuntimeException(e);
//                } catch (InvocationTargetException e) {
//                    if (e.getTargetException() instanceof RuntimeException ex) {
//                        if (PRINT_ERRORS) {
//                            if (PRINT_STACKTRACE) {
//                                MachineLibGametest.LOGGER.error("", ex);
//                            } else {
//                                MachineLibGametest.LOGGER.error(ex.getMessage());
//                            }
//                        }
//                        throw ex;
//                    } else {
//                        throw new RuntimeException(e);
//                    }
//                }
//            });
//        } else {
//            MachineLibGametest.super.invokeTestMethod(context, method);
//        }
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage
//    void insert$fail_nothing(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertThrows(() -> storage.insert(ItemVariant.blank(), 1, transaction));
//        }
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage
//    void insert$fail_negative(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertThrows(() -> storage.insert(type.generateVariant(), -1, transaction));
//        }
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage
//    void insert$empty(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(5, storage.insert(variant, 5, transaction));
//            assertEquals(5, storage.count(variant));
//        }
//        assertTrue(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage
//    void insert$empty_nbt(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(8, storage.insert(variant, 8, transaction));
//            assertEquals(8, storage.count(variant));
//        }
//        assertTrue(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage
//    void insert$empty_overflow(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(64, storage.insert(variant, 65, transaction));
//            assertEquals(64, storage.count(variant));
//        }
//        assertTrue(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(maxCount = 32)
//    void insert$empty_overflow_small_slot(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(32, storage.insert(variant, 64, transaction));
//            assertEquals(32, storage.count(variant));
//        }
//        assertTrue(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(type = ItemType.STACK_16)
//    void insert$empty_overflow_item(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(16, storage.insert(variant, 32, transaction));
//            assertEquals(16, storage.count(variant));
//        }
//        assertTrue(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(amount = 48)
//    void insert$partially_filled(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(16, storage.insert(variant, 64, transaction));
//            assertEquals(64, storage.count(variant));
//        }
//        assertEquals(48, storage.count(variant));
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(amount = 64)
//    void insert$filled(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(0, storage.insert(variant, 64, transaction));
//            assertEquals(64, storage.count(variant));
//        }
//        assertEquals(64, storage.count(variant));
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(amount = 1)
//    void insert$filled_type_full(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant(1);
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(0, storage.insert(variant, 64, transaction));
//            assertEquals(0, storage.count(variant));
//        }
//        assertEquals(1, storage.count(type.generateVariant(0)));
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(type = ItemType.STACK_64_NBT, amount = 1)
//    void insert$filled_nbt_type_full(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.getNbtInverse().generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(0, storage.insert(variant, 64, transaction));
//        }
//        assertFalse(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(type = ItemType.STACK_64, amount = 1)
//    void insert$filled_type_full_nbt(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.getNbtInverse().generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(0, storage.insert(variant, 64, transaction));
//        }
//        assertEquals(1, storage.count(type.generateVariant()));
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(type = ItemType.STACK_64_NBT, amount = 1)
//    void insert$filled_type_full_different_nbt(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(0, storage.insert(variant, 64, transaction));
//        }
//        assertFalse(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(type = ItemType.STACK_64_NBT)
//    void insert$partially_filled_nbt(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(16, storage.insert(variant, 16, transaction));
//            assertEquals(32, storage.insert(variant, 32, transaction));
//        }
//        assertTrue(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage
//    void insert$fill_overflow(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(64, storage.insert(variant, 96, transaction));
//            assertEquals(64, storage.count(variant));
//        }
//        assertTrue(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(maxCount = 16)
//    void insert$fill_overflow_small(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(16, storage.insert(variant, 64, transaction));
//            assertEquals(16, storage.count(variant));
//        }
//        assertTrue(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(type = ItemType.STACK_1)
//    void insert$fill_overflow_item(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(1, storage.insert(variant, 8, transaction));
//            assertEquals(1, storage.count(variant));
//        }
//        assertTrue(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(block = ItemType.STACK_64)
//    void insert$filter(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        ItemVariant variant2 = type.generateVariant(1);
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(0, storage.insert(variant, 64, transaction));
//            assertEquals(0, storage.count(variant));
//        }
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(64, storage.insert(variant2, 64, transaction));
//            assertEquals(64, storage.count(variant2));
//        }
//        assertTrue(storage.isEmpty());
//    }
//
//    @GameTest(template = EMPTY_STRUCTURE, batch = "item_storage/insert/single", timeoutTicks = 0)
//    @SingleSlotItemStorage(blockNbt = true, type = ItemType.STACK_16_NBT)
//    void insert$filter_nbt(@NotNull MachineItemStorageImpl storage, @NotNull ItemType type) {
//        ItemVariant variant = type.generateVariant();
//        ItemVariant variant2 = type.getNbtInverse().generateVariant();
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(0, storage.insert(variant, 16, transaction));
//            assertEquals(0, storage.count(variant));
//        }
//        try (Transaction transaction = Transaction.openOuter()) {
//            assertEquals(16, storage.insert(variant2, 16, transaction));
//            assertEquals(16, storage.count(variant2));
//        }
//        assertTrue(storage.isEmpty());
//    }
//}
