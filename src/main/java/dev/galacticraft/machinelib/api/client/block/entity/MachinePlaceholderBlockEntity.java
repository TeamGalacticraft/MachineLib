package dev.galacticraft.machinelib.api.client.block.entity;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachinePlaceholderBlockEntity extends BlockEntity implements RenderDataBlockEntity {
    public MachinePlaceholderBlockEntity(@NotNull MachineType<? extends MachineBlockEntity, ? extends MachineMenu<? extends MachineBlockEntity>> type, BlockPos pos, BlockState blockState) {
        super(type.getBlockEntityType(), pos, blockState);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return null;
    }

    @Override
    public @Nullable Object getRenderData() {
        return super.getRenderData();
    }
}
