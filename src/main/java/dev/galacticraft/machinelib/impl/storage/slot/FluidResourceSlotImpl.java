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
import org.jetbrains.annotations.NotNull;

public class FluidResourceSlotImpl extends ResourceSlotImpl<Fluid> implements FluidResourceSlot {
    private final @NotNull TankDisplay display;

    public FluidResourceSlotImpl(@NotNull InputType inputType, @NotNull TankDisplay display, long capacity, ResourceFilter<Fluid> filter, @NotNull ResourceFilter<Fluid> externalFilter) {
        super(inputType, filter, externalFilter, capacity);
        this.display = display;
    }

    @Override
    public @NotNull TankDisplay getDisplay() {
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
        CompoundTag tag = new CompoundTag();
        if (this.isEmpty()) return tag;
        tag.putString(RESOURCE_KEY, BuiltInRegistries.FLUID.getKey(this.getResource()).toString());
        tag.putInt(AMOUNT_KEY, (int) this.getAmount());
        if (this.getTag() != null && !this.getTag().isEmpty()) tag.put(TAG_KEY, this.getTag());
        return tag;
    }

    @Override
    public void readTag(@NotNull CompoundTag tag) {
        if (tag.isEmpty()) {
            this.setEmpty();
        } else {
            this.set(BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString(RESOURCE_KEY))), tag.contains(TAG_KEY, Tag.TAG_COMPOUND) ? tag.getCompound(TAG_KEY) : null, tag.getInt(AMOUNT_KEY));
        }
    }

    @Override
    public void writePacket(@NotNull FriendlyByteBuf buf) {
        if (this.getAmount() > 0) {
            buf.writeInt((int) this.getAmount());
            buf.writeUtf(BuiltInRegistries.FLUID.getKey(this.getResource()).toString());
            buf.writeNbt(this.getTag());
        } else {
            buf.writeInt(0);
        }
    }

    @Override
    public void readPacket(@NotNull FriendlyByteBuf buf) {
        int amount = buf.readInt();
        if (amount == 0) {
            this.setEmpty();
        } else {
            Fluid resource = BuiltInRegistries.FLUID.get(new ResourceLocation(buf.readUtf()));
            CompoundTag tag = buf.readNbt();
            this.set(resource, tag, amount);
        }
    }
}
