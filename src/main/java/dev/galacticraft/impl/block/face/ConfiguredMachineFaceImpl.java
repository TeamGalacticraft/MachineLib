/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.impl.block.face;

import com.mojang.datafixers.util.Either;
import dev.galacticraft.api.block.face.ConfiguredMachineFace;
import dev.galacticraft.api.machine.storage.MachineEnergyStorage;
import dev.galacticraft.api.machine.storage.ResourceStorage;
import dev.galacticraft.api.machine.storage.io.*;
import dev.galacticraft.impl.MLConstant;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.Objects;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class ConfiguredMachineFaceImpl implements ConfiguredMachineFace {
    /**
     * The type of resource that this face is configured to accept.
     */
    private @NotNull ResourceType<?, ?> type;
    /**
     * The flow direction of this face.
     */
    private @NotNull ResourceFlow flow;
    /**
     * The filter of this face.
     * <p>
     * When it is null, the face is not filtering.
     * When it is an Integer, the face is locked to a single slot/
     * When it is a {@link SlotType}, the face is locked to all slots of that type.
     */
    private @Nullable Either<Integer, SlotType<?, ?>> matching = null;

    /**
     * The cached exposed storage of this face.
     */
    private @Nullable Object storage = null;

    public ConfiguredMachineFaceImpl(@NotNull ResourceType<?, ?> type, @NotNull ResourceFlow flow) {
        this.type = type;
        this.flow = flow;
    }

    @Override
    public void setOption(@NotNull ResourceType<?, ?> type, @NotNull ResourceFlow flow) {
        this.type = type;
        this.flow = flow;
        this.matching = null;
        this.storage = null;
    }

    @Override
    public void setMatching(@Nullable Either<Integer, SlotType<?, ?>> matching) {
        assert matching == null || matching.right().isEmpty() || this.type.willAcceptResource(matching.right().get().getType());
        this.matching = matching;
        this.storage = null;
    }

    @Override
    public @NotNull ResourceType<?, ?> getType() {
        return type;
    }

    @Override
    public @NotNull ResourceFlow getFlow() {
        return flow;
    }

    @Override
    public @Nullable Either<Integer, SlotType<?, ?>> getMatching() {
        return matching;
    }

    @Override
    public <T, V extends TransferVariant<T>> @Nullable ExposedStorage<T, V> getExposedStorage(@NotNull ResourceStorage<T, V, ?> storage) {
        if (this.getType().willAcceptResource(storage.getResource())) {
            if (this.storage == null) this.storage = storage.getExposedStorage(this.matching, this.flow);
            return (ExposedStorage<T, V>) this.storage;
        }
        return null;
    }

    @Override
    public EnergyStorage getExposedStorage(@NotNull MachineEnergyStorage storage) {
        if (this.getType().willAcceptResource(ResourceType.ENERGY) && this.matching == null) {
            if (this.storage == null) this.storage = storage.getExposedStorage(this.flow);
            return (EnergyStorage) this.storage;
        }
        return null;
    }

    @Override
    public <T, V extends TransferVariant<T>> int @NotNull [] getMatching(@Nullable ConfiguredStorage<T, V> storage) {
        if (storage == null) return new int[0];
        if (matching != null) {
            if (matching.left().isPresent()) {
                return new int[]{matching.left().get()};
            } else {
                IntList types = new IntArrayList();
                SlotType<T, V>[] slots = storage.getTypes();
                //noinspection OptionalGetWithoutIsPresent - we know that right is present because left is not present
                SlotType<?, ?> type = matching.right().get();
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i].equals(type)) {
                        types.add(i);
                    }
                }
                return types.toIntArray();
            }
        } else {
            IntList types = new IntArrayList();
            SlotType<T, V>[] slots = storage.getTypes();
            for (int i = 0; i < slots.length; i++) {
                SlotType<T, V> slot = slots[i];
                if (slot.getType().willAcceptResource(this.type)) {
                    if (slot.getFlow().canFlowIn(this.flow)) {
                        types.add(i);
                    }
                }
            }
            return types.toIntArray();
        }
    }

    @Override
    public @NotNull CompoundTag writeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putByte(MLConstant.Nbt.FLOW, (byte) this.flow.ordinal());
        nbt.putByte(MLConstant.Nbt.RESOURCE, this.type.getOrdinal());
        nbt.putBoolean(MLConstant.Nbt.MATCH, this.matching != null);
        if (this.matching != null) {
            nbt.putBoolean(MLConstant.Nbt.IS_SLOT_ID, this.matching.left().isPresent());
            if (this.matching.left().isPresent()) {
                nbt.putInt(MLConstant.Nbt.VALUE, this.matching.left().get());
            } else {
                nbt.putString(MLConstant.Nbt.VALUE, Objects.requireNonNull(SlotType.REGISTRY.getKey(this.matching.right().orElseThrow(RuntimeException::new))).toString());
            }
        }
        return nbt;
    }

    @Override
    public void readNbt(@NotNull CompoundTag nbt) {
        this.type = ResourceType.getFromOrdinal(nbt.getByte(MLConstant.Nbt.RESOURCE));
        this.flow = ResourceFlow.values()[nbt.getByte(MLConstant.Nbt.FLOW)];
        if (nbt.getBoolean(MLConstant.Nbt.MATCH)) {
            if (nbt.getBoolean(MLConstant.Nbt.IS_SLOT_ID)) {
                this.matching = Either.left(nbt.getInt(MLConstant.Nbt.VALUE));
            } else {
                this.matching = Either.right(SlotType.REGISTRY.get(new ResourceLocation(nbt.getString(MLConstant.Nbt.VALUE))));
            }
        } else {
            this.matching = null;
        }
    }
}
