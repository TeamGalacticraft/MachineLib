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

package dev.galacticraft.machinelib.client.api.screen.port;

import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Right-side advanced editor panel for the full-screen port config view.
 */
public final class PortConfigSidebarWidget {

    private static final int PADDING = 8;
    private static final int ROW_HEIGHT = 22;

    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * Updates this sidebar's screen bounds.
     *
     * @param x sidebar x
     * @param y sidebar y
     * @param width sidebar width
     * @param height sidebar height
     */
    public void setBounds(
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Renders selected face details and clickable port options.
     *
     * @param graphics GUI graphics
     * @param font font
     * @param scene preview scene
     * @param widget preview widget
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    public void render(
            final GuiGraphics graphics,
            final Font font,
            final PortPreviewScene scene,
            final PortPreviewWidget widget,
            final int mouseX,
            final int mouseY
    ) {
        graphics.fill(
                this.x,
                this.y,
                this.x + this.width,
                this.y + this.height,
                0xDD202020
        );

        graphics.drawString(
                font,
                Component.translatable(Constant.TranslationKey.PORT_CONFIG_SELECTED),
                this.x + PADDING,
                this.y + PADDING,
                0xFFFFFFFF,
                false
        );

        final PreviewPortFace selectedFace = widget.selectedFace();

        if (selectedFace == null) {
            graphics.drawWordWrap(
                    font,
                    Component.translatable(Constant.TranslationKey.PORT_CONFIG_NONE_SELECTED),
                    this.x + PADDING,
                    this.y + PADDING + 16,
                    this.width - PADDING * 2,
                    0xFFB0B0B0
            );
            return;
        }

        int lineY = this.y + PADDING + 18;

        for (final Component line : scene.detailsFor(selectedFace)) {
            graphics.drawWordWrap(
                    font,
                    line,
                    this.x + PADDING,
                    lineY,
                    this.width - PADDING * 2,
                    0xFFE0E0E0
            );

            lineY += font.split(
                    line,
                    this.width - PADDING * 2
            ).size() * font.lineHeight + 3;

            if (lineY > this.y + this.height - 80) {
                break;
            }
        }

        lineY += 8;

        graphics.drawString(
                font,
                Component.translatable(Constant.TranslationKey.PORT_CONFIG_ALLOWED),
                this.x + PADDING,
                lineY,
                0xFFFFFFFF,
                false
        );

        lineY += 14;

        final List<PreviewPortOption> options = scene.optionsFor(selectedFace);

        for (int i = 0; i < options.size(); i++) {
            final PreviewPortOption option = options.get(i);
            final int rowY = lineY + i * ROW_HEIGHT;

            if (rowY + ROW_HEIGHT > this.y + this.height - PADDING) {
                break;
            }

            final boolean hovered = this.mouseIn(
                    mouseX,
                    mouseY,
                    this.x + PADDING,
                    rowY,
                    this.width - PADDING * 2,
                    ROW_HEIGHT - 2
            );

            graphics.fill(
                    this.x + PADDING,
                    rowY,
                    this.x + this.width - PADDING,
                    rowY + ROW_HEIGHT - 2,
                    hovered ? 0xEE404040 : 0xAA303030
            );

            graphics.drawString(
                    font,
                    option.label(),
                    this.x + PADDING + 5,
                    rowY + 4,
                    option.clearsPort() ? 0xFFAAAAAA : 0xFFFFFFFF,
                    false
            );
        }
    }

    /**
     * Handles clicking a sidebar option.
     *
     * @param scene preview scene
     * @param widget preview widget
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button mouse button
     * @return {@code true} if handled
     */
    public boolean mouseClicked(
            final PortPreviewScene scene,
            final PortPreviewWidget widget,
            final double mouseX,
            final double mouseY,
            final int button
    ) {
        if (button != 0) {
            return false;
        }

        final PreviewPortFace selectedFace = widget.selectedFace();

        if (selectedFace == null) {
            return false;
        }

        final List<PreviewPortOption> options = scene.optionsFor(selectedFace);

        final int optionsStartY = this.optionsStartY(scene, widget);

        for (int i = 0; i < options.size(); i++) {
            final int rowY = optionsStartY + i * ROW_HEIGHT;

            if (rowY + ROW_HEIGHT > this.y + this.height - PADDING) {
                break;
            }

            if (this.mouseIn(
                    mouseX,
                    mouseY,
                    this.x + PADDING,
                    rowY,
                    this.width - PADDING * 2,
                    ROW_HEIGHT - 2
            )) {
                scene.setPort(
                        selectedFace,
                        options.get(i)
                );

                widget.refreshSelection(scene);
                return true;
            }
        }

        return false;
    }

    /**
     * Calculates where the option rows begin.
     *
     * @param scene preview scene
     * @param widget preview widget
     * @return option row start y
     */
    private int optionsStartY(
            final PortPreviewScene scene,
            final PortPreviewWidget widget
    ) {
        final PreviewPortFace selectedFace = widget.selectedFace();

        if (selectedFace == null) {
            return this.y + PADDING + 32;
        }

        int lineY = this.y + PADDING + 18;

        for (final Component ignored : scene.detailsFor(selectedFace)) {
            lineY += 12;

            if (lineY > this.y + this.height - 80) {
                break;
            }
        }

        return lineY + 22;
    }

    /**
     * Checks whether a point is inside a rectangle.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param x rectangle x
     * @param y rectangle y
     * @param width rectangle width
     * @param height rectangle height
     * @return {@code true} if inside
     */
    private boolean mouseIn(
            final double mouseX,
            final double mouseY,
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        return mouseX >= x
                && mouseY >= y
                && mouseX < x + width
                && mouseY < y + height;
    }
}