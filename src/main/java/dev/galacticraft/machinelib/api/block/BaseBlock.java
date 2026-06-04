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

package dev.galacticraft.machinelib.api.block;

import dev.galacticraft.machinelib.api.block.entity.BaseBlockEntity;
import dev.galacticraft.machinelib.api.menu.SynchronizedMenu;
import dev.galacticraft.machinelib.impl.block.entity.BaseBlockEntityTicker;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The base block for all machines.
 */
public abstract class BaseBlock extends BaseEntityBlock {
    /**
     * Creates a new machine block.
     *
     * @param settings The settings for the block.
     */
    public BaseBlock(Properties settings) {
        super(settings);
    }

    @Nullable
    @Override
    public abstract BaseBlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof BaseBlockEntity be) {
                player.openMenu(new ExtendedScreenHandlerFactory<>() {
                    @Nullable
                    @Override
                    public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
                        assert player.getInventory() == inventory;

                        SynchronizedMenu<?> menu = be.createMenu(syncId, inventory, player);
                        if (menu != null) {
                            menu.registerData(menu.getData());
                        }
                        return menu;
                    }

                    @Override
                    public @NotNull Component getDisplayName() {
                        return be.getDisplayName();
                    }

                    @Override
                    public Object getScreenOpeningData(ServerPlayer player) {
                        return be.getScreenOpeningData(player);
                    }

                    @Override
                    public boolean shouldCloseCurrentScreen() {
                        return be.shouldCloseCurrentScreen();
                    }
                });
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <B extends BlockEntity> BlockEntityTicker<B> getTicker(Level level, BlockState state, BlockEntityType<B> type) {
        return !level.isClientSide ? BaseBlockEntityTicker.getInstance() : null;
    }
}
