/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.menu;

import dev.galacticraft.machinelib.api.menu.Tank;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.api.util.StorageHelper;
import dev.galacticraft.machinelib.client.api.util.DisplayUtil;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Somewhat like a {@link net.minecraft.world.inventory.Slot} but for fluids and gases.
 * Resources can be inserted into the tank and extracted from it via the gui.
 */
public final class TankImpl implements Tank {
    public final ResourceSlot<Fluid> slot;
    private final TransferType transferType;
    private final int index;
    private final int x;
    private final int y;
    private final int height;
    private final int width;
    private final boolean marked;
    public int id = -1;

    public TankImpl(ResourceSlot<Fluid> slot, TransferType transferType, int index, int x, int y, int width, int height, boolean marked) {
        this.slot = slot;
        this.transferType = transferType;
        this.index = index;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.marked = marked;
    }

    @Override
    public @Nullable Fluid getFluid() {
        return this.slot.getResource();
    }

    @Override
    public @NotNull DataComponentPatch getComponents() {
        return this.slot.getComponents();
    }

    @Override
    public long getAmount() {
        return this.slot.getAmount();
    }

    @Override
    public long getCapacity() {
        return this.slot.getCapacity();
    }

    @Override
    public boolean isEmpty() {
        return this.slot.isEmpty();
    }

    @Override
    public boolean isMarked() {
        return this.marked;
    }

    @Override
    public FluidVariant createVariant() {
        if (this.getFluid() == null) {
            return FluidVariant.blank();
        } else {
            return FluidVariant.of(this.getFluid(), this.getComponents());
        }
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> list = new ArrayList<>();
        DisplayUtil.createFluidTooltip(list, this.getFluid(), this.getComponents(), this.getAmount(), this.getCapacity());
        return list;
    }

    @Override
    public boolean acceptStack(@NotNull ContainerItemContext context) {
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        if (storage != null) {
            if (storage.supportsExtraction() && this.transferType.playerInsertion()) {
                FluidVariant storedResource;
                if (this.isEmpty()) {
                    storedResource = StorageUtil.findStoredResource(storage, variant -> this.slot.getFilter().test(variant.getFluid(), variant.getComponents()));
                } else {
                    storedResource = this.createVariant();
                }
                if (storedResource != null && !storedResource.isBlank()) {
                    return StorageHelper.move(storedResource, storage, this.slot, Long.MAX_VALUE, null) != 0;
                }
            } else if (storage.supportsInsertion() && this.transferType.playerExtraction()) {
                FluidVariant storedResource = this.createVariant();
                if (!storedResource.isBlank()) {
                    return StorageHelper.move(storedResource, this.slot, storage, Long.MAX_VALUE, null) != 0;
                }
            }
        }
        return false;
    }

    @Override
    public ResourceSlot<Fluid> getSlot() {
        return this.slot;
    }

    @Override
    public TransferType getInputType() {
        return this.transferType;
    }
}
