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

package dev.galacticraft.impl.block;

import com.mojang.datafixers.util.Either;
import dev.galacticraft.api.block.ConfiguredMachineFace;
import dev.galacticraft.api.machine.storage.MachineEnergyStorage;
import dev.galacticraft.api.machine.storage.ResourceStorage;
import dev.galacticraft.api.machine.storage.io.*;
import dev.galacticraft.impl.machine.Constant;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class ConfiguredMachineFaceImpl implements ConfiguredMachineFace {
    /**
     * The type of resource that this face is configured to accept.
     */
    private ResourceType<?, ?> type;
    /**
     * The flow direction of this face.
     */
    private ResourceFlow flow;
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

    public ConfiguredMachineFaceImpl(ResourceType<?, ?> type, ResourceFlow flow) {
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
        assert matching.right().isEmpty() || this.type.willAcceptResource(matching.right().get().getType());
        this.matching = matching;
        this.storage = null;
    }

    @Override
    public ResourceType<?, ?> getType() {
        return type;
    }

    @Override
    public ResourceFlow getFlow() {
        return flow;
    }

    @Override
    public @Nullable Either<Integer, SlotType<?, ?>> getMatching() {
        return matching;
    }

    @Override
    public <T, V extends TransferVariant<T>> ExposedStorage<T, V> getExposedStorage(@NotNull ResourceStorage<T, V, ?> storage) {
        if (this.getType().willAcceptResource(storage.getResource())) {
            if (this.storage == null) this.storage = storage.getExposedStorage(this.matching, this.flow);
            return (ExposedStorage<T, V>) this.storage;
        }
        return storage.view();
    }

    @Override
    public EnergyStorage getExposedStorage(@NotNull MachineEnergyStorage storage) {
        if (this.getType().willAcceptResource(ResourceType.ENERGY) && this.matching == null) {
            if (this.storage == null) this.storage = storage.getExposedStorage(this.flow);
            return (EnergyStorage) this.storage;
        }
        return storage.view();
    }

    @Override
    public <T, V extends TransferVariant<T>> int[] getMatching(ConfiguredStorage<T, V> storage) {
        if (matching != null) {
            if (matching.left().isPresent()) {
                return new int[]{matching.left().get()};
            } else {
                IntList types = new IntArrayList();
                SlotType<T, V>[] slots = storage.getTypes();
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
    public NbtCompound writeNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putByte(Constant.Nbt.FLOW, (byte) this.flow.ordinal());
        nbt.putByte(Constant.Nbt.RESOURCE, this.type.getOrdinal());
        nbt.putBoolean(Constant.Nbt.MATCH, this.matching != null);
        if (this.matching != null) {
            nbt.putBoolean(Constant.Nbt.IS_SLOT_ID, this.matching.left().isPresent());
            if (this.matching.left().isPresent()) {
                nbt.putInt(Constant.Nbt.VALUE, this.matching.left().get());
            } else {
                nbt.putString(Constant.Nbt.VALUE, this.matching.right().orElseThrow(RuntimeException::new).getReference().registryKey().getValue().toString());
            }
        }
        return nbt;
    }

    @Override
    public void readNbt(@NotNull NbtCompound nbt) {
        this.type = ResourceType.getFromOrdinal(nbt.getByte(Constant.Nbt.RESOURCE));
        this.flow = ResourceFlow.values()[nbt.getByte(Constant.Nbt.FLOW)];
        if (nbt.getBoolean(Constant.Nbt.MATCH)) {
            if (nbt.getBoolean(Constant.Nbt.IS_SLOT_ID)) {
                this.matching = Either.left(nbt.getInt(Constant.Nbt.VALUE));
            } else {
                this.matching = Either.right(SlotType.REGISTRY.get(new Identifier(nbt.getString(Constant.Nbt.VALUE))));
            }
        } else {
            this.matching = null;
        }
    }
}
