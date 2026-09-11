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

package dev.galacticraft.machinelib.impl.storage;

import com.google.common.collect.Iterators;
import dev.galacticraft.machinelib.api.storage.SlottedStorageAccess;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.component.DataComponentPatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Iterator;

public class BaseSlottedStorage<Resource, Slot extends ResourceSlot<Resource>> implements SlottedStorageAccess<Resource, Slot> {
    protected final @NotNull Slot @NotNull [] slots;

    public BaseSlottedStorage(@NotNull Slot @NotNull [] slots) {
        this.slots = slots;
    }

    @Override
    public boolean isEmpty() {
        for (Slot slot : this.slots) {
            if (!slot.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean isFull() {
        for (Slot slot : this.slots) {
            if (!slot.isFull()) return false;
        }
        return true;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @NotNull DataComponentPatch components) {
        for (Slot slot : this.slots) {
            if (slot.canInsert(resource, components)) return true;
        }
        return false;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.tryInsert(resource, components, amount - inserted);
            if (inserted == amount) return true;
        }
        return inserted == amount;
    }

    @Override
    public long tryInsert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.tryInsert(resource, components, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, components, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insertMatching(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            if (slot.contains(resource, components)) {
                inserted += slot.insert(resource, components, amount - inserted);
                if (inserted == amount) return inserted;
            }
        }

        return this.insert(resource, components, amount - inserted) + inserted;
    }

    @Override
    public boolean contains(@NotNull Resource resource) {
        for (Slot slot : this.slots) {
            if (slot.contains(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean contains(@NotNull Resource resource, @Nullable DataComponentPatch components) {
        for (Slot slot : this.slots) {
            if (slot.contains(resource, components)) return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        for (Slot slot : this.slots) {
            if (slot.canExtract(resource, components, amount)) return true;
        }
        return false;
    }

    @Override
    public boolean extractOne(@NotNull Resource resource, @Nullable DataComponentPatch components) {
        for (Slot slot : this.slots) {
            if (slot.extractOne(resource, components)) return true;
        }
        return false;
    }

    @Override
    public long tryExtract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.tryExtract(resource, components, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long extract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.extract(resource, components, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long insert(@NotNull Resource resource, @NotNull DataComponentPatch components, long amount, @Nullable TransactionContext context) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, components, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long extract(@NotNull Resource resource, @Nullable DataComponentPatch components, long amount, @Nullable TransactionContext context) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.extract(resource, components, amount - extracted, context);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public int size() {
        return this.slots.length;
    }

    @Override
    public SlottedStorageAccess<Resource, Slot> subStorage(int start, int len) {
        Slot[] slots1 = (Slot[]) Array.newInstance(this.slots.getClass().componentType(), len);
        System.arraycopy(this.slots, start, slots1, 0, len);
        return new BaseSlottedStorage<>(slots1);
    }

    @Override
    public SlottedStorageAccess<Resource, Slot> subStorage(int... slots) {
        Slot[] slots1 = (Slot[]) Array.newInstance(this.slots.getClass().componentType(), slots.length);
        for (int i = 0; i < slots.length; i++) {
            slots1[i] = this.slots[slots[i]];
        }
        return new BaseSlottedStorage<>(slots1);
    }

    @Override
    public Slot[] getSlots() {
        return this.slots;
    }

    @Override
    public @NotNull Slot slot(int slot) {
        return this.slots[slot];
    }

    @NotNull
    @Override
    public Iterator<Slot> iterator() {
        return Iterators.forArray(this.slots);
    }

    @Override
    public long getModifications() {
        long modifications = 0;
        for (Slot slot : this.slots) {
            modifications += slot.getModifications();
        }
        return modifications;
    }
}
