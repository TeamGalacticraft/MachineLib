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

package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.compat.transfer.ExposedStorage;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortTarget;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedItemSlotImpl;
import dev.galacticraft.machinelib.impl.compat.transfer.ExposedStorageImpl;
import dev.galacticraft.machinelib.impl.util.Utils;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Default item storage implementation.
 */
public class MachineItemStorageImpl extends ResourceStorageImpl<Item, ItemResourceSlot> implements MachineItemStorage {

    public static final MachineItemStorageImpl EMPTY = new MachineItemStorageImpl(new ItemResourceSlot[0]);

    private final ExposedStorage<Item, ItemVariant>[] exposedStorages = new ExposedStorage[3];

    /**
     * Creates item storage from concrete slots.
     *
     * @param slots item slots
     */
    public MachineItemStorageImpl(final @NotNull ItemResourceSlot @NotNull [] slots) {
        super(slots);

        for (int i = 0; i < 3; i++) {
            this.exposedStorages[i] = this.createExposedStorage(ResourceFlow.values()[i]);
        }
    }

    @Override
    public int getContainerSize() {
        return this.size();
    }

    @Override
    public @NotNull ItemStack getItem(final int i) {
        return ItemStackUtil.create(this.slot(i));
    }

    @Override
    public @NotNull ItemStack removeItem(
            final int slot,
            final int amount
    ) {
        Utils.breakpointMe("attempted to remove item from vanilla compat container!");
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(final int i) {
        Utils.breakpointMe("attempted to remove item from vanilla compat container!");
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(
            final int i,
            final ItemStack itemStack
    ) {
        Utils.breakpointMe("attempted to modify item from vanilla compat container!");
    }

    @Override
    public void setChanged() {
        Utils.breakpointMe("attempted to mark vanilla compat container as modified!");
    }

    @Override
    public boolean stillValid(final Player player) {
        Utils.breakpointMe("testing player validity of vanilla compat container");
        return false;
    }

    @Override
    public boolean canPlaceItem(
            final int i,
            final ItemStack itemStack
    ) {
        return false;
    }

    @Override
    public boolean canTakeItem(
            final Container container,
            final int i,
            final ItemStack itemStack
    ) {
        return false;
    }

    @Override
    public void clearContent() {
        Utils.breakpointMe("attempted to clear items in a vanilla compat container!");
    }

    /**
     * Creates exposed storage for the given flow using all slots.
     *
     * @param flow resource flow
     * @return exposed storage, or {@code null} if no slot supports the flow
     */
    protected @Nullable ExposedStorage<Item, ItemVariant> createExposedStorage(final @NotNull ResourceFlow flow) {
        final ExposedItemSlotImpl[] slots = new ExposedItemSlotImpl[this.size()];
        boolean support = false;

        for (int i = 0; i < slots.length; i++) {
            slots[i] = new ExposedItemSlotImpl(
                    this.slot(i),
                    flow
            );

            support |= slots[i].supportsInsertion() || slots[i].supportsExtraction();
        }

        return support ? new ExposedStorageImpl<>(
                this,
                slots
        ) : null;
    }

    @Override
    public @Nullable ExposedStorage<Item, ItemVariant> getExposedStorage(final @NotNull ResourceFlow flow) {
        return this.exposedStorages[flow.ordinal()];
    }

    @Override
    public @Nullable ExposedStorage<Item, ItemVariant> getExposedStorage(
            final @NotNull ResourceFlow flow,
            final @NotNull MultiblockPortTarget target
    ) {
        final List<ExposedItemSlotImpl> exposedSlots = new ArrayList<>();

        for (final ItemResourceSlot slot : this.getSlots()) {
            if (!matchesTarget(
                    slot,
                    target
            )) {
                continue;
            }

            final ExposedItemSlotImpl exposedSlot = new ExposedItemSlotImpl(
                    slot,
                    flow
            );

            if (exposedSlot.supportsInsertion() || exposedSlot.supportsExtraction()) {
                exposedSlots.add(exposedSlot);
            }
        }

        if (exposedSlots.isEmpty()) {
            return null;
        }

        return new ExposedStorageImpl<>(
                this,
                exposedSlots.toArray(ExposedItemSlotImpl[]::new)
        );
    }

    /**
     * Checks whether an item slot matches a port target.
     *
     * @param slot item slot
     * @param target port target
     * @return {@code true} if the target matches
     */
    private static boolean matchesTarget(
            final ItemResourceSlot slot,
            final MultiblockPortTarget target
    ) {
        if (target.group()) {
            return slot.groups().contains(target.id());
        }

        return target.id().equals(slot.id());
    }

    @Override
    public boolean consumeOne(final @NotNull Item resource) {
        for (final ItemResourceSlot slot : this.slots) {
            if (slot.consumeOne(resource)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean consumeOne(
            final @NotNull Item resource,
            final @Nullable DataComponentPatch components
    ) {
        for (final ItemResourceSlot slot : this.slots) {
            if (slot.consumeOne(
                    resource,
                    components
            )) {
                return true;
            }
        }

        return false;
    }

    @Override
    public long consume(
            final @NotNull Item resource,
            final long amount
    ) {
        long consumed = 0;

        for (final ItemResourceSlot slot : this.slots) {
            consumed += slot.consume(
                    resource,
                    amount - consumed
            );

            if (consumed == amount) {
                break;
            }
        }

        return consumed;
    }

    @Override
    public long consume(
            final @NotNull Item resource,
            final @Nullable DataComponentPatch components,
            final long amount
    ) {
        long consumed = 0;

        for (final ItemResourceSlot slot : this.slots) {
            consumed += slot.consume(
                    resource,
                    components,
                    amount - consumed
            );

            if (consumed == amount) {
                break;
            }
        }

        return consumed;
    }
}