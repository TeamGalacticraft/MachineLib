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
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

/**
 * Shared 3D port preview widget used by both ordinary machines and formed
 * multiblock machines.
 */
public final class PortPreviewWidget {

    private static final int POP_OUT_SIZE = 14;
    private static final int POP_OUT_PADDING = 3;

    private final PortPreviewCamera camera = new PortPreviewCamera();

    private int x;
    private int y;
    private int width;
    private int height;

    private boolean dragging;
    private int dragButton = -1;
    private double lastMouseX;
    private double lastMouseY;

    private Runnable popOutHandler;
    private PreviewPortFace hoveredFace;
    private PreviewPortFace selectedFace;

    private final PortPreviewMeshCache meshCache = new PortPreviewMeshCache();

    /**
     * Creates a preview widget.
     *
     * @param x widget x
     * @param y widget y
     * @param width widget width
     * @param height widget height
     */
    public PortPreviewWidget(
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        this.setBounds(
                x,
                y,
                width,
                height
        );
    }

    /**
     * Sets the pop-out action for this widget.
     *
     * @param popOutHandler pop-out action, or {@code null}
     */
    public void setPopOutHandler(final Runnable popOutHandler) {
        this.popOutHandler = popOutHandler;
    }

    /**
     * Updates widget bounds.
     *
     * @param x widget x
     * @param y widget y
     * @param width widget width
     * @param height widget height
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
     * Centers the preview camera on the current scene bounds.
     *
     * @param scene preview scene
     */
    public void centerOnScene(final PortPreviewScene scene) {
        this.camera.applyBounds(
                scene.bounds(),
                this.width,
                this.height
        );

        this.camera.centerOnBounds();
    }

    /**
     * Renders the widget.
     *
     * @param graphics GUI graphics
     * @param scene preview scene
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    public void render(
            final GuiGraphics graphics,
            final PortPreviewScene scene,
            final int mouseX,
            final int mouseY
    ) {
        this.camera.applyBounds(
                scene.bounds(),
                this.width,
                this.height
        );

        this.hoveredFace = PortPreviewRenderer.pick(
                scene,
                this.camera,
                this.x,
                this.y,
                this.width,
                this.height,
                mouseX,
                mouseY
        );

        PortPreviewRenderer.render(
                graphics,
                scene,
                this.meshCache.get(scene),
                this.camera,
                this.hoveredFace,
                this.selectedFace,
                this.x,
                this.y,
                this.width,
                this.height
        );

        this.renderPopOutButton(
                graphics,
                mouseX,
                mouseY
        );

        if (this.contains(
                mouseX,
                mouseY
        )) {
            graphics.fill(this.x, this.y, this.x + this.width, this.y + 1, 0xFFFFFFFF);
            graphics.fill(this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, 0xFFFFFFFF);
            graphics.fill(this.x, this.y, this.x + 1, this.y + this.height, 0xFFFFFFFF);
            graphics.fill(this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height, 0xFFFFFFFF);
        }
    }

    /**
     * Handles mouse click.
     *
     * @param scene preview scene
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button mouse button
     * @return {@code true} if handled
     */
    public boolean mouseClicked(
            final PortPreviewScene scene,
            final double mouseX,
            final double mouseY,
            final int button
    ) {
        if (!this.contains(
                mouseX,
                mouseY
        )) {
            return false;
        }

        if (this.isPopOutButton(mouseX, mouseY)) {
            if (this.popOutHandler != null) {
                this.popOutHandler.run();
            }

            return true;
        }

        final PreviewPortFace picked = PortPreviewRenderer.pick(
                scene,
                this.camera,
                this.x,
                this.y,
                this.width,
                this.height,
                mouseX,
                mouseY
        );

        if (picked != null && !Screen.hasAltDown()) {
            this.selectedFace = picked;

            if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE || Screen.hasControlDown()) {
                scene.removePort(picked);
            } else {
                scene.cyclePort(
                        picked,
                        button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || Screen.hasShiftDown()
                );
            }

            return true;
        }

        this.dragging = true;
        this.dragButton = button;
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        return true;
    }

    /**
     * Handles mouse release.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button mouse button
     * @return {@code true} if handled
     */
    public boolean mouseReleased(
            final double mouseX,
            final double mouseY,
            final int button
    ) {
        if (!this.dragging || this.dragButton != button) {
            return false;
        }

        this.dragging = false;
        this.dragButton = -1;
        return true;
    }

    /**
     * Handles mouse dragging.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button mouse button
     * @param deltaX mouse delta x
     * @param deltaY mouse delta y
     * @return {@code true} if handled
     */
    public boolean mouseDragged(
            final double mouseX,
            final double mouseY,
            final int button,
            final double deltaX,
            final double deltaY
    ) {
        if (!this.dragging || this.dragButton != button) {
            return false;
        }

        final double moveX = mouseX - this.lastMouseX;
        final double moveY = mouseY - this.lastMouseY;

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE || Screen.hasControlDown()) {
            this.camera.moveFocus(
                    moveX,
                    moveY
            );
        } else {
            this.camera.rotate(
                    moveX,
                    moveY
            );
        }

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        return true;
    }

    /**
     * Handles mouse scrolling.
     *
     * @param scene preview scene
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param amount scroll amount
     * @return {@code true} if handled
     */
    public boolean mouseScrolled(
            final PortPreviewScene scene,
            final double mouseX,
            final double mouseY,
            final double amount
    ) {
        if (!this.contains(
                mouseX,
                mouseY
        )) {
            return false;
        }

        this.camera.applyBounds(
                scene.bounds(),
                this.width,
                this.height
        );

        this.camera.zoom(
                amount,
                this.width,
                this.height
        );

        return true;
    }

    /**
     * Gets this widget's screen exclusion zone.
     *
     * @return exclusion rectangle
     */
    public Rect2i exclusionZone() {
        return new Rect2i(
                this.x,
                this.y,
                this.width,
                this.height
        );
    }

    /**
     * Draws the pop-out button.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    private void renderPopOutButton(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY
    ) {
        if (this.popOutHandler == null) {
            return;
        }

        final int buttonX = this.x + this.width - POP_OUT_SIZE - POP_OUT_PADDING;
        final int buttonY = this.y + this.height - POP_OUT_SIZE - POP_OUT_PADDING;
        final boolean hovered = this.isPopOutButton(mouseX, mouseY);

        graphics.fill(
                buttonX,
                buttonY,
                buttonX + POP_OUT_SIZE,
                buttonY + POP_OUT_SIZE,
                hovered ? 0xEE505050 : 0xCC303030
        );

        graphics.hLine(buttonX + 3, buttonX + 10, buttonY + 3, 0xFFFFFFFF);
        graphics.vLine(buttonX + 10, buttonY + 3, buttonY + 10, 0xFFFFFFFF);
        graphics.hLine(buttonX + 6, buttonX + 10, buttonY + 10, 0xFFFFFFFF);
        graphics.vLine(buttonX + 3, buttonY + 3, buttonY + 7, 0xFFFFFFFF);
    }

    /**
     * Checks whether a point hits the pop-out button.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @return {@code true} if the point hits the button
     */
    private boolean isPopOutButton(
            final double mouseX,
            final double mouseY
    ) {
        final int buttonX = this.x + this.width - POP_OUT_SIZE - POP_OUT_PADDING;
        final int buttonY = this.y + this.height - POP_OUT_SIZE - POP_OUT_PADDING;

        return mouseX >= buttonX
                && mouseY >= buttonY
                && mouseX < buttonX + POP_OUT_SIZE
                && mouseY < buttonY + POP_OUT_SIZE;
    }

    /**
     * Checks whether a point is inside this widget.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @return {@code true} if inside
     */
    private boolean contains(
            final double mouseX,
            final double mouseY
    ) {
        return mouseX >= this.x
                && mouseY >= this.y
                && mouseX < this.x + this.width
                && mouseY < this.y + this.height;
    }
}