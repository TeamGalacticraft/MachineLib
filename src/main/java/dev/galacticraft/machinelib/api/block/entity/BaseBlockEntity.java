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

package dev.galacticraft.machinelib.api.block.entity;

import dev.galacticraft.machinelib.api.menu.SynchronizedMenu;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A block entity with additional synchronization utilities.
 *
 * @see SynchronizedMenu
 */
public abstract class BaseBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {
    /**
     * Constructs a new base block entity.
     *
     * @param type the type of block entity
     * @param pos the position of the block entity in the level
     * @param state the block state of the block entity
     */
    protected BaseBlockEntity(BlockEntityType<? extends BaseBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Updates the machine every tick.
     *
     * @param level the world.
     * @param pos the position of this machine.
     * @param state the block state of this machine.
     * @param profiler the world profiler.
     */
    public abstract void tickBase(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ProfilerFiller profiler);

    /**
     * {@return a packet payload that updates the machine's state}
     * Only data necessary for rendering the machine in-world should be included.
     */
    public abstract @Nullable CustomPacketPayload createUpdatePayload();

    public abstract void populateUpdateTag(CompoundTag tag);

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.getBlockPos();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag tag = super.getUpdateTag(registryLookup);
        populateUpdateTag(tag);
        return tag;
    }

    @Override
    public final @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        CustomPacketPayload payload = this.createUpdatePayload();

        return payload == null ? null : new ClientboundBundlePacket(List.of(ClientboundBlockEntityDataPacket.create(this), new ClientboundCustomPayloadPacket(payload)));
    }

    /**
     * {@return a newly created menu}
     *
     * @param syncId the synchronization id of the menu
     * @param inventory the player's inventory
     * @param player the player opening the menu
     */
    @Nullable
    @Override
    public abstract SynchronizedMenu<? extends BaseBlockEntity> createMenu(int syncId, Inventory inventory, Player player);

    @Override
    public @NotNull Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    /**
     * Broadcasts an update to all players tracking this machine (within view distance).
     *
     * @param payload the packet to send
     */
    protected void broadcastToPlayers(CustomPacketPayload payload) {
        if (this.level != null && !this.level.isClientSide) {
            for (ServerPlayer player : ((ServerLevel) BaseBlockEntity.this.level).getChunkSource().chunkMap.getPlayers(new ChunkPos(BaseBlockEntity.this.worldPosition), false)) {
                ServerPlayNetworking.getSender(player).sendPacket(payload);
            }
        }
    }

    /**
     * Marks the block entity for re-rendering.
     */
    public void requestRerender() {
        if (this.level != null && this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 0);
        }
    }
}
