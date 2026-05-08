package dev.galacticraft.machinelib.client.api.screen;

import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.menu.Tank;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenu;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.impl.model.MachineBakedModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MultiblockMachineScreen<Menu extends MultiblockMachineMenu> extends AbstractMachineScreen<Menu> {

    /**
     * Creates a machine screen backed by a formed multiblock menu.
     *
     * @param menu menu
     * @param title title
     * @param texture background texture
     */
    public MultiblockMachineScreen(
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
        return null;
    }

    @Override
    protected BlockState machineBlockState() {
        return null;
    }

    @Override
    protected void refreshMachineModel() {

    }

}