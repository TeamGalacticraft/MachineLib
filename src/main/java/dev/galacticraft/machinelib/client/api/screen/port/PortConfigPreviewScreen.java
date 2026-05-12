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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/**
 * Full-screen advanced port configuration preview.
 */
public final class PortConfigPreviewScreen extends Screen {

    private static final int MARGIN = 18;
    private static final int TITLE_HEIGHT = 14;
    private static final int GAP = 8;
    private static final int SIDEBAR_WIDTH = 230;

    private final Screen parent;
    private final PortPreviewScene scene;
    private final PortPreviewWidget widget;
    private final PortConfigSidebarWidget sidebar;

    /**
     * Creates a full-screen port configuration preview.
     *
     * @param parent parent screen
     * @param scene preview scene
     */
    public PortConfigPreviewScreen(
            final Screen parent,
            final PortPreviewScene scene
    ) {
        super(scene.title());

        this.parent = parent;
        this.scene = scene;
        this.widget = new PortPreviewWidget(
                0,
                0,
                1,
                1
        );
        this.sidebar = new PortConfigSidebarWidget();
    }

    @Override
    protected void init() {
        final int contentY = MARGIN + TITLE_HEIGHT;
        final int contentHeight = this.height - MARGIN * 2 - TITLE_HEIGHT;
        final int previewWidth = this.width - MARGIN * 2 - SIDEBAR_WIDTH - GAP;

        this.widget.setBounds(
                MARGIN,
                contentY,
                previewWidth,
                contentHeight
        );

        this.sidebar.setBounds(
                MARGIN + previewWidth + GAP,
                contentY,
                SIDEBAR_WIDTH,
                contentHeight
        );

        this.widget.centerOnScene(this.scene);

        this.widget.setRequireSecondClickToEdit(true);
    }

    @Override
    public void render(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY,
            final float delta
    ) {
        super.render(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        graphics.drawString(
                this.font,
                this.title,
                MARGIN,
                MARGIN,
                0xFFFFFFFF,
                false
        );

        this.widget.render(
                graphics,
                this.scene,
                mouseX,
                mouseY
        );

        this.sidebar.render(
                graphics,
                this.font,
                this.scene,
                this.widget,
                mouseX,
                mouseY
        );
    }

    @Override
    public boolean mouseClicked(
            final double mouseX,
            final double mouseY,
            final int button
    ) {
        if (this.sidebar.mouseClicked(
                this.scene,
                this.widget,
                mouseX,
                mouseY,
                button
        )) {
            return true;
        }

        if (this.widget.mouseClicked(
                this.scene,
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

    @Override
    public boolean mouseReleased(
            final double mouseX,
            final double mouseY,
            final int button
    ) {
        if (this.widget.mouseReleased(
                mouseX,
                mouseY,
                button
        )) {
            return true;
        }

        return super.mouseReleased(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean mouseDragged(
            final double mouseX,
            final double mouseY,
            final int button,
            final double deltaX,
            final double deltaY
    ) {
        if (this.widget.mouseDragged(
                mouseX,
                mouseY,
                button,
                deltaX,
                deltaY
        )) {
            return true;
        }

        return super.mouseDragged(
                mouseX,
                mouseY,
                button,
                deltaX,
                deltaY
        );
    }

    @Override
    public boolean mouseScrolled(
            final double mouseX,
            final double mouseY,
            final double horizontalAmount,
            final double verticalAmount
    ) {
        if (this.widget.mouseScrolled(
                this.scene,
                mouseX,
                mouseY,
                verticalAmount
        )) {
            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}