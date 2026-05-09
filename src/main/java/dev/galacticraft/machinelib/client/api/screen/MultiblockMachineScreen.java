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

import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.menu.Tank;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenu;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.api.screen.port.MultiblockPortPreviewScene;
import dev.galacticraft.machinelib.client.api.screen.port.PortPreviewScene;
import dev.galacticraft.machinelib.client.impl.model.MachineBakedModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class MultiblockMachineScreen<Menu extends MultiblockMachineMenu> extends AbstractMachineScreen<Menu> {

    private static final int PORT_PANEL_WIDTH = 170;
    private static final int PORT_ROW_HEIGHT = 14;
    private static final int PORT_PANEL_PADDING = 6;

    private final List<MultiblockPortFace> visiblePortFaces = new ArrayList<>();

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
    protected PortPreviewScene createPortPreviewScene() {
        return new MultiblockPortPreviewScene<>(this.menu);
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

    /**
     * Renders the temporary multiblock port configuration panel.
     *
     * <p>This is intentionally simple and text-based. It exists only so the
     * packet/menu backend can be tested before the future 3D multiblock port
     * editor replaces it.</p>
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param delta tick delta
     */
    @Override
    protected void renderForeground(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY,
            final float delta
    ) {
        super.renderForeground(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        this.renderPortPanel(
                graphics,
                mouseX,
                mouseY
        );
    }

    /**
     * Handles clicks on the temporary port panel.
     *
     * <p>Left click cycles forward, right click cycles backward, and middle click
     * or control-click removes the configured port from the clicked face.</p>
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button mouse button
     * @return {@code true} if the click was handled
     */
    @Override
    public boolean mouseClicked(
            final double mouseX,
            final double mouseY,
            final int button
    ) {
        if (this.handlePortPanelClick(
                mouseX,
                mouseY,
                button
        )) {
            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    /**
     * Draws the temporary multiblock port panel.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    private void renderPortPanel(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY
    ) {
        this.refreshVisiblePortFaces();

        final int x = this.leftPos + this.imageWidth + SPACING;
        final int y = this.topPos + SPACING;
        final int height = PORT_PANEL_PADDING * 2 + 12 + this.visiblePortFaces.size() * PORT_ROW_HEIGHT;

        graphics.fill(
                x,
                y,
                x + PORT_PANEL_WIDTH,
                y + height,
                0xCC111111
        );

        graphics.drawString(
                this.font,
                Component.literal("Ports"),
                x + PORT_PANEL_PADDING,
                y + PORT_PANEL_PADDING,
                0xFFFFFFFF,
                false
        );

        int rowY = y + PORT_PANEL_PADDING + 14;

        for (int i = 0; i < this.visiblePortFaces.size(); i++) {
            final MultiblockPortFace face = this.visiblePortFaces.get(i);
            final boolean hovered = mouseIn(
                    mouseX,
                    mouseY,
                    x + PORT_PANEL_PADDING,
                    rowY,
                    PORT_PANEL_WIDTH - PORT_PANEL_PADDING * 2,
                    PORT_ROW_HEIGHT
            );

            if (hovered) {
                graphics.fill(
                        x + PORT_PANEL_PADDING,
                        rowY,
                        x + PORT_PANEL_WIDTH - PORT_PANEL_PADDING,
                        rowY + PORT_ROW_HEIGHT,
                        0x55FFFFFF
                );
            }

            graphics.drawString(
                    this.font,
                    this.describePortFace(face),
                    x + PORT_PANEL_PADDING + 2,
                    rowY + 3,
                    0xFFE0E0E0,
                    false
            );

            rowY += PORT_ROW_HEIGHT;
        }
    }

    /**
     * Handles a mouse click against the temporary port panel.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button mouse button
     * @return {@code true} if a port row was clicked
     */
    private boolean handlePortPanelClick(
            final double mouseX,
            final double mouseY,
            final int button
    ) {
        this.refreshVisiblePortFaces();

        final int x = this.leftPos + this.imageWidth + SPACING;
        final int y = this.topPos + SPACING + PORT_PANEL_PADDING + 14;

        for (int i = 0; i < this.visiblePortFaces.size(); i++) {
            final int rowY = y + i * PORT_ROW_HEIGHT;

            if (!mouseIn(
                    mouseX,
                    mouseY,
                    x + PORT_PANEL_PADDING,
                    rowY,
                    PORT_PANEL_WIDTH - PORT_PANEL_PADDING * 2,
                    PORT_ROW_HEIGHT
            )) {
                continue;
            }

            final MultiblockPortFace face = this.visiblePortFaces.get(i);
            final boolean remove = button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE || Screen.hasControlDown();
            final boolean reverse = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || Screen.hasShiftDown();

            if (remove) {
                this.menu.removePort(face);
            } else {
                this.menu.cyclePort(
                        face,
                        reverse
                );
            }

            this.playButtonSound();
            return true;
        }

        return false;
    }

    /**
     * Rebuilds the visible temporary port face list.
     *
     * <p>Only faces with at least one valid port rule are shown. This avoids
     * cluttering the temporary UI with exposed faces that cannot be configured.</p>
     */
    private void refreshVisiblePortFaces() {
        this.visiblePortFaces.clear();

        for (final MultiblockPortFace face : this.menu.exposedPortFaces()) {
            if (!this.menu.portRulesFor(face).isEmpty()) {
                this.visiblePortFaces.add(face);
            }
        }

        this.visiblePortFaces.sort(Comparator
                .comparingInt((MultiblockPortFace face) -> face.relativePos().getY())
                .thenComparingInt(face -> face.relativePos().getZ())
                .thenComparingInt(face -> face.relativePos().getX())
                .thenComparing(face -> face.face().getName())
        );
    }

    /**
     * Creates one readable row of text for a port face.
     *
     * @param face port face
     * @return row text
     */
    private Component describePortFace(final MultiblockPortFace face) {
        final Optional<ConfiguredMultiblockPort> configured = this.menu.configuredPortAt(face);
        final String position = face.relativePos().getX()
                + ","
                + face.relativePos().getY()
                + ","
                + face.relativePos().getZ();

        if (configured.isEmpty()) {
            return Component.literal(position + " " + shortFace(face.face()) + " : none");
        }

        final ConfiguredMultiblockPort port = configured.get();

        return Component.literal(
                position
                        + " "
                        + shortFace(face.face())
                        + " : "
                        + port.type().name()
                        + " "
                        + port.mode().name()
                        + " "
                        + port.target().id().getPath()
        );
    }

    /**
     * Gets a short readable direction name.
     *
     * @param direction direction
     * @return short direction name
     */
    private static String shortFace(final Direction direction) {
        return switch (direction) {
            case DOWN -> "D";
            case UP -> "U";
            case NORTH -> "N";
            case SOUTH -> "S";
            case WEST -> "W";
            case EAST -> "E";
        };
    }

}