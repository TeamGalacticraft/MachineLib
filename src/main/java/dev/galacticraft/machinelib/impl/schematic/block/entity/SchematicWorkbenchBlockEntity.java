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
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.impl.schematic.MachineLibSchematicContent;
import dev.galacticraft.machinelib.impl.schematic.menu.SchematicWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Machine block entity for the schematic workbench.
 *
 * <p>The workbench accepts schematic paper and writes a selected multiblock id onto it.</p>
 */
public final class SchematicWorkbenchBlockEntity extends MachineBlockEntity {
    public static final int PAPER_SLOT = 0;

    public static final StorageSpec STORAGE_SPEC = StorageSpec.of(
            MachineItemStorage.spec(
                    ItemResourceSlot.builder(TransferType.PROCESSING)
                            .pos(12, 24)
                            .filter((item, components) -> item == MachineLibSchematicContent.schematicPaper())
                            .capacity(1)
            )
    );

    /**
     * Creates a schematic workbench block entity.
     *
     * @param pos block position
     * @param state block state
     */
    public SchematicWorkbenchBlockEntity(final BlockPos pos, final BlockState state) {
        super(MachineLibSchematicContent.schematicWorkbenchBlockEntity(), pos, state, STORAGE_SPEC);
    }

    /**
     * Ticks the workbench.
     *
     * @param level server level
     * @param pos block position
     * @param state block state
     * @param profiler profiler
     * @return machine status
     */
    @Override
    protected @NotNull MachineStatus tick(
            final @NotNull ServerLevel level,
            final @NotNull BlockPos pos,
            final @NotNull BlockState state,
            final @NotNull ProfilerFiller profiler
    ) {
        return MachineStatuses.IDLE;
    }

    /**
     * Creates the workbench menu.
     *
     * @param syncId menu sync id
     * @param inventory player inventory
     * @param player player
     * @return workbench menu
     */
    @Override
    public @Nullable MachineMenu<SchematicWorkbenchBlockEntity> createMenu(
            final int syncId,
            final Inventory inventory,
            final Player player
    ) {
        return new SchematicWorkbenchMenu(syncId, player, this);
    }
}