/*
 * Copyright (c) 2021-2023 Team Galacticraft
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
import dev.galacticraft.machinelib.api.storage.SlotProvider;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public abstract class AbstractSlotProvider<Resource, Slot extends ResourceSlot<Resource>> implements SlotProvider<Resource, Slot> {
    protected final @NotNull Slot @NotNull [] slots;

    public AbstractSlotProvider(@NotNull Slot @NotNull [] slots) {
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
    public boolean canInsert(@NotNull Resource resource) {
        for (Slot slot : this.slots) {
            if (slot.canInsert(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag) {
        for (Slot slot : this.slots) {
            if (slot.canInsert(resource, tag)) return true;
        }
        return false;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.tryInsert(resource, amount - inserted);
            if (inserted == amount) return true;
        }
        return inserted == amount;
    }

    @Override
    public boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.tryInsert(resource, tag, amount - inserted);
            if (inserted == amount) return true;
        }
        return inserted == amount;
    }

    @Override
    public long tryInsert(@NotNull Resource resource, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.tryInsert(resource, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long tryInsert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.tryInsert(resource, tag, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insert(@NotNull Resource resource, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, tag, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insertMatching(@NotNull Resource resource, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            if (slot.contains(resource)) {
                inserted += slot.insert(resource, amount - inserted);
                if (inserted == amount) return inserted;
            }
        }

        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long insertMatching(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            if (slot.contains(resource, tag)) {
                inserted += slot.insert(resource, tag, amount - inserted);
                if (inserted == amount) return inserted;
            }
        }

        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, tag, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public boolean containsAny(@NotNull Resource resource) {
        for (Slot slot : this.slots) {
            if (slot.contains(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean containsAny(@NotNull Resource resource, @Nullable CompoundTag tag) {
        for (Slot slot : this.slots) {
            if (slot.contains(resource, tag)) return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(@NotNull Resource resource, long amount) {
        for (Slot slot : this.slots) {
            if (slot.canExtract(resource, amount)) return true;
        }
        return false;
    }

    @Override
    public boolean canExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        for (Slot slot : this.slots) {
            if (slot.canExtract(resource, tag, amount)) return true;
        }
        return false;
    }

    @Override
    public long tryExtract(@NotNull Resource resource, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.tryExtract(resource, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public boolean extractOne(@NotNull Resource resource) {
        for (Slot slot : this.slots) {
            if (slot.extractOne(resource)) return true;
        }
        return false;
    }

    @Override
    public boolean extractOne(@NotNull Resource resource, @Nullable CompoundTag tag) {
        for (Slot slot : this.slots) {
            if (slot.extractOne(resource, tag)) return true;
        }
        return false;
    }

    @Override
    public long tryExtract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.tryExtract(resource, tag, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long extract(@NotNull Resource resource, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.extract(resource, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long extract(@NotNull Resource resource, @Nullable CompoundTag tag, long amount) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.extract(resource, tag, amount - extracted);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @Override
    public long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        long inserted = 0;
        for (Slot slot : this.slots) {
            inserted += slot.insert(resource, tag, amount - inserted);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        long extracted = 0;
        for (Slot slot : this.slots) {
            extracted += slot.extract(resource, tag, amount - extracted, context);
            if (extracted == amount) break;
        }
        return extracted;
    }

    @NotNull
    @Override
    public Iterator<Slot> iterator() {
        return Iterators.forArray(this.slots);
    }
}
