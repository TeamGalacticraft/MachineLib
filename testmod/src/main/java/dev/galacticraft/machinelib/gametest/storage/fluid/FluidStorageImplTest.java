/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.machinelib.gametest.storage.fluid;

import dev.galacticraft.api.machine.storage.MachineFluidStorage;
import dev.galacticraft.api.machine.storage.display.TankDisplay;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.impl.fluid.FluidStack;
import dev.galacticraft.impl.machine.Constant;
import dev.galacticraft.impl.machine.storage.MachineFluidStorageImpl;
import dev.galacticraft.machinelib.gametest.MachineLibGametest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class FluidStorageImplTest implements MachineLibGametest {
    private static final TankDisplay DISPLAY = new TankDisplay(0, 0, 0);
    private static final SlotType<Fluid, FluidVariant> TEST_SLOT_0 = SlotType.create(
            new Identifier(Constant.MOD_ID, "fluid_test_slot_0"),
            TextColor.fromRgb(0xFFFFFF),
            new TranslatableText("Slot 0"),
            v -> true,
            ResourceFlow.BOTH,
            ResourceType.FLUID
    );

    private static final SlotType<Fluid, FluidVariant> TEST_SLOT_1 = SlotType.create(
            new Identifier(Constant.MOD_ID, "fluid_test_slot_1"), TextColor.fromRgb(0xFFFFFFFE),
            new TranslatableText("Slot 1"),
            v -> v.getFluid() != Fluids.WATER,
            ResourceFlow.BOTH,
            ResourceType.FLUID
    );

    private MachineFluidStorageImpl storage;

    @Override
    public void beforeEach(TestContext context) {
        this.storage = (MachineFluidStorageImpl) MachineFluidStorage.Builder.create()
                .addTank(TEST_SLOT_0, 64, DISPLAY)
                .addTank(TEST_SLOT_1, 64, DISPLAY)
                .build();
    }

    @Override
    public void afterEach(TestContext context) {
        this.storage = null;
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void size(@NotNull TestContext context) {
        assertEquals(2, this.storage.size(), "Fluid Storage should have 2 slots!");
        assertEquals(1, MachineFluidStorage.Builder.create().addTank(TEST_SLOT_0, 1, DISPLAY).build().size(), "Fluid Storage should have 1 slot!");
        assertEquals(2, MachineFluidStorage.Builder.create().addTank(TEST_SLOT_0, 1, DISPLAY).addTank(TEST_SLOT_0, 1, DISPLAY).build().size(), "Fluid Storage should have 2 slots!");
        assertEquals(0, MachineFluidStorage.Builder.create().build().size(), "Fluid Storage should have 0 slots!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__generic(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(6, this.storage.insert(FluidVariant.of(Fluids.WATER), 6, transaction), "Expected 6 water to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__pass_to_next(@NotNull TestContext context) {
        typefillSlot(0);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(12, this.storage.insert(FluidVariant.of(Fluids.LAVA), 12, transaction), "Expected 12 yellow dye to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__filled(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 6);
        typefillSlot(1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(6, this.storage.insert(FluidVariant.of(Fluids.WATER), 6, transaction), "Expected 6 honeycombs to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__type_full_block(@NotNull TestContext context) {
        typefillSlot(0);
        typefillSlot(1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(FluidVariant.of(Fluids.WATER), 23, transaction), "Expected 0 melons to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__cap_full_block(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.LAVA), 64);
        this.storage.setSlot(1, FluidVariant.of(Fluids.LAVA), 64);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(FluidVariant.of(Fluids.LAVA), 27, transaction), "Expected 0 quartz to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__filter_block(@NotNull TestContext context) {
        typefillSlot(0);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(FluidVariant.of(Fluids.WATER), 14, transaction), "Expected 0 diamonds to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__overflow(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(65, this.storage.insert(FluidVariant.of(Fluids.LAVA), 65, transaction), "Expected 65 carrots to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__overflow_filled(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(7, this.storage.insert(FluidVariant.of(Fluids.LAVA), 7, transaction), "Expected 7 bone to be inserted!");
            assertEquals(65, this.storage.insert(FluidVariant.of(Fluids.LAVA), 65, transaction), "Expected 65 bone to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__overflow_type_full_block(@NotNull TestContext context) {
        this.storage.setSlot(1, FluidVariant.of(Fluids.LAVA), 1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(FluidVariant.of(Fluids.WATER), 65, transaction), "Expected 64 fluid frames to be inserted!");
            assertEquals(3, this.storage.insert(FluidVariant.of(Fluids.LAVA), 3, transaction), "Expected 3 beacons to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__overflow_cap_full_block(@NotNull TestContext context) {
        this.storage.setSlot(1, FluidVariant.of(Fluids.WATER), 64);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(FluidVariant.of(Fluids.WATER), 65, transaction), "Expected 64 grass to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__overflow_filter_block(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(FluidVariant.of(Fluids.WATER), 65, transaction), "Expected 64 diamonds to be inserted!");
            assertEquals(5, this.storage.insert(FluidVariant.of(Fluids.LAVA), 5, transaction), "Expected 5 amethyst shards to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__nbt(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(8, this.storage.insert(FluidVariant.of(Fluids.WATER, compound), 8, transaction), "Expected 8 diamonds with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__nbt_fill(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER, compound), 9);
        this.storage.setSlot(1, FluidVariant.of(Fluids.LAVA, compound), 21);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(2, this.storage.insert(FluidVariant.of(Fluids.WATER, compound), 2, transaction), "Expected 2 calcite with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__nbt_fill_block(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER, compound), 9);
        typefillSlot(1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(FluidVariant.of(Fluids.WATER), 4, transaction), "Expected 0 feathers to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert__nbt_fill_block_n(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 3);
        typefillSlot(1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(FluidVariant.of(Fluids.WATER, compound), 4, transaction), "Expected 0 warped fungi to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0, maxAttempts = 2)
    void insert__nbt_fill_block_d(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER, generateRandomNbt()), 3);
        typefillSlot(1);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(FluidVariant.of(Fluids.WATER, generateRandomNbt()), 4, transaction), "Expected 0 levers to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__generic(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(39, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 39, transaction), "Expected 39 carved pumpkins to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__filled(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(4, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 4, transaction), "Expected 4 oak buttons to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__full(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 64);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 4, transaction), "Expected 0 waxed cut copper to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__filled_block(@NotNull TestContext context) {
        typefillSlot(0);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 23, transaction), "Expected 0 fire charges to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__filter_block(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(1, FluidVariant.of(Fluids.WATER), 5, transaction), "Expected 0 diamonds to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__overflow(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(64, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 65, transaction), "Expected 64 chorus fruit to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__overflow_full(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(13, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 13, transaction), "Expected 13 targets to be inserted!");
            assertEquals(51, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 64, transaction), "Expected 51 targets to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__nbt(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(25, this.storage.insert(0, FluidVariant.of(Fluids.WATER, compound), 25, transaction), "Expected 25 firework rockets with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__nbt_fill(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER, compound), 22);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(4, this.storage.insert(0, FluidVariant.of(Fluids.WATER, compound), 4, transaction), "Expected 4 player heads with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__nbt_dif(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER, generateRandomNbt()), 22);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, FluidVariant.of(Fluids.WATER, generateRandomNbt()), 5, transaction), "Expected 0 oxeye daisies with nbt to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void insert_slot__nbt_fill_block(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER, compound), 9);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 7, transaction), "Expected 0 smooth quartz to be inserted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__generic(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 37);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(37, this.storage.extract(FluidVariant.of(Fluids.WATER), 37, transaction), "Expected 37 dead bushes to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__over(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 24);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(24, this.storage.extract(FluidVariant.of(Fluids.WATER), 25, transaction), "Expected 24 glass bottles to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__under(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 9);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(7, this.storage.extract(FluidVariant.of(Fluids.WATER), 7, transaction), "Expected 7 copper ingots to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__overflow(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 64);
        this.storage.setSlot(1, FluidVariant.of(Fluids.WATER), 9);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(73, this.storage.extract(FluidVariant.of(Fluids.WATER), 73, transaction), "Expected 73 copper ingots to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__overflow_over(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 64);
        this.storage.setSlot(1, FluidVariant.of(Fluids.WATER), 23);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(87, this.storage.extract(FluidVariant.of(Fluids.WATER), 88, transaction), "Expected 87 lapis lazuli to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__overflow_under(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 64);
        this.storage.setSlot(1, FluidVariant.of(Fluids.WATER), 9);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(65, this.storage.extract(FluidVariant.of(Fluids.WATER), 65, transaction), "Expected 65 blaze powder to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__nbt(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER, compound), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(10, this.storage.extract(FluidVariant.of(Fluids.WATER, compound), 10, transaction), "Expected 10 green dye to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__nbt_n_block(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER, compound), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(FluidVariant.of(Fluids.WATER), 42, transaction), "Expected 0 stripped acacia logs to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__nbt_d_block(@NotNull TestContext context) {
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER, generateRandomNbt()), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(FluidVariant.of(Fluids.WATER, generateRandomNbt()), 56, transaction), "Expected 0 beef to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__nbt_n_o_block(@NotNull TestContext context) {
        NbtCompound compound = generateRandomNbt();
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(0, this.storage.extract(FluidVariant.of(Fluids.WATER, compound), 13, transaction), "Expected 0 crafting tables to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void extract__nbt_d(@NotNull TestContext context) {
        NbtCompound nbt = generateRandomNbt();
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER, nbt), 13);

        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(13, this.storage.extract(FluidVariant.of(Fluids.WATER, nbt), 13, transaction), "Expected 13 pistons to be extracted!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void mod_count__initial(@NotNull TestContext context) {
        assertEquals(0, this.storage.getModCount(), "Fluid Storage should not be modified, as no transaction has occurred!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void mod_count__transaction_fail(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertThrowsExactly(IllegalStateException.class, () -> this.storage.getModCount(), "Expected ModCount access to fail during a transaction!");
        }
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void mod_count__transaction_cancel(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(32, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 32, transaction), "Fluids should have been inserted into slot 0!");
        }
        assertEquals(0, this.storage.getModCount(), "Fluid Storage should not be modified, as the transaction was aborted!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void mod_count__transaction_commit(@NotNull TestContext context) {
        try (Transaction transaction = Transaction.openOuter()) {
            assertEquals(44, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 44, transaction), "Fluids should have been inserted into slot 0!");
            transaction.commit();
        }
        assertEquals(1, this.storage.getModCount(), "Fluid Storage should have been modified, as the transaction was committed!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void empty__initial(@NotNull TestContext context) {
        assertTrue(this.storage.isEmpty(), "Fluid Storage should be empty!");
        assertEquals(2, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 2, null), "Fluids should have been inserted into slot 0!");
        assertFalse(this.storage.isEmpty(), "Fluid Storage should not be empty!");
        assertEquals(2, this.storage.extract(0, FluidVariant.of(Fluids.WATER), 2, null), "Fluids should have been extracted from slot 0!");
        assertTrue(this.storage.isEmpty(), "Fluid Storage should be empty!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void empty__inserted(@NotNull TestContext context) {
        assertEquals(2, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 2, null), "Fluids should have been inserted into slot 0!");
        assertFalse(this.storage.isEmpty(), "Fluid Storage should not be empty!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void empty__extracted(@NotNull TestContext context) {
        assertEquals(2, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 2, null), "Fluids should have been inserted into slot 0!");
        assertEquals(2, this.storage.extract(0, FluidVariant.of(Fluids.WATER), 2, null), "Fluids should have been extracted from slot 0!");
        assertTrue(this.storage.isEmpty(), "Fluid Storage should be empty!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void empty__extract_partial(@NotNull TestContext context) {
        assertEquals(6, this.storage.insert(0, FluidVariant.of(Fluids.WATER), 6, null), "Fluids should have been inserted into slot 0!");
        assertEquals(5, this.storage.extract(0, FluidVariant.of(Fluids.WATER), 5, null), "Fluids should have been extracted from slot 0!");
        assertFalse(this.storage.isEmpty(), "Fluid Storage should not be empty!");
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = "fluid_storage", tickLimit = 0)
    void getStack(@NotNull TestContext context) {
        // Should always return the EMPTY stack
        assertSame(FluidStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack in empty inventory!");
        this.storage.setSlot(0, FluidVariant.of(Fluids.WATER), 1);
        assertEquals(new FluidStack(FluidVariant.of(Fluids.WATER), 1), this.storage.getStack(0), "Expected 1 golden shovel in storage!");
        this.storage.setSlot(0, FluidVariant.blank(), 0);
        assertSame(FluidStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack after extraction!");
        this.storage.setSlot(0, FluidVariant.of(Fluids.LAVA), 4);
        assertEquals(new FluidStack(FluidVariant.of(Fluids.LAVA), 4), this.storage.getStack(0), "Expected 4 acacia saplings in storage!");
        this.storage.setSlot(0, FluidVariant.blank(), 0);
        assertSame(FluidStack.EMPTY, this.storage.getStack(0), "Expected identity empty stack after extraction!");
    }

    private static @NotNull NbtCompound generateRandomNbt() {
        NbtCompound compound = new NbtCompound();
        compound.putUuid("id", UUID.randomUUID());
        return compound;
    }
    
    private void typefillSlot(int index) {
        this.storage.setSlot(index, FluidVariant.of(Fluids.LAVA), 1);
    }
}