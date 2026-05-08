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

package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.filter.ResourceFilter;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Default fluid slot implementation.
 */
public class FluidResourceSlotImpl extends ResourceSlotImpl<Fluid> implements FluidResourceSlot {

    private final @Nullable TankDisplay display;
    private final @Nullable ResourceLocation id;
    private final @NotNull Set<ResourceLocation> groups;

    /**
     * Creates a fluid slot with no logical target metadata.
     *
     * @param transferType transfer mode
     * @param display optional display data
     * @param capacity tank capacity
     * @param filter fluid filter
     */
    public FluidResourceSlotImpl(
            final @NotNull TransferType transferType,
            final @Nullable TankDisplay display,
            final long capacity,
            final ResourceFilter<Fluid> filter
    ) {
        this(
                transferType,
                display,
                capacity,
                filter,
                null,
                Set.of()
        );
    }

    /**
     * Creates a fluid slot with logical target metadata.
     *
     * @param transferType transfer mode
     * @param display optional display data
     * @param capacity tank capacity
     * @param filter fluid filter
     * @param id optional exact tank id
     * @param groups logical tank groups
     */
    public FluidResourceSlotImpl(
            final @NotNull TransferType transferType,
            final @Nullable TankDisplay display,
            final long capacity,
            final ResourceFilter<Fluid> filter,
            final @Nullable ResourceLocation id,
            final @NotNull Set<ResourceLocation> groups
    ) {
        super(
                transferType,
                filter,
                capacity
        );

        this.display = display;
        this.id = id;
        this.groups = Set.copyOf(groups);
    }

    @Override
    public @Nullable ResourceLocation id() {
        return this.id;
    }

    @Override
    public @NotNull Set<ResourceLocation> groups() {
        return this.groups;
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
    public long getCapacityFor(
            final @NotNull Fluid fluid,
            final @NotNull DataComponentPatch components
    ) {
        return this.getCapacity();
    }

    @Override
    public @NotNull CompoundTag createTag() {
        assert this.isSane();

        final CompoundTag tag = new CompoundTag();

        if (this.isEmpty()) {
            return tag;
        }

        tag.putString(
                RESOURCE_KEY,
                BuiltInRegistries.FLUID.getKey(this.resource).toString()
        );
        tag.putLong(
                AMOUNT_KEY,
                this.amount
        );

        if (!this.components.isEmpty()) {
            tag.put(
                    COMPONENTS_KEY,
                    DataComponentPatch.CODEC.encode(
                            this.components,
                            NbtOps.INSTANCE,
                            new CompoundTag()
                    ).getOrThrow()
            );
        }

        return tag;
    }

    @Override
    public void readTag(final @NotNull CompoundTag tag) {
        if (tag.isEmpty()) {
            this.setEmpty();
            return;
        }

        this.set(
                BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString(RESOURCE_KEY))),
                tag.contains(COMPONENTS_KEY, Tag.TAG_COMPOUND)
                        ? DataComponentPatch.CODEC.decode(
                        NbtOps.INSTANCE,
                        tag.getCompound(COMPONENTS_KEY)
                ).getOrThrow().getFirst()
                        : DataComponentPatch.EMPTY,
                tag.getLong(AMOUNT_KEY)
        );
    }

    @Override
    public void writePacket(final @NotNull RegistryFriendlyByteBuf buf) {
        assert this.isSane();

        if (this.amount > 0) {
            buf.writeLong(this.amount);
            buf.writeUtf(BuiltInRegistries.FLUID.getKey(this.resource).toString());
            DataComponentPatch.STREAM_CODEC.encode(
                    buf,
                    this.components
            );
        } else {
            buf.writeLong(0);
        }
    }

    @Override
    public void readPacket(final @NotNull RegistryFriendlyByteBuf buf) {
        final long amount = buf.readLong();

        if (amount == 0) {
            this.setEmpty();
            return;
        }

        final Fluid resource = BuiltInRegistries.FLUID.get(ResourceLocation.parse(buf.readUtf()));
        final DataComponentPatch components = DataComponentPatch.STREAM_CODEC.decode(buf);

        this.set(
                resource,
                components,
                amount
        );
    }

    @Override
    public boolean isSane() {
        return super.isSane() && this.resource != Fluids.EMPTY;
    }
}