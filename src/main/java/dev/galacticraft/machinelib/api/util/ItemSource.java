/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

package dev.galacticraft.machinelib.api.util;

import com.google.common.base.Predicates;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ItemSource {
    private final IOConfig config;
    private final Storage<ItemVariant> storage;
    private AdjacentBlockApiCache<Storage<ItemVariant>> cache = null;

    public ItemSource(IOConfig config, MachineItemStorage storage) {
        this.config = config;
        this.storage = storage.getExposedStorage(ResourceFlow.OUTPUT);
    }

    public ItemSource(MachineBlockEntity machine) {
        this.config = machine.getIOConfig();
        this.storage = machine.itemStorage().getExposedStorage(ResourceFlow.OUTPUT);
    }

    public void trySpreadItems(ServerLevel level, BlockPos pos, BlockState state) {
        if (this.cache == null) {
            this.cache = AdjacentBlockApiCache.create(ItemStorage.SIDED, level, pos);
        }

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (Direction direction : Constant.Cache.DIRECTIONS) {
            IOFace face = this.config.get(BlockFace.from(facing, direction));
            if (face.getType().willAcceptResource(ResourceType.ITEM) && face.getFlow().canFlowIn(ResourceFlow.OUTPUT)) {
                Storage<ItemVariant> storage = this.cache.find(direction);
                if (storage != null) {
                    StorageUtil.move(this.storage, storage, Predicates.alwaysTrue(), 16, null);
                }
            }
        }
    }
}
