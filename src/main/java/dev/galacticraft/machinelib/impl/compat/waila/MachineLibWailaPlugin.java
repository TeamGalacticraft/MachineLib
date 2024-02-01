package dev.galacticraft.machinelib.impl.compat.waila;

import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.MachineConfiguration;
import dev.galacticraft.machinelib.impl.Constant;
import mcp.mobius.waila.api.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MachineLibWailaPlugin implements IWailaPlugin {
    private static final IBlockComponentProvider COMPONENT_PROVIDER = new IBlockComponentProvider() {
        @Override
        public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (Screen.hasShiftDown()) {
                MachineConfiguration configuration = MachineConfiguration.create();
                configuration.readTag(accessor.getData().raw().getCompound("config"));
                tooltip.addLine(Component.translatable("ui.machinelib.machine.redstone_mode.tooltip", configuration.getRedstoneMode().getName()).setStyle(Constant.Text.RED_STYLE));
                if (configuration.getSecurity().getOwner() != null && configuration.getSecurity().getUsername() != null) {
                    String username = configuration.getSecurity().getUsername();
                    tooltip.addLine(Component.translatable("ui.machinelib.machine.security.owner", Component.literal(username).setStyle(Constant.Text.WHITE_STYLE)).setStyle(Constant.Text.AQUA_STYLE));
                }
            }
        }
    };

    @Override
    public void register(IRegistrar registrar) {
        registrar.addBlockData((IDataProvider<MachineBlockEntity>) (data, accessor, config) -> data.raw().put("config", accessor.getTarget().getConfiguration().createTag()), MachineBlock.class);
        registrar.addComponent(COMPONENT_PROVIDER, TooltipPosition.TAIL, MachineBlock.class);
    }
}
