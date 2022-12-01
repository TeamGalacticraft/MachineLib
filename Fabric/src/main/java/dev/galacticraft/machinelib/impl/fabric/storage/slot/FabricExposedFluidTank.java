package dev.galacticraft.machinelib.impl.fabric.storage.slot;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.storage.slot.MachineFluidTank;
import dev.galacticraft.machinelib.api.world.level.fluid.FluidStack;
import dev.galacticraft.machinelib.impl.fabric.storage.FabricExposedFluidStorage;
import dev.galacticraft.machinelib.impl.storage.ResourceFilter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.level.material.Fluid;

import java.util.Iterator;

public class FabricExposedFluidTank extends SnapshotParticipant<FabricExposedFluidTank.TankSnapshot> implements Storage<FluidVariant>, StorageView<FluidVariant> {
    private final FabricExposedFluidStorage storage;
    private final MachineFluidTank tank;
    private final ResourceFilter<Fluid> filter;
    private final boolean insertion;
    private final boolean extraction;

    public FabricExposedFluidTank(FabricExposedFluidStorage storage, MachineFluidTank tank, ResourceFilter<Fluid> filter, boolean insertion, boolean extraction) {
        this.storage = storage;
        this.tank = tank;
        this.filter = filter;
        this.insertion = insertion;
        this.extraction = extraction;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (this.insertion && this.filter.matches(resource.getFluid(), resource.getNbt())) {
            this.createSnapshot();
            return this.tank.insertCopyNbt(resource.getFluid(), resource.getNbt(), maxAmount);
        }
        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (this.extraction) {
            this.createSnapshot();
            return this.tank.extract(resource.getFluid(), resource.getNbt(), maxAmount);
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return this.tank.isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        FluidStack stack = this.tank.getStack();
        if (stack.isEmpty()) return FluidVariant.blank();
        assert stack.getFluid() != null;
        return FluidVariant.of(stack.getFluid(), stack.getTag());
    }

    @Override
    public long getAmount() {
        return this.tank.getAmount();
    }

    @Override
    public boolean supportsInsertion() {
        return this.insertion;
    }

    @Override
    public boolean supportsExtraction() {
        return this.extraction;
    }

    @Override
    public long getVersion() {
        return this.tank.getModCount();
    }

    @Override
    public long getCapacity() {
        return this.tank.getCapacity();
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return Iterators.singletonIterator(this);
    }

    @Override
    protected TankSnapshot createSnapshot() {
        TankSnapshot tankSnapshot = new TankSnapshot(this.tank.getStack(), this.tank.getModCount());
        this.tank.silentSetStack(this.tank.copyStack()); // set stack to a copy
        this.storage.createSnapshot();
        return tankSnapshot;
    }

    @Override
    protected void readSnapshot(TankSnapshot snapshot) {
        this.tank.silentSetStack(snapshot.stack);
        this.tank.setModCount(snapshot.modCount);
    }

    public record TankSnapshot(FluidStack stack, long modCount) {}
}
