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

package dev.galacticraft.machinelib.impl.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.screen.MachineScreenHandler;
import dev.galacticraft.machinelib.api.screen.StorageSyncHandler;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.exposed.ExposedSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.cache.ModCount;
import dev.galacticraft.machinelib.client.api.screen.Tank;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.fluid.FluidStack;
import dev.galacticraft.machinelib.impl.storage.slot.FluidSlot;
import dev.galacticraft.machinelib.impl.storage.slot.ResourceSlot;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

@ApiStatus.Internal
public final class MachineFluidStorageImpl implements MachineFluidStorage {
    private final int size;
    private final boolean @NotNull [] allowsGas;
    private final TankDisplay @NotNull [] displays;
    private final @NotNull FluidSlot @NotNull [] inventory;
    private final @NotNull SlotGroup @NotNull [] types;
    private final boolean @NotNull [] playerInsertion;

    private final @NotNull ModCount modCount = ModCount.root();

    public MachineFluidStorageImpl(int size, @NotNull SlotGroup @NotNull [] types, long[] capacities, @NotNull Predicate<FluidVariant> @NotNull [] filters, boolean @NotNull [] playerInsertion, boolean @NotNull [] allowsGas, @NotNull TankDisplay @NotNull [] displays) {
        this.size = size;
        this.allowsGas = allowsGas;
        this.displays = displays;
        this.inventory = new FluidSlot[this.size];
        this.playerInsertion = playerInsertion;
        this.types = types;

        for (int i = 0; i < this.size; i++) {
            this.inventory[i] = new FluidSlot(capacities[i], filters[i], this.modCount);
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public long getModCount() {
        return this.modCount.getModCount();
    }

    @Override
    public long getModCountUnsafe() {
        return this.modCount.getModCountUnsafe();
    }

    @Override
    public long getSlotModCount(int slot) {
        return this.getSlot(slot).getModCount();
    }

    @Override
    public long getSlotModCountUnsafe(int slot) {
        return this.getSlot(slot).getModCountUnsafe();
    }

    @Override
    public @NotNull ResourceSlot<Fluid, FluidVariant, FluidStack> getSlot(int slot) {
        return this.inventory[slot];
    }

    @Override
    public boolean isEmpty() {
        for (FluidSlot fluidSlot : this.inventory) {
            if (!fluidSlot.isResourceBlank()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canExposedExtract(int slot) {
        return this.types[slot].isAutomatable();
    }

    @Override
    public boolean canExposedInsert(int slot) {
        return this.canPlayerInsert(slot) && this.types[slot].isAutomatable();
    }

    @Override
    public boolean canPlayerInsert(int slot) {
        return this.playerInsertion[slot];
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return Iterators.forArray(this.inventory); // we do not need to iterate over the inner slots' iterator as there's only one slot.
    }

    @Override
    public @NotNull FluidStack extract(int slot, @NotNull TagKey<Fluid> tag, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);

        if (this.getSlot(slot).getResource().getFluid().builtInRegistryHolder().is(tag)) {
            return this.extract(slot, amount, context);
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public long insert(int slot, @NotNull FluidVariant variant, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notBlankNotNegative(variant, amount);
        if (!this.canAccept(slot, variant) || amount == 0) return 0;
        ResourceSlot<Fluid, FluidVariant, FluidStack> invSlot = this.getSlot(slot);
        if (invSlot.isResourceBlank()) {
            amount = Math.min(amount, invSlot.getCapacity(variant));
            try (Transaction transaction = Transaction.openNested(context)) {
                invSlot.setStack(variant, amount, transaction);
                transaction.commit();
                return amount;
            }
        } else if (variant.equals(invSlot.getResource())) {
            try (Transaction transaction = Transaction.openNested(context)) {
                long inserted = Math.min(amount, invSlot.getCapacity(variant) - invSlot.getAmount());
                if (inserted > 0) {
                    invSlot.setAmount(invSlot.getAmount() + inserted, transaction);
                    transaction.commit();
                    return inserted;
                }
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public boolean canAccess(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean canAccept(int slot, @NotNull FluidVariant variant) {
        if (!this.allowsGases(slot) && FluidVariantAttributes.isLighterThanAir(variant)) return false;
        return this.getSlot(slot).canAccept(variant);
    }

    @Override
    public Predicate<FluidVariant> getFilter(int slot) {
        return this.getSlot(slot).getFilter();
    }

    @Override
    public long count(@NotNull Fluid fluid) {
        long count = 0;
        for (FluidSlot fluidSlot : this.inventory) {
            if (fluidSlot.getResource().getFluid() == fluid) {
                count += fluidSlot.getAmount();
            }
        }
        return count;
    }

    @Override
    public long count(@NotNull FluidVariant fluid) {
        long count = 0;
        for (FluidSlot fluidSlot : this.inventory) {
            if (fluidSlot.getResource().equals(fluid)) {
                count += fluidSlot.getAmount();
            }
        }
        return count;
    }

    @Override
    public boolean containsAny(@NotNull Collection<Fluid> fluids) {
        for (FluidSlot fluidSlot : this.inventory) {
            if (fluids.contains(fluidSlot.getResource().getFluid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull Tag writeNbt() {
        ListTag list = new ListTag();
        for (FluidSlot fluidSlot : this.inventory) {
            CompoundTag compound = fluidSlot.getResource().toNbt();
            compound.putLong(Constant.Nbt.AMOUNT, fluidSlot.getAmount());
            list.add(compound);
        }
        return list;
    }

    @Override
    public void readNbt(@NotNull Tag nbt) {
        if (nbt instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                CompoundTag compound = list.getCompound(i);
                this.inventory[i].setStackUnsafe(FluidVariant.fromNbt(compound), compound.getLong(Constant.Nbt.AMOUNT), true);
            }
        }
    }

    @Override
    public void clearContent() {
        assert !Transaction.isOpen();
        for (FluidSlot fluidSlot : this.inventory) {
            fluidSlot.setStackUnsafe(FluidVariant.blank(), 0, true);
        }
    }

    @Override
    public void setSlotUnsafe(int slot, FluidVariant variant, long amount, boolean markDirty) {
        assert !Transaction.isOpen();
        this.inventory[slot].setStackUnsafe(variant, amount, markDirty);
    }

    @Override
    public long getCapacity(int slot) {
        return this.getSlot(slot).getCapacity();
    }

    @Override
    public @NotNull SlotGroup @NotNull [] getGroups() {
        return this.types;
    }

    @Override
    public @NotNull StorageSyncHandler createSyncHandler() {
        return new StorageSyncHandler() {
            private long modCount = -1;

            @Override
            public boolean needsSyncing() {
                return MachineFluidStorageImpl.this.getModCount() != this.modCount;
            }

            @Override
            public void sync(@NotNull FriendlyByteBuf buf) {
                this.modCount = MachineFluidStorageImpl.this.modCount.getModCount();
                for (FluidSlot slot : MachineFluidStorageImpl.this.inventory) {
                    slot.getResource().toPacket(buf);
                    buf.writeVarLong(slot.getAmount());
                }
            }

            @Override
            public void read(@NotNull FriendlyByteBuf buf) {
                for (FluidSlot slot : MachineFluidStorageImpl.this.inventory) {
                    slot.setStackUnsafe(FluidVariant.fromPacket(buf), buf.readVarLong(), false);
                }
            }
        };
    }

    @Override
    public boolean allowsGases(int slot) {
        return this.allowsGas[slot];
    }

    @Override
    public <M extends MachineBlockEntity> void addTanks(MachineScreenHandler<M> handler) {
        TankDisplay[] tankDisplays = this.displays;
        for (int i = 0; i < tankDisplays.length; i++) {
            TankDisplay tankDisplay = tankDisplays[i];
            handler.addTank(Tank.create(ExposedSlot.ofPlayerSlot(this, i, true), i, tankDisplay.x(), tankDisplay.y(), tankDisplay.height()));
        }
    }
}
