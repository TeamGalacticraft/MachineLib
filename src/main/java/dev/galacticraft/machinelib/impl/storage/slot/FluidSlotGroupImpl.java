package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

public class FluidSlotGroupImpl<Slot extends ResourceSlot<Fluid, FluidStack>> extends SlotGroupImpl<Fluid, FluidStack, Slot> {
    public FluidSlotGroupImpl(@NotNull Slot @NotNull [] slots) {
        super(slots);
    }

    @Override
    public boolean canInsertStack(@NotNull FluidStack stack) {
        if (stack.isEmpty()) return true;
        assert stack.getFluid() != null && stack.getFluid() != Fluids.EMPTY && stack.getAmount() > 0;
        long inserted = 0;
        for (Slot slot : this) {
            inserted += slot.tryInsert(stack.getFluid(), stack.getTag(), stack.getAmount() - inserted);
            if (stack.getAmount() == inserted) return true;
        }
        return stack.getAmount() == inserted;
    }

    @Override
    public long tryInsertStack(@NotNull FluidStack stack) {
        if (stack.isEmpty()) return 0;
        assert stack.getFluid() != null && stack.getFluid() != Fluids.EMPTY && stack.getAmount() > 0;
        long inserted = 0;
        for (Slot slot : this) {
            inserted += slot.tryInsert(stack.getFluid(), stack.getTag(), stack.getAmount() - inserted);
            if (stack.getAmount() == inserted) break;
        }
        return inserted;
    }

    @Override
    public long insertStack(@NotNull FluidStack stack) {
        if (stack.isEmpty()) return 0;
        assert stack.getFluid() != null && stack.getFluid() != Fluids.EMPTY && stack.getAmount() > 0;
        long inserted = 0;
        for (Slot slot : this) {
            inserted += slot.insert(stack.getFluid(), stack.getTag(), stack.getAmount() - inserted);
            if (stack.getAmount() == inserted) break;
        }
        return inserted;
    }
}
