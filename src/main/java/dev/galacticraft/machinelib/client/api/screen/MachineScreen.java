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

package dev.galacticraft.machinelib.client.api.screen;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.menu.Tank;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.impl.model.MachineBakedModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MachineScreen<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> extends AbstractMachineScreen<Menu> {

    private MachineBakedModel model;
    private BlockState previousState;

    /**
     * Creates a machine screen backed by a block entity machine menu.
     *
     * @param menu menu
     * @param title title
     * @param texture background texture
     */
    protected MachineScreen(
            final Menu menu,
            final Component title,
            final ResourceLocation texture
    ) {
        super(
                menu,
                menu.playerInventory,
                menu.player,
                menu.security,
                title,
                texture
        );
    }

    @Override
    protected Inventory playerInventory() {
        return this.menu.playerInventory;
    }

    @Override
    protected Player machinePlayer() {
        return this.menu.player;
    }

    @Override
    protected SecuritySettings security() {
        return this.menu.security;
    }

    @Override
    protected IOConfig configuration() {
        return this.menu.configuration;
    }

    @Override
    protected MachineState state() {
        return this.menu.state;
    }

    @Override
    protected RedstoneMode redstoneMode() {
        return this.menu.redstoneMode;
    }

    @Override
    protected void setLocalRedstoneMode(final RedstoneMode mode) {
        this.menu.redstoneMode = mode;
    }

    @Override
    protected MachineEnergyStorage energyStorage() {
        return this.menu.energyStorage;
    }

    @Override
    protected List<Tank> tanks() {
        return this.menu.tanks;
    }

    @Override
    protected boolean isFaceLocked(final BlockFace face) {
        return this.menu.isFaceLocked(face);
    }

    @Override
    protected void cycleFaceConfig(
            final BlockFace face,
            final boolean reverse,
            final boolean reset
    ) {
        this.menu.cycleFaceConfig(face, reverse, reset);
    }

    @Override
    protected MachineBakedModel machineModel() {
        return this.model;
    }

    @Override
    protected BlockState machineBlockState() {
        return this.menu.be.getBlockState();
    }

    @Override
    protected void refreshMachineModel() {
        final BlockState blockState = this.menu.be.getBlockState();

        if (this.model != null && blockState.equals(this.previousState)) {
            return;
        }

        this.previousState = blockState;

        final BakedModel bakedModel =
                this.minecraft.getModelManager()
                        .getBlockModelShaper()
                        .getBlockModel(blockState);

        if (WrapperBakedModel.unwrap(bakedModel) instanceof MachineBakedModel machineBakedModel) {
            this.model = machineBakedModel;
        }
    }

}