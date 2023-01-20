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

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.storage.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.Utils;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// assertions made:
// if AMOUNT > 0 then RESOURCE is NOT NULL (and the inverse - if RESOURCE is NOT NULL then AMOUNT > 0)
// the associated TAG will either be NULL or contain a value - it will never be EMPTY
// EVERY aborted transaction will unwind - if it skips then MODIFICATIONS will be off
public abstract class ResourceSlotImpl<Resource, Stack> extends SnapshotParticipant<ResourceSlotImpl.Snapshot<Resource>> implements ResourceSlot<Resource, Stack> {
    protected static final String RESOURCE_KEY = "Resource";
    protected static final String AMOUNT_KEY = "Amount";
    protected static final String TAG_KEY = "Tag";

    private final ResourceFilter<Resource> filter;
    private final ResourceFilter<Resource> externalFilter;
    private final long capacity;
    private SlotGroup<Resource, Stack, ? extends ResourceSlot<Resource, Stack>> group;
    private @Nullable Resource resource = null;
    private @Nullable CompoundTag tag = null;
    private long amount = 0;
    private long modifications = 0;

    protected ResourceSlotImpl(ResourceFilter<Resource> filter, ResourceFilter<Resource> externalFilter, long capacity) {
        this.filter = filter;
        this.externalFilter = externalFilter;
        this.capacity = capacity;
    }

    @Contract("null -> null")
    private static @Nullable CompoundTag stripTag(@Nullable CompoundTag tag) {
        return tag == null ? null : (tag.isEmpty() ? null : tag);
    }

    @Override
    public @NotNull ResourceFilter<Resource> getFilter() {
        return this.filter;
    }

    @Override
    public @NotNull ResourceFilter<Resource> getExternalFilter() {
        return this.externalFilter;
    }

    @Override
    public @NotNull SlotGroup<Resource, Stack, ? extends ResourceSlot<Resource, Stack>> getGroup() {
        if (this.group == null) throw new AssertionError();
        return this.group;
    }

    @Override
    public void _setGroup(SlotGroup<Resource, Stack, ? extends ResourceSlot<Resource, Stack>> group) {
        assert group != null;
        this.group = group;
    }

    @Override
    public @Nullable Resource getResource() {
        assert this.isSane();
        return this.resource;
    }

    protected void setResource(@Nullable Resource resource) {
        this.resource = resource;
    }

    @Override
    public long getAmount() {
        assert this.isSane();
        return this.amount;
    }

    protected void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public @Nullable CompoundTag getTag() {
        assert this.isSane();
        return this.tag;
    }

    protected void setTag(@Nullable CompoundTag tag) {
        this.tag = tag;
    }

    @Override
    public @Nullable CompoundTag copyTag() {
        assert this.isSane();
        return this.tag == null ? null : this.tag.copy();
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }

    @Override
    public boolean isEmpty() {
        assert this.isSane();
        return this.amount == 0;
    }

    @Override
    public boolean isFull() {
        assert this.isSane();
        return this.amount == this.getRealCapacity();
    }

    @Override
    public boolean insertOne(@NotNull Resource resource, @Nullable TransactionContext context) {
        assert this.isSane();

        if (this.amount < this.capacity && this.canInsert(resource)) {
            this.updateSnapshots(context);
            this.resource = resource;
            this.amount++;
            return true;
        }
        return false;
    }

    @Override
    public boolean insertOne(@NotNull Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context) {
        assert this.isSane();

        if (this.amount < this.capacity && this.canInsert(resource, tag)) {
            this.updateSnapshots(context);
            this.resource = resource;
            this.tag = stripTag(tag);
            this.amount++;
            return true;
        }
        return false;
    }

    @Override
    public long insert(@NotNull Resource resource, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);
        assert this.isSane();

        if (this.canInsert(resource)) {
            long previous = this.amount;
            long total = Math.min(this.getRealCapacity(), this.amount + amount);
            if (total != this.amount) {
                this.updateSnapshots(context);
                this.resource = resource;
                this.amount = total;
                return previous - amount;
            }
        }
        return 0;
    }

    @Override
    public long insert(@NotNull Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        StoragePreconditions.notNegative(amount);
        assert this.isSane();

        if (this.canInsert(resource, tag)) {
            long previous = this.amount;
            long total = Math.min(this.getRealCapacity(), this.amount + amount);
            if (total != this.amount) {
                this.updateSnapshots(context);
                this.resource = resource;
                this.tag = stripTag(tag);
                this.amount = total;
                return previous - amount;
            }
        }
        return 0;
    }

    @Override
    public boolean extractOne(@Nullable TransactionContext context) {
        assert this.isSane();

        if (this.resource != null) {
            this.updateSnapshots(context);
            if (--this.amount == 0) {
                this.resource = null;
                this.tag = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean extractOne(@Nullable Resource resource, @Nullable TransactionContext context) {
        assert this.isSane();

        if (this.canExtract(resource)) {
            this.updateSnapshots(context);
            if (--this.amount == 0) {
                this.resource = null;
                this.tag = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean extractOne(@Nullable Resource resource, @Nullable CompoundTag tag, @Nullable TransactionContext context) {
        assert this.isSane();

        if (this.canExtract(resource, tag)) {
            this.updateSnapshots(context);
            if (--this.amount == 0) {
                this.resource = null;
                this.tag = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public long extract(long amount, @Nullable TransactionContext context) {
        if (amount == 0) return 0;
        StoragePreconditions.notNegative(amount);
        assert this.isSane();

        if (this.resource != null) {
            this.updateSnapshots(context);
            long extracted = Math.min(this.amount, amount);
            this.amount -= extracted;
            if (this.amount == 0) {
                this.resource = null;
                this.tag = null;
            }
            return extracted;
        }
        return 0;
    }

    @Override
    public long extract(@Nullable Resource resource, long amount, @Nullable TransactionContext context) {
        if (amount == 0) return 0;
        StoragePreconditions.notNegative(amount);
        assert this.isSane();

        if (this.canExtract(resource)) {
            this.updateSnapshots(context);
            long extracted = Math.min(this.amount, amount);
            this.amount -= extracted;
            if (this.amount == 0) {
                this.resource = null;
                this.tag = null;
            }
            return extracted;
        }
        return 0;
    }

    @Override
    public long extract(@Nullable Resource resource, @Nullable CompoundTag tag, long amount, @Nullable TransactionContext context) {
        if (amount == 0) return 0;
        StoragePreconditions.notNegative(amount);
        assert this.isSane();

        if (this.canExtract(resource, tag)) {
            this.updateSnapshots(context);
            long extracted = Math.min(this.amount, amount);
            this.amount -= extracted;
            if (this.amount == 0) {
                this.resource = null;
                this.tag = null;
            }
            return extracted;
        }
        return 0;
    }

    @Override
    public boolean contains(@NotNull Resource resource) {
        return this.resource == resource;
    }

    @Override
    public boolean contains(@NotNull Resource resource, @Nullable CompoundTag tag) {
        return this.resource == resource && Utils.tagsEqual(this.tag, tag);
    }

    @Override
    public long getModifications() {
        return this.modifications;
    }

    @Override
    public void revertModification() {
        this.modifications--;
    }

    @Override
    public void markModified() {
        this.modifications++;
    }

    @Override
    protected Snapshot<Resource> createSnapshot() {
        this.modifications++;
        return new Snapshot<>(this.resource, this.amount, this.tag);
    }

    @Override
    protected void readSnapshot(Snapshot<Resource> snapshot) {
        this.modifications--;
        this.resource = snapshot.resource;
        this.amount = snapshot.amount;
        this.tag = snapshot.tag;
    }

    @Override
    public void updateSnapshots(TransactionContext transaction) {
        if (transaction == null) {
            this.modifications++;
        } else {
            super.updateSnapshots(transaction);
        }
    }

    protected void setEmpty() {
        this.resource = null;
        this.tag = null;
        this.amount = 0;
    }

    public void set(@Nullable Resource resource, @Nullable CompoundTag tag, long amount) {
        this.resource = resource;
        this.tag = tag;
        this.amount = amount;
        assert this.isSane();
    }

    @Contract(pure = true)
    private boolean canInsert(@NotNull Resource resource) {
        return this.resource == resource || (this.resource == null && this.filter.test(resource, null));
    }

    @Contract(pure = true)
    private boolean canInsert(@NotNull Resource resource, @Nullable CompoundTag tag) {
        return (this.resource == resource && Utils.tagsEqual(this.tag, tag)) || (this.resource == null && this.filter.test(resource, tag));
    }

    @Contract(pure = true)
    private boolean canExtract(@Nullable Resource resource) {
        return this.resource != null && (this.resource == resource || resource == null);
    }

    @Contract(pure = true)
    private boolean canExtract(@Nullable Resource resource, @Nullable CompoundTag tag) {
        return this.resource != null && (this.resource == resource || resource == null) && Utils.tagsEqual(this.tag, tag);
    }

    private boolean isSane() {
        return (this.resource == null && this.tag == null && this.amount == 0) || (this.resource != null && this.amount > 0/* && this.amount < this.getCapacity()*/ && (this.tag == null || !this.tag.isEmpty()));
    }

    protected record Snapshot<Resource>(@Nullable Resource resource, long amount, @Nullable CompoundTag tag) {
    }
}
