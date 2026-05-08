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

package dev.galacticraft.machinelib.api.multiblock.components;

import dev.galacticraft.machinelib.api.multiblock.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

/**
 * Persistent storage component for a formed multiblock machine.
 *
 * <p>This component owns MachineLib item, fluid, and energy storage for a
 * formed multiblock. It is the multiblock equivalent of the storage fields held
 * by {@code MachineBlockEntity}, but it is not attached to a block entity.</p>
 *
 * <p>This component only stores and persists resources. Exposing resources to
 * pipes, hoppers, cables, or port blocks should be handled by a later port
 * system, because that requires mapping multiblock faces and part positions to
 * world-side storage providers.</p>
 */
public final class MultiblockStorageComponent implements MultiblockComponent {

    private static final String ITEM_STORAGE = "ItemStorage";
    private static final String FLUID_STORAGE = "FluidStorage";
    private static final String ENERGY_STORAGE = "EnergyStorage";

    private final MachineItemStorage itemStorage;
    private final MachineFluidStorage fluidStorage;
    private final MachineEnergyStorage energyStorage;

    private MultiblockComponentContext context;

    /**
     * Creates a storage component from a storage specification.
     *
     * @param spec storage specification
     */
    public MultiblockStorageComponent(final StorageSpec spec) {
        this.itemStorage = spec.createItemStorage();
        this.fluidStorage = spec.createFluidStorage();
        this.energyStorage = spec.createEnergyStorage();
    }

    /**
     * Gets the item storage owned by this multiblock.
     *
     * @return item storage
     */
    public MachineItemStorage itemStorage() {
        return this.itemStorage;
    }

    /**
     * Gets the fluid storage owned by this multiblock.
     *
     * @return fluid storage
     */
    public MachineFluidStorage fluidStorage() {
        return this.fluidStorage;
    }

    /**
     * Gets the energy storage owned by this multiblock.
     *
     * @return energy storage
     */
    public MachineEnergyStorage energyStorage() {
        return this.energyStorage;
    }

    /**
     * Marks this storage component as changed.
     *
     * <p>Call this after direct storage mutations until the multiblock menu and
     * port systems can automatically mark storage dirty.</p>
     */
    public void setChanged() {
        if (this.context != null) {
            this.context.setChanged();
        }
    }

    /**
     * Loads stored resources from persistent component NBT.
     *
     * @param context component context
     * @param tag saved component tag
     */
    @Override
    public void load(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        this.context = context;

        if (tag.contains(ITEM_STORAGE, Tag.TAG_LIST)) {
            this.itemStorage.readTag(tag.getList(
                    ITEM_STORAGE,
                    Tag.TAG_COMPOUND
            ));
        }

        if (tag.contains(FLUID_STORAGE, Tag.TAG_LIST)) {
            this.fluidStorage.readTag(tag.getList(
                    FLUID_STORAGE,
                    Tag.TAG_COMPOUND
            ));
        }

        if (tag.contains(ENERGY_STORAGE, Tag.TAG_LONG)) {
            this.energyStorage.readTag((LongTag) tag.get(ENERGY_STORAGE));
        }
    }

    /**
     * Saves stored resources to persistent component NBT.
     *
     * @param context component context
     * @param tag component tag to write into
     */
    @Override
    public void save(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        if (this.itemStorage.size() > 0) {
            tag.put(
                    ITEM_STORAGE,
                    this.itemStorage.createTag()
            );
        }

        if (this.fluidStorage.size() > 0) {
            tag.put(
                    FLUID_STORAGE,
                    this.fluidStorage.createTag()
            );
        }

        if (this.energyStorage.getCapacity() > 0) {
            tag.put(
                    ENERGY_STORAGE,
                    this.energyStorage.createTag()
            );
        }
    }

    /**
     * Stores the active component context.
     *
     * @param context component context
     */
    @Override
    public void onFormed(final MultiblockComponentContext context) {
        this.context = context;
    }

}