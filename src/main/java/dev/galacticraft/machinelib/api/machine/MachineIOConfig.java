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

package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.api.block.face.BlockFace;
import dev.galacticraft.machinelib.api.block.face.MachineIOFaceConfig;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.machine.MachineIOConfigImpl;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the configuration of a machine's I/O for all six faces.
 */
public interface MachineIOConfig {
    /**
     * Constructs a new machine i/o configuration.
     * @see MachineIOConfigImpl the default implementation
     * @return a new {@link MachineIOConfig}
     */
    @Contract(" -> new")
    static @NotNull MachineIOConfig create() {
        return new MachineIOConfigImpl();
    }

    /**
     * Returns the I/O configuration for the given face.
     * @param face the block face to pull the option from
     * @return the I/O configuration for the given face.
     */
    @NotNull MachineIOFaceConfig get(@NotNull BlockFace face);

    /**
     * Serializes the {@link MachineIOConfig} to NBT.
     * @param groups The available slot groups.
     * @return a NBT compound containing the serialized {@link MachineIOConfig}
     */
    @NotNull CompoundTag writeNbt(@NotNull SlotGroup @NotNull[] groups);

    /**
     * Deserializes the {@link MachineIOConfig} from NBT.
     *
     * @param nbt    the nbt compound to deserialize.
     * @param groups The available slot groups.
     */
    void readNbt(@NotNull CompoundTag nbt, @NotNull SlotGroup @Nullable[] groups);
}
