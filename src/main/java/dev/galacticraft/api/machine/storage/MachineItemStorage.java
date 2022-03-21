/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.api.machine.storage;

import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.machine.storage.display.ItemSlotDisplay;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.impl.machine.storage.MachineItemStorageImpl;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface MachineItemStorage extends ResourceStorage<Item, ItemVariant, ItemStack> {
    <M extends MachineBlockEntity> void addSlots(MachineScreenHandler<M> handler);

    Inventory playerInventory();

    class Builder {
        private int size = 0;
        private final List<SlotType<Item, ItemVariant>> types = new ArrayList<>();
        private final List<ItemSlotDisplay> displays = new ArrayList<>();
        private final LongList counts = new LongArrayList();

        public Builder() {}

        @Contract(value = " -> new", pure = true)
        public static @NotNull Builder create() {
            return new Builder();
        }

        public @NotNull Builder addSlot(SlotType<Item, ItemVariant> type, @NotNull ItemSlotDisplay display) {
            return this.addSlot(type, 64, display);
        }

        public @NotNull Builder addSlot(SlotType<Item, ItemVariant> type, int maxCount, @NotNull ItemSlotDisplay display) {
            maxCount = Math.min(maxCount, 64);
            this.size++;
            this.types.add(type);
            this.displays.add(display);
            this.counts.add(maxCount);
            return this;
        }

        @Contract(pure = true, value = " -> new")
        public @NotNull MachineItemStorageImpl build() {
            return new MachineItemStorageImpl(this.size, this.types.toArray(new SlotType[0]), this.counts.toLongArray(), this.displays.toArray(new ItemSlotDisplay[0]));
        }
    }
}
