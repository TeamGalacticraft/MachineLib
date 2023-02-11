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
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.gametest.Assertions;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public final class FluidSlotGroupBuilderTest extends GameUnitTest<SlotGroup.Builder<Fluid, FluidStack, FluidResourceSlot>> {
    public static final Supplier<FluidResourceSlot> SLOT_SUPPLIER = FluidResourceSlot.builder().capacity(1)::build;

    public FluidSlotGroupBuilderTest() {
        super("fluid_slot_group_builder", SlotGroup::fluid);
    }

    @Override
    @GameTestGenerator
    public @NotNull List<TestFunction> generateTests() {
        return super.generateTests();
    }

    @UnitTest
    public void empty(SlotGroup.Builder<Fluid, FluidStack, FluidResourceSlot> builder) {
        Assertions.assertThrows(builder::build);
    }

    @UnitTest
    public void single(SlotGroup.Builder<Fluid, FluidStack, FluidResourceSlot> builder) {
        SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = builder.add(SLOT_SUPPLIER).build();
        Assertions.assertEquals(1, group.size());
    }

    @UnitTest
    public void multiple(SlotGroup.Builder<Fluid, FluidStack, FluidResourceSlot> builder) {
        SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = builder.add(SLOT_SUPPLIER).add(SLOT_SUPPLIER).build();
        Assertions.assertEquals(2, group.size());
    }

    @UnitTest
    public void reuse(SlotGroup.Builder<Fluid, FluidStack, FluidResourceSlot> builder) {
        SlotGroup<Fluid, FluidStack, FluidResourceSlot> group = builder.add(SLOT_SUPPLIER).add(SLOT_SUPPLIER).build();
        Assertions.assertIdentityNotEquals(group, builder.build());
        Assertions.assertEquals(group.size(), builder.build().size());

        Assertions.assertIdentityNotEquals(group.getSlot(0), builder.build().getSlot(0));
        Assertions.assertIdentityNotEquals(group.getSlot(1), builder.build().getSlot(1));
    }
}
