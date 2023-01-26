/*
 * Copyright (c) 2021-2023 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.machinelib.gametest.storage;

import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.gametest.MachineLibGametest;
import dev.galacticraft.machinelib.gametest.Util;
import dev.galacticraft.machinelib.impl.Utils;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dev.galacticraft.machinelib.gametest.Assertions.*;
import static net.minecraft.world.item.Items.*;

public final class ResourceSlotInsertionTest implements MachineLibGametest {
    private static final CompoundTag ILLEGAL_NBT = new CompoundTag();
    private ResourceSlot<Item, ItemStack> slot;

    static {
        ILLEGAL_NBT.putBoolean("illegal", true);
    }

    @Override
    public void beforeEach(@NotNull GameTestHelper context) {
        this.slot = ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), (item, tag) -> item != Items.HONEYCOMB && !Utils.tagsEqual(ILLEGAL_NBT, tag));
    }
    
    @Override
    public void afterEach(@NotNull GameTestHelper context) {
        this.slot = null;
    }

    @Override
    public void invokeTestMethod(@NotNull GameTestHelper context, @NotNull Method method) {
        if (method.getAnnotation(GameTest.class) == null && method.getReturnType() == Runnable.class) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] == TransactionContext.class) {
                return;
            }
        }
        MachineLibGametest.super.invokeTestMethod(context, method);
    }

    @GameTestGenerator
    public @NotNull Collection<TestFunction> generate_tests() {
        List<TestFunction> tests = new ArrayList<>();
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(GameTest.class) == null && !method.getName().contains("lambda")) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1 && parameterTypes[0] == TransactionContext.class) {
                    tests.add(new TestFunction("resource_slot", "resource_slot." + method.getName().replace("$", "."), EMPTY_STRUCTURE, Rotation.NONE, 0, 0, true, 1, 1, gameTestHelper -> {
                        this.beforeEach(gameTestHelper);
                        try {
                            Object invoke = method.invoke(this, new Object[]{null});
                            if (invoke != null) ((Runnable) invoke).run();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            if (e.getTargetException() instanceof GameTestAssertException) {
                                throw (GameTestAssertException) e.getTargetException();
                            }
                            throw new RuntimeException(e);
                        } finally {
                            this.afterEach(gameTestHelper);
                        }
                        gameTestHelper.succeed();
                    }));

                    tests.add(new TestFunction("resource_slot", "resource_slot." + method.getName().replace("$", ".") + "_transactive", EMPTY_STRUCTURE, Rotation.NONE, 0, 0, true, 1, 1, gameTestHelper -> {
                        this.beforeEach(gameTestHelper);
                        try (Transaction transaction = Transaction.openOuter()) {
                            ItemStack itemStack = ItemStack.EMPTY;
                            Object invoke = method.invoke(this, transaction);
                            if (invoke != null) {
                                itemStack = this.slot.copyStack();
                                ((Runnable) invoke).run();
                            }
                            transaction.abort();
                            ItemStack itemStack1 = this.slot.copyStack();
                            if (Util.stacksEqual(itemStack, itemStack1)) {
                                gameTestHelper.succeed();
                            } else {
                                gameTestHelper.fail("transaction test failed" + itemStack + " != " + itemStack1);
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            if (e.getTargetException() instanceof GameTestAssertException) {
                                throw (GameTestAssertException) e.getTargetException();
                            }
                            throw new RuntimeException(e);
                        } finally {
                            this.afterEach(gameTestHelper);
                        }
                    }));
                }
            }
        }
        return tests;
    }

    void insert$fail_negative(@Nullable TransactionContext context) {
        assertThrows(() -> this.slot.insert(GOLD_INGOT, -1, context));
        assertThrows(() -> this.slot.insert(GOLD_INGOT, null, -1, context));
    }

    void insert$empty(@Nullable TransactionContext context) {
        assertEquals(8, this.slot.insert(GOLD_INGOT, 8, context));

        assertEquals(GOLD_INGOT, this.slot.getResource());
        assertEquals(null, this.slot.getTag());
        assertEquals(8, this.slot.getAmount());
    }

    void insert$empty_nbt(@Nullable TransactionContext context) {
        CompoundTag tag = Util.generateNbt();
        assertEquals(8, this.slot.insert(GOLD_INGOT, tag, 8, context));

        assertEquals(GOLD_INGOT, this.slot.getResource());
        assertEquals(tag, this.slot.getTag());
        assertEquals(8, this.slot.getAmount());
    }

    void insert$empty_overflow(@Nullable TransactionContext context) {
        assertEquals(64, this.slot.insert(GOLD_INGOT, 100, context));

        assertEquals(GOLD_INGOT, this.slot.getResource());
        assertEquals(null, this.slot.getTag());
        assertEquals(64, this.slot.getAmount());
        assertTrue(this.slot.isFull());
    }

    void insert$empty_overflow_item_limiting(@Nullable TransactionContext context) {
        assertEquals(16, this.slot.insert(EGG, 64, context));

        assertEquals(EGG, this.slot.getResource());
        assertEquals(null, this.slot.getTag());
        assertEquals(16, this.slot.getAmount());
        assertTrue(this.slot.isFull());
    }

    @NotNull Runnable insert$partially_filled(@Nullable TransactionContext context) {
        this.slot.set(GOLD_INGOT, null, 16);

        return () -> assertEquals(48, this.slot.insert(GOLD_INGOT, 64, context));
    }

    @NotNull Runnable insert$filled(@Nullable TransactionContext context) {
        this.slot.set(GOLD_INGOT, null, 64);

        return () -> assertEquals(0, this.slot.insert(GOLD_INGOT, 24, context));
    }

    @NotNull Runnable insert$type_full(@Nullable TransactionContext context) {
        this.slot.set(IRON_INGOT, null, 1);

        return () -> assertEquals(0, this.slot.insert(GOLD_INGOT, 64, context));
    }

    @NotNull Runnable insert$nbt_type_full(@Nullable TransactionContext context) {
        this.slot.set(GOLD_INGOT, null, 1);

        return () -> assertEquals(0, this.slot.insert(GOLD_INGOT, Util.generateNbt(), 64, context));
    }

    @NotNull Runnable insert$filled_nbt_type_full(@Nullable TransactionContext context) {
        this.slot.set(GOLD_INGOT, Util.generateNbt(), 1);

        return () -> assertEquals(0, this.slot.insert(GOLD_INGOT, 64, context));
    }

    @NotNull Runnable insert$filled_nbt_type_different(@Nullable TransactionContext context) {
        this.slot.set(GOLD_INGOT, Util.generateNbt(), 1);

        return () -> assertEquals(0, this.slot.insert(GOLD_INGOT, Util.generateNbt(),64, context));
    }

    @NotNull Runnable insert$fill_nbt(@Nullable TransactionContext context) {
        CompoundTag tag = Util.generateNbt();
        this.slot.set(GOLD_INGOT, tag, 16);

        return () -> assertEquals(48, this.slot.insert(GOLD_INGOT, tag,48, context));
    }

    @NotNull Runnable insert$fill_overflow(@Nullable TransactionContext context) {
        this.slot.set(GOLD_INGOT, null, 16);

        return () -> assertEquals(48, this.slot.insert(GOLD_INGOT,100, context));
    }

    @NotNull Runnable insert$fill_overflow_item_limiting(@Nullable TransactionContext context) {
        this.slot.set(EGG, null, 6);

        return () -> assertEquals(10, this.slot.insert(EGG, 20, context));
    }

    @NotNull Runnable insert$fill_item_full_single(@Nullable TransactionContext context) {
        this.slot.set(STONE_PICKAXE, null, 1);

        return () -> assertEquals(0, this.slot.insert(STONE_PICKAXE, 20, context));
    }

    void insert$filtered(@Nullable TransactionContext context) {
        assertEquals(0, this.slot.insert(HONEYCOMB, 64, context));
    }

    void insert$filtered_nbt(@Nullable TransactionContext context) {
        assertEquals(0, this.slot.insert(IRON_INGOT, ILLEGAL_NBT, 64, context));
        assertEquals(0, this.slot.insert(GOLD_INGOT, ILLEGAL_NBT, 64, context));
    }
}
