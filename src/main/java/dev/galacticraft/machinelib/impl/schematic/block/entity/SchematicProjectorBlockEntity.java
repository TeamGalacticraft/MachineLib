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

package dev.galacticraft.machinelib.impl.schematic.block.entity;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.impl.schematic.MachineLibSchematicContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Machine block entity for the schematic projector.
 *
 * <p>The schematic projector will later hold schematic paper and broadcast a persistent multiblock
 * hologram to nearby clients. The projector itself should not edit schematic placement data; it
 * should only display the schematic data stored on the inserted paper.</p>
 */
public final class SchematicProjectorBlockEntity extends MachineBlockEntity {
    private static final StorageSpec STORAGE_SPEC = StorageSpec.empty();

    /**
     * Creates a schematic projector block entity.
     *
     * @param pos the block position
     * @param state the block state
     */
    public SchematicProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(MachineLibSchematicContent.schematicProjectorBlockEntity(), pos, state, STORAGE_SPEC);
    }

    /**
     * Ticks the schematic projector.
     *
     * <p>The projector currently has no server-side behaviour. Later, this should detect whether
     * schematic paper is present, synchronize active hologram data, and update clients when the
     * inserted schematic changes.</p>
     *
     * @param level the server level
     * @param pos the block position
     * @param state the current block state
     * @param profiler the active profiler
     * @return the current machine status
     */
    @Override
    protected @NotNull MachineStatus tick(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler) {
        return MachineStatuses.IDLE;
    }

    /**
     * Creates the schematic projector menu.
     *
     * <p>No menu is created yet. This method should return a real menu once the projector inventory
     * and GUI are implemented.</p>
     *
     * @param syncId the menu sync id
     * @param inventory the player inventory
     * @param player the player opening the menu
     * @return {@code null} until the projector menu exists
     */
    @Override
    public @Nullable MachineMenu<? extends MachineBlockEntity> createMenu(int syncId, Inventory inventory, Player player) {
        return null;
    }
}