
package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.slot.FluidTank;
import dev.galacticraft.machinelib.api.storage.slot.MachineFluidTank;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.FluidTankDisplay;
import dev.galacticraft.machinelib.api.world.level.fluid.FluidStack;
import dev.galacticraft.machinelib.impl.storage.slot.MachineFluidTankImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

import java.util.Objects;

public class MachineFluidStorageImpl implements MachineFluidStorage {
    private final int size;
    private final MachineFluidTank[] slots;
    private final SlotGroup[] groups;
    private final ResourceFilter<Fluid>[] filters;
    private final FluidTankDisplay[] displays;
    private final boolean[] playerInsertion;
    private long modCount = 0;

    public MachineFluidStorageImpl(int size, SlotGroup[] groups, long[] capacity, ResourceFilter<Fluid>[] filters, FluidTankDisplay[] displays, boolean[] playerInsertion) {
        assert filters.length == size;
        this.size = size;
        this.slots = new MachineFluidTank[size];
        this.groups = groups;
        this.filters = filters;
        this.displays = displays;
        this.playerInsertion = playerInsertion;

        for (int i = 0; i < this.slots.length; i++) {
            this.slots[i] = new MachineFluidTankImpl(this, capacity[i]);
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public MachineFluidTank getTank(int slot) {
        return this.slots[slot];
    }

    @Override
    public SlotGroup getGroup(int slot) {
        return this.groups[slot];
    }

    @Override
    public ResourceFilter<Fluid> getFilter(int slot) {
        return this.filters[slot];
    }

    @Override
    public boolean canPlayerInsert(int slot) {
        return this.playerInsertion[slot];
    }

    @Override
    public boolean canExternalInsert(int slot) {
        return this.playerInsertion[slot] && this.groups[slot].isAutomatable();
    }

    @Override
    public boolean canExternalExtract(int slot) {
        return !this.playerInsertion[slot] && this.groups[slot].isAutomatable();
    }

    @Override
    public long insert(Fluid fluid, long amount) {
        long initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.filters[i].matches(fluid, null)) {
                    amount -= this.insert(i, fluid, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public long insert(Fluid fluid, CompoundTag tag, long amount) {
        long initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.filters[i].matches(fluid, tag)) {
                    amount -= this.insert(i, fluid, tag, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public long insertMerge(Fluid fluid, long amount) {
        long initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.getFluid(i) == fluid) {
                    amount -= this.insert(i, fluid, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return this.insert(fluid, amount);
    }

    @Override
    public long insertMerge(Fluid fluid, CompoundTag tag, long amount) {
        long initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                FluidStack stack = this.getStack(i);
                if (stack.getFluid() == fluid && Objects.equals(stack.getTag(), tag)) {
                    amount -= this.insert(i, fluid, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return this.insert(fluid, tag, amount);
    }

    @Override
    public long simulateInsert(Fluid fluid, long amount) {
        long initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.filters[i].matches(fluid, null)) {
                    amount -= this.simulateInsert(i, fluid, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public long simulateInsert(Fluid fluid, CompoundTag tag, long amount) {
        long initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.filters[i].matches(fluid, tag)) {
                    amount -= this.simulateInsert(i, fluid, tag, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public long simulateInsertMerge(Fluid fluid, long amount) {
        long initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.getFluid(i) == fluid) {
                    amount -= this.simulateInsert(i, fluid, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.getFluid(i) != fluid && this.filters[i].matches(fluid, null)) {
                    amount -= this.simulateInsert(i, fluid, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public long simulateInsertMerge(Fluid fluid, CompoundTag tag, long amount) {
        long initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                FluidStack stack = this.getStack(i);
                if (stack.getFluid() == fluid && Objects.equals(stack.getTag(), tag)) {
                    amount -= this.simulateInsert(i, fluid, amount);
                    if (amount == 0) return initial;
                }
            }
        }

        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                FluidStack stack = this.getStack(i);
                if ((stack.getFluid() != fluid || !Objects.equals(stack.getTag(), tag)) && this.filters[i].matches(fluid, tag)) {
                    amount -= this.simulateInsert(i, fluid, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public boolean extract(Fluid fluid) {
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                if (this.extract(i, fluid)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean extractExact(Fluid fluid, long amount) {
        long initial = amount;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.simulateExtract(i, fluid, amount);
                if (amount == 0) break;
            }
        }
        if (amount != 0) return false;

        amount = initial;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.extract(i, fluid, amount);
                if (amount == 0) return true;
            }
        }
        return false;
    }

    @Override
    public long extract(Fluid fluid, long amount) {
        long initial = amount;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.extract(i, fluid, amount);
                if (amount == 0) return initial;
            }
        }
        return initial - amount;
    }

    @Override
    public long extract(Fluid fluid, CompoundTag tag, long amount) {
        long initial = amount;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.extract(i, fluid, tag, amount);
                if (amount == 0) return initial;
            }
        }
        return initial - amount;
    }

    @Override
    public boolean simulateExtract(Fluid fluid) {
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                if (this.simulateExtract(i, fluid)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean simulateExtractExact(Fluid fluid, long amount) {
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.simulateExtract(i, fluid, amount);
                if (amount == 0) return true;
            }
        }
        return false;
    }

    @Override
    public long simulateExtract(Fluid fluid, long amount) {
        long initial = amount;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.simulateExtract(i, fluid, amount);
                if (amount == 0) return initial;
            }
        }
        return initial - amount;
    }

    @Override
    public long simulateExtract(Fluid fluid, CompoundTag tag, long amount) {
        long initial = amount;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.simulateExtract(i, fluid, tag, amount);
                if (amount == 0) return initial;
            }
        }
        return initial - amount;
    }

    @Override
    public boolean isEmpty() {
        for (FluidTank slot : this.slots) {
            if (!slot.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public long getModCount() {
        return this.modCount;
    }

    @Override
    public void markDirty() {
        this.modCount++;
    }

    @Override
    public void setModCount(long value) {
        this.modCount = value;
    }
}
