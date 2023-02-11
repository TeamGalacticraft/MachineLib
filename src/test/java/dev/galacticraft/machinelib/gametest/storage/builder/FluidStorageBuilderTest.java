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

package dev.galacticraft.machinelib.gametest.storage.builder;

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.gametest.GameUnitTest;
import dev.galacticraft.machinelib.api.gametest.annotation.UnitTest;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.testmod.slot.TestModSlotGroupTypes;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static dev.galacticraft.machinelib.gametest.Assertions.*;

public final class FluidStorageBuilderTest extends GameUnitTest<MachineFluidStorage.Builder> {
    public static final Supplier<FluidResourceSlot> SLOT_SUPPLIER = FluidResourceSlot.builder().capacity(1)::build;
    public static final Supplier<SlotGroup<Fluid, FluidStack, FluidResourceSlot>> GROUP_SUPPLIER = SlotGroup.fluid().add(SLOT_SUPPLIER)::build;

    public FluidStorageBuilderTest() {
        super("fluid_storage_builder", MachineFluidStorage::builder);
    }

    @Override
    @GameTestGenerator
    public @NotNull List<TestFunction> generateTests() {
        return super.generateTests();
    }

    @UnitTest
    public void empty() {
        assertEquals(0, MachineFluidStorage.empty().groups());
    }

    @UnitTest
    public void buildEmpty(MachineFluidStorage.@NotNull Builder builder) {
        MachineFluidStorage storage = builder.build();
        assertIdentityEquals(MachineFluidStorage.empty(), storage);
    }

    @UnitTest
    public void singleSlot(MachineFluidStorage.@NotNull Builder builder) {
        MachineFluidStorage storage = builder.single(TestModSlotGroupTypes.CHARGE, SLOT_SUPPLIER).build();
        assertEquals(1, storage.groups());
        assertEquals(1, storage.getSlots().length);
    }

    @UnitTest
    public void multiSingleSlot(MachineFluidStorage.@NotNull Builder builder) {
        MachineFluidStorage storage = builder.single(TestModSlotGroupTypes.CHARGE, SLOT_SUPPLIER).single(TestModSlotGroupTypes.DIRT, SLOT_SUPPLIER).build();
        assertEquals(2, storage.groups());
        assertEquals(2, storage.getSlots().length);
    }

    @UnitTest
    public void singleSlotGroup(MachineFluidStorage.@NotNull Builder builder) {
        MachineFluidStorage storage = builder.group(TestModSlotGroupTypes.CHARGE, GROUP_SUPPLIER).build();
        assertEquals(1, storage.groups());
        assertEquals(1, storage.getSlots().length);
    }

    @UnitTest
    public void multiSlotGroup(MachineFluidStorage.@NotNull Builder builder) {
        MachineFluidStorage storage = builder.group(TestModSlotGroupTypes.CHARGE, SlotGroup.fluid()
                .add(SLOT_SUPPLIER).add(SLOT_SUPPLIER)::build).build();
        assertEquals(1, storage.groups());
        assertEquals(2, storage.getSlots().length);
    }

    @UnitTest
    public void multiSingleSlotGroup(MachineFluidStorage.@NotNull Builder builder) {
        MachineFluidStorage storage = builder.group(TestModSlotGroupTypes.CHARGE, GROUP_SUPPLIER)
                .group(TestModSlotGroupTypes.SOLID_FUEL, GROUP_SUPPLIER).build();
        assertEquals(2, storage.groups());
        assertEquals(2, storage.getSlots().length);
    }

    @UnitTest
    public void multiMultiSlotGroup(MachineFluidStorage.@NotNull Builder builder) {
        MachineFluidStorage storage = builder.group(TestModSlotGroupTypes.CHARGE, SlotGroup.fluid()
                .add(SLOT_SUPPLIER).add(SLOT_SUPPLIER)::build).group(TestModSlotGroupTypes.DIAMONDS, SlotGroup.fluid()
                .add(SLOT_SUPPLIER).add(SLOT_SUPPLIER)::build).build();
        assertEquals(2, storage.groups());
        assertEquals(4, storage.getSlots().length);
    }

    @UnitTest
    public void failDuplicateType(MachineFluidStorage.@NotNull Builder builder) {
        assertThrows(() -> builder.group(TestModSlotGroupTypes.CHARGE, GROUP_SUPPLIER)
                .group(TestModSlotGroupTypes.CHARGE, GROUP_SUPPLIER).build());

        assertThrows(() -> builder.single(TestModSlotGroupTypes.DIAMONDS, SLOT_SUPPLIER)
                .single(TestModSlotGroupTypes.DIAMONDS, SLOT_SUPPLIER).build());

        assertThrows(() -> builder.group(TestModSlotGroupTypes.SOLID_FUEL, GROUP_SUPPLIER)
                .single(TestModSlotGroupTypes.SOLID_FUEL, SLOT_SUPPLIER).build());
    }
}
