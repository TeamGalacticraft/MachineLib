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

package dev.galacticraft.machinelib.api.gametest;

import com.google.common.base.CaseFormat;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.gametest.annotation.InstantTest;
import dev.galacticraft.machinelib.api.gametest.annotation.ProcessingTest;
import dev.galacticraft.machinelib.api.gametest.annotation.container.DefaultedMetadata;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class MachineGameTest<Machine extends MachineBlockEntity> implements FabricGameTest {
    private static final Function<String, String> NAME_CONVERSION = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);

    private final MachineType<Machine, ?> type;

    protected MachineGameTest(MachineType<Machine, ?> type) {
        this.type = type;
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) {
        if (method.getAnnotation(GameTest.class) != null) FabricGameTest.super.invokeTestMethod(context, method);
    }

    @MustBeInvokedByOverriders
    public @NotNull List<TestFunction> generateTests() {
        List<TestFunction> tests = new ArrayList<>();

        tests.add(new TestFunction(this.getBaseId(), this.getBaseId() + "/create_machine", EMPTY_STRUCTURE, Rotation.NONE, 1, 1, true, 1, 1, helper -> {
            if (this.createMachine(helper) == null) {
                throw new GameTestAssertException("No machine assoicated with block!");
            }
            helper.succeed();
        }));

        Class<? extends MachineGameTest<Machine>> clazz = (Class<? extends MachineGameTest<Machine>>) this.getClass();
        DefaultedMetadata meta = clazz.getAnnotation(DefaultedMetadata.class);
        String structure = meta != null ? meta.structure() : EMPTY_STRUCTURE;
        for (Method method : clazz.getMethods()) {
            ProcessingTest processingTest = method.getAnnotation(ProcessingTest.class);
            if (processingTest != null) {
                String subId = "";
                if (!processingTest.group().isBlank()) {
                    subId = "/" + processingTest.group();
                } else {
                    if (meta != null && !meta.group().isBlank()) {
                        subId = "/" + meta.group();
                    }
                }
                tests.add(new TestFunction(this.getBaseId() + subId, this.getBaseId() + '/' + NAME_CONVERSION.apply(method.getName()), structure, Rotation.NONE, processingTest.workTime() + 1, 1, true, 1, 1, helper -> {
                    Machine machine = this.createMachine(helper);
                    if (processingTest.requiresEnergy()) machine.energyStorage().setEnergy(machine.energyStorage().getCapacity());

                    try {
                        Runnable runnable = (Runnable) method.invoke(this, machine);
                        helper.runAfterDelay(processingTest.workTime() + 1, () -> {
                            runnable.run();
                            helper.succeed();
                        });
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to invoke test method!", e);
                    } catch (InvocationTargetException e) {
                        handleException(e);
                    }
                }));
            } else {
                InstantTest instantTest = method.getAnnotation(InstantTest.class);
                if (instantTest != null) {
                    String subId = "";
                    if (!instantTest.group().isBlank()) {
                        subId = "/" + instantTest.group();
                    } else {
                        if (meta != null && !meta.group().isBlank()) {
                            subId = "/" + meta.group();
                        }
                    }
                    tests.add(new TestFunction(this.getBaseId() + subId, this.getBaseId() + '/' + NAME_CONVERSION.apply(method.getName()), EMPTY_STRUCTURE, Rotation.NONE, 1, 1, true, 1, 1, helper -> {
                        Machine machine = this.createMachine(helper);
                        if (instantTest.requiresEnergy())
                            machine.energyStorage().setEnergy(machine.energyStorage().getCapacity());
                        try {
                            Runnable runnable = (Runnable) method.invoke(this, machine);
                            if (runnable == null) {
                                helper.succeed();
                                return;
                            }

                            helper.runAfterDelay(1, () -> {
                                runnable.run();
                                helper.succeed();
                            });
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Failed to invoke test method!", e);
                        } catch (InvocationTargetException e) {
                            handleException(e);
                        }
                    }));
                }
            }
        }

        return tests;
    }

    public @NotNull TestFunction createChargeFromEnergyItemTest(@NotNull SlotGroupType slotType, Item infiniteBattery) {
        return new TestFunction(this.getBaseId(), this.getBaseId() + "/charge_from_item", EMPTY_STRUCTURE, Rotation.NONE, 2, 1, true, 1, 1, helper -> {
            Machine machine = this.createMachine(helper);
            for (ItemResourceSlot slot : machine.itemStorage().getGroup(slotType)) {
                slot.set(infiniteBattery, 1);
            }
            helper.runAfterDelay(1, () -> {
                if (machine.energyStorage().isEmpty()) {
                    helper.fail("Machine did not charge from the stack!", BlockPos.ZERO);
                }
                helper.succeed();
            });
        });
    }

    public @NotNull TestFunction createDrainToEnergyItemTest(@NotNull SlotGroupType slotType, Item battery) {
        return new TestFunction(this.getBaseId(), this.getBaseId() + "/drain_to_item", EMPTY_STRUCTURE, Rotation.NONE, 2, 1, true, 1, 1, helper -> {
            Machine machine = this.createMachine(helper);
            machine.energyStorage().setEnergy(machine.energyStorage().getCapacity());

            for (ItemResourceSlot slot : machine.itemStorage().getGroup(slotType)) {
                slot.set(battery, 1);
            }

            helper.runAfterDelay(1, () -> {
                if (machine.energyStorage().isFull()) {
                    helper.fail("Machine did not drain energy to the stack!", BlockPos.ZERO);
                }
                helper.succeed();
            });
        });
    }

    public static void handleException(@NotNull InvocationTargetException e) {
        if (e.getCause() instanceof GameTestAssertException ex) {
            throw ex;
        } else if (e.getCause() instanceof RuntimeException ex){
            throw ex;
        } else if (e.getCause() != null) {
            throw new RuntimeException(e.getCause());
        } else {
            throw new RuntimeException(e);
        }
    }

    public @NotNull String getBaseId() {
        return BuiltInRegistries.BLOCK.getKey(this.type.getBlock()).toString().replace(":", ".");
    }

    protected Machine createMachine(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, this.type.getBlock());
        return (Machine) helper.getBlockEntity(BlockPos.ZERO);
    }
}
