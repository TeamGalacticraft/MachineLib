package dev.galacticraft.machinelib.client.impl.multiblock;

import dev.galacticraft.machinelib.impl.multiblock.TestIronCubeMultiblockMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class TestIronCubeMultiblockScreen extends AbstractContainerScreen<TestIronCubeMultiblockMenu> {

    public TestIronCubeMultiblockScreen(
            final TestIronCubeMultiblockMenu menu,
            final Inventory inventory,
            final Component title
    ) {
        super(
                menu,
                inventory,
                title
        );

        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(
            final GuiGraphics graphics,
            final float partialTick,
            final int mouseX,
            final int mouseY
    ) {
        graphics.fill(
                this.leftPos,
                this.topPos,
                this.leftPos + this.imageWidth,
                this.topPos + this.imageHeight,
                0xFF202020
        );
    }

    @Override
    public void render(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY,
            final float partialTick
    ) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
        this.renderTooltip(
                graphics,
                mouseX,
                mouseY
        );
    }

}