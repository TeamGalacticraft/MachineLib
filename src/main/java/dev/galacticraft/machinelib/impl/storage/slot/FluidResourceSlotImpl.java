/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.InputType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidResourceSlotImpl extends ResourceSlotImpl<Fluid> implements FluidResourceSlot {
    private final @Nullable TankDisplay display;

    public FluidResourceSlotImpl(@NotNull InputType inputType, @Nullable TankDisplay display, long capacity, ResourceFilter<Fluid> filter) {
        super(inputType, filter, capacity);
        this.display = display;
    }

    @Override
    public boolean isHidden() {
        return this.display == null;
    }

    @Override
    public @Nullable TankDisplay getDisplay() {
        return this.display;
    }

    @Override
    public long getRealCapacity() {
        return this.getCapacity();
    }

    @Override
    public long getCapacityFor(@NotNull Fluid fluid) {
        return this.getCapacity();
    }

    @Override
    public @NotNull CompoundTag createTag() {
        assert this.isSane();
        CompoundTag tag = new CompoundTag();
        if (this.isEmpty()) return tag;
        tag.putString(RESOURCE_KEY, BuiltInRegistries.FLUID.getKey(this.resource).toString());
        tag.putLong(AMOUNT_KEY, this.amount);
        if (this.tag != null && !this.tag.isEmpty()) tag.put(TAG_KEY, this.tag);
        return tag;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        if (tag.isEmpty()) {
            this.setEmpty();
        } else {
            this.set(BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString(RESOURCE_KEY))), tag.contains(TAG_KEY, Tag.TAG_COMPOUND) ? tag.getCompound(TAG_KEY) : null, tag.getLong(AMOUNT_KEY));
        }
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        assert this.isSane();
        if (this.amount > 0) {
            buf.writeLong(this.amount);
            buf.writeUtf(BuiltInRegistries.FLUID.getKey(this.resource).toString());
            buf.writeNbt(this.tag);
        } else {
            buf.writeLong(0);
        }
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        long amount = buf.readLong();
        if (amount == 0) {
            this.setEmpty();
        } else {
            Fluid resource = BuiltInRegistries.FLUID.get(new ResourceLocation(buf.readUtf()));
            CompoundTag tag = buf.readNbt();
            this.set(resource, tag, amount);
        }
    }

    @Override
    public boolean isSane() {
        return super.isSane() && this.resource != Fluids.EMPTY;
    }
}
