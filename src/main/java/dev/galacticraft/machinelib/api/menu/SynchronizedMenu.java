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

package dev.galacticraft.machinelib.api.menu;

import dev.galacticraft.machinelib.api.block.entity.BaseBlockEntity;
import dev.galacticraft.machinelib.client.impl.menu.MenuDataClient;
import dev.galacticraft.machinelib.impl.menu.MenuDataImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

/**
 * Base menu for machines.
 * Handles the basic synchronization and slot management of machines.
 *
 * @param <BE> The type of machine block entity this menu is linked to.
 */
public abstract class SynchronizedMenu<BE extends BaseBlockEntity> extends AbstractContainerMenu {
    /**
     * The machine that is the source of this menu.
     */
    @ApiStatus.Internal
    public final @NotNull BE be;

    /**
     * The level this menu exists in
     */
    public final @NotNull ContainerLevelAccess levelAccess;

    /**
     * The player interacting with this menu.
     */
    public final @NotNull Player player;

    /**
     * The inventory of the player opening this menu
     */
    public final @NotNull Inventory playerInventory;

    /**
     * Handles synchronization of data between the logical server and the client.
     *
     * @see #registerData(MenuData)
     */
    private final MenuData data;

    /**
     * Constructs a new menu for a machine.
     * Called on the logical server
     *
     * @param syncId The sync id for this menu.
     * @param player The player who is interacting with this menu.
     * @param be The machine this menu is for.
     */
    public SynchronizedMenu(MenuType<? extends SynchronizedMenu<BE>> type, int syncId, @NotNull Player player, @NotNull BE be) {
        super(type, syncId);
        assert player instanceof ServerPlayer;

        this.be = be;
        this.data = new MenuDataImpl((ServerPlayer) player, syncId);

        this.player = player;
        this.playerInventory = player.getInventory();
        this.levelAccess = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        this.data.synchronizeFull();
    }

    /**
     * Constructs a new menu for a machine.
     * Called on the logical client
     *
     * @param syncId The sync id for this menu.
     * @param pos the position of the block being opened
     * @param inventory The inventory of the player interacting with this menu.
     * @param type The type of menu this is.
     */
    protected SynchronizedMenu(MenuType<? extends SynchronizedMenu<BE>> type, int syncId, @NotNull Inventory inventory, @NotNull BlockPos pos) {
        super(type, syncId);

        this.data = new MenuDataClient(syncId);
        this.player = inventory.player;
        this.playerInventory = inventory;

        this.be = (BE) inventory.player.level().getBlockEntity(pos);
        this.levelAccess = ContainerLevelAccess.create(inventory.player.level(), pos);
    }

    /**
     * Registers the sync handlers for the menu.
     */
    @MustBeInvokedByOverriders
    public void registerData(@NotNull MenuData data) {
        // Override this method to register data to be synchronized.
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, this.be.getBlockState().getBlock());
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        this.data.synchronize();
    }

    @ApiStatus.Internal
    public MenuData getData() {
        return this.data;
    }
}
