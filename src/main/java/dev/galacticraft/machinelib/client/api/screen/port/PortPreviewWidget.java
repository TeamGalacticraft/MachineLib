package dev.galacticraft.machinelib.client.api.screen.port;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

/**
 * Shared 3D port preview widget used by both ordinary machines and formed
 * multiblock machines.
 */
public final class PortPreviewWidget {

    private final PortPreviewCamera camera = new PortPreviewCamera();

    private int x;
    private int y;
    private int width;
    private int height;

    private boolean dragging;
    private double lastMouseX;
    private double lastMouseY;

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
        PortPreviewRenderer.render(
                graphics,
                scene,
                this.camera,
                this.x,
                this.y,
                this.width,
                this.height
        );

        if (this.contains(
                mouseX,
                mouseY
        )) {
            graphics.fill(
                    this.x,
                    this.y,
                    this.x + this.width,
                    this.y + 1,
                    0xFFFFFFFF
            );
            graphics.fill(
                    this.x,
                    this.y + this.height - 1,
                    this.x + this.width,
                    this.y + this.height,
                    0xFFFFFFFF
            );
            graphics.fill(
                    this.x,
                    this.y,
                    this.x + 1,
                    this.y + this.height,
                    0xFFFFFFFF
            );
            graphics.fill(
                    this.x + this.width - 1,
                    this.y,
                    this.x + this.width,
                    this.y + this.height,
                    0xFFFFFFFF
            );
        }
    }

    /**
     * Handles mouse click.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button mouse button
     * @return {@code true} if handled
     */
    public boolean mouseClicked(
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

        this.dragging = true;
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
        if (!this.dragging) {
            return false;
        }

        this.dragging = false;
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
        if (!this.dragging) {
            return false;
        }

        this.camera.rotate(
                mouseX - this.lastMouseX,
                mouseY - this.lastMouseY
        );

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        return true;
    }

    /**
     * Handles mouse scrolling.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param amount scroll amount
     * @return {@code true} if handled
     */
    public boolean mouseScrolled(
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

        this.camera.zoom(amount);
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