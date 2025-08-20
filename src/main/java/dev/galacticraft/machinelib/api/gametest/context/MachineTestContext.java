/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.api.gametest.context;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.UUID;

/**
 * Wrapper around {@link GameTestHelper} with additional methods for testing machines.
 */
public class MachineTestContext<BE extends MachineBlockEntity> extends AssertionContext {
    public final BE be;

    public MachineTestContext(GameTestHelper helper, BlockPos pos) {
        super(helper, pos);
        this.be = helper.getBlockEntity(pos);
    }

    public void setDisabled() {
        if (be.getState().isPowered()) {
            this.be.setRedstoneMode(RedstoneMode.LOW);
        } else {
            this.be.setRedstoneMode(RedstoneMode.HIGH);
        }
    }

    public void emptyCapacitor() {
        this.setEnergy(0);
    }

    public void fillCapacitor() {
        this.setEnergy(Integer.MAX_VALUE);
    }

    public void setEnergy(long energy) {
        this.be.energyStorage().setEnergy(energy);
    }

    public void clearItem(int slot) {
        this.setItem(slot, null, 0L);
    }

    public void setItem(int slot, Item item) {
        this.setItem(slot, item, 1L);
    }

    public void setItem(int slot, Item item, long amount) {
        this.setItem(slot, item, DataComponentPatch.EMPTY, amount);
    }

    public void setItem(int slot, Item item, DataComponentPatch components, long amount) {
        ItemResourceSlot slot1 = this.be.itemStorage().slot(slot);
        slot1.set(item, components, amount);
        slot1.markModified();
    }

    public void fillItem(int slot, Item item) {
        this.setItem(slot, item, this.be.itemStorage().slot(slot).getCapacity());
    }

    public void blockItemSlot(int slot) {
        CompoundTag compound = new CompoundTag();
        compound.putUUID("Random", UUID.randomUUID());
        this.be.itemStorage().slot(slot).set(Items.BARRIER, DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(compound)).build(), this.be.itemStorage().slot(slot).getCapacity());
    }

    public void clearFluid(int slot) {
        this.setFluid(slot, null, 0L);
    }

    public void setFluid(int slot, Fluid fluid) {
        this.setFluid(slot, fluid, 1L);
    }

    public void setFluid(int slot, Fluid fluid, long amount) {
        this.setFluid(slot, fluid, DataComponentPatch.EMPTY, amount);
    }

    public void setFluid(int slot, Fluid fluid, DataComponentPatch components, long amount) {
        FluidResourceSlot slot1 = this.be.fluidStorage().slot(slot);
        slot1.set(fluid, components, amount);
        slot1.markModified();
    }

    public void fillFluid(int slot, Fluid fluid) {
        this.setFluid(slot, fluid, this.be.fluidStorage().slot(slot).getCapacity());
    }

    public void blockFluidTank(int slot) {
        CompoundTag compound = new CompoundTag();
        compound.putUUID("Random", UUID.randomUUID()); //todo: is this sufficient?
        this.be.fluidStorage().slot(slot).set(Fluids.WATER, DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(compound)).build(), this.be.fluidStorage().slot(slot).getCapacity());
    }

    // assertions

    public void assertCapacitorEmpty() {
        this.assertTrue(this.be.energyStorage().isEmpty(), "Expected capacitor to be empty, but it contains {}gJ", be.energyStorage().getAmount());
    }

    public void assertCapacitorFull() {
        this.assertTrue(this.be.energyStorage().isFull(), "Expected capacitor to be full ({} / {})", be.energyStorage().getAmount(), be.energyStorage().getCapacity());
    }

    public void assertEnergy(long amount) {
        this._assertEquals(amount, be.energyStorage().getAmount(), r -> f("Expected energy to be equal to {}, was {}", amount, r));
    }

    public void assertEnergyCapacity(long capacity) {
        this._assertEquals(capacity, be.energyStorage().getCapacity(), r -> f("Expected energy capacity to be {}, was {}", capacity, r));
    }

    public void assertSlotEmpty(int slot) {
        this.assertTrue(be.itemStorage().slot(slot).isEmpty(), f("Expected slot {} to be empty but found {}x {}", slot, be.itemStorage().slot(slot).getAmount(), be.itemStorage().slot(slot).getResource()));
    }

    public void assertSlotNotEmpty(int slot) {
        this.assertFalse(be.itemStorage().slot(slot).isEmpty(), f("Expected slot {} to not be empty", slot));
    }

    public void assertSlotFull(int slot) {
        this.assertTrue(be.itemStorage().slot(slot).isFull(), f("Expected slot {} to be full but it can still fit {} items", slot, be.itemStorage().slot(slot).getCapacity() - be.itemStorage().slot(slot).getAmount()));
    }

    public void assertSlotNotFull(int slot) {
        this.assertFalse(be.itemStorage().slot(slot).isFull(), f("Expected slot {} to be not full of {}", slot, be.itemStorage().slot(slot).getResource()));
    }

    public void assertSlotContains(int slot, Item item) {
        this._assertEquals(item, be.itemStorage().slot(slot).getResource(), r -> f("Expected {} in slot {} but found {}", item, slot, r));
    }

    public void assertSlotContains(int slot, Item item, long amount) {
        this._assertEquals(item, be.itemStorage().slot(slot).getResource(), r -> f("Expected {} in slot {} but found {}", item, slot, r));
        this._assertEquals(amount, be.itemStorage().slot(slot).getAmount(), r -> f("Expected {}x {} in slot {} but found {}", amount, item, slot, r));
    }

    public void assertSlotContains(int slot, long amount) {
        this._assertEquals(amount, be.itemStorage().slot(slot).getAmount(), r -> f("Expected {} items in slot {} but found {}x", amount, slot, r));
    }

    public void assertTankEmpty(int tank) {
        this.assertTrue(be.fluidStorage().slot(tank).isEmpty(), f("Expected tank {} to be empty but found {} droplets of {}", tank, be.fluidStorage().slot(tank).getAmount(), be.fluidStorage().slot(tank).getResource()));
    }

    public void assertTankNotEmpty(int tank) {
        this.assertFalse(be.fluidStorage().slot(tank).isEmpty(), f("Expected tank {} to not be empty", tank));
    }

    public void assertTankFull(int tank) {
        this.assertTrue(be.fluidStorage().slot(tank).isFull(), f("Expected tank {} to be full but it can still fit {} items", tank, be.fluidStorage().slot(tank).getCapacity() - be.fluidStorage().slot(tank).getAmount()));
    }

    public void assertTankNotFull(int tank) {
        this.assertFalse(be.fluidStorage().slot(tank).isFull(), f("Expected tank {} to not be full of {}", tank, be.fluidStorage().slot(tank).getResource()));
    }

    public void assertTankContains(int tank, Fluid fluid) {
        this._assertEquals(fluid, be.fluidStorage().slot(tank).getResource(), r -> f("Expected {} in tank {} but found {}", fluid, tank, r));
    }

    public void assertTankContains(int tank, Fluid fluid, long amount) {
        this._assertEquals(fluid, be.fluidStorage().slot(tank).getResource(), r -> f("Expected {} in tank {} but found {}", fluid, tank, r));
        this._assertEquals(amount, be.fluidStorage().slot(tank).getAmount(), r -> f("Expected {} droplets of {} in tank {} but found {}", amount, fluid, tank, r));
    }

    public void assertTankContains(int tank, long amount) {
        this._assertEquals(amount, be.fluidStorage().slot(tank).getAmount(), r -> f("Expected {} droplets in tank {} but found {}", amount, tank, r));
    }

    public void assertActive() {
        this.assertTrue(be.isActive(), "Expected machine to be active");
    }

    public void assertInactive() {
        this.assertFalse(be.isActive(), "Expected machine to be inactive");
    }

    public void assertStatus(MachineStatus status) {
        this._assertEquals(status, be.getState().getStatus(), r -> f("Expected machine to be {} but it was {}", status, r));
    }
}
