package dev.galacticraft.machinelib.client.api.screen.port;

import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * Right-side advanced editor panel for the full-screen port config view.
 */
public final class PortConfigSidebarWidget {

    private static final int PADDING = 8;
    private static final int GROUP_ROW_HEIGHT = 20;
    private static final int CHILD_ROW_HEIGHT = 18;
    private static final int DETAIL_RESERVED_HEIGHT = 80;
    private static final int SCROLLBAR_WIDTH = 5;
    private static final int SCROLL_SPEED = 18;

    private final Set<String> expandedGroups = new HashSet<>();
    private final List<Row> rows = new ArrayList<>();

    private int x;
    private int y;
    private int width;
    private int height;

    private int contentTop;
    private int contentBottom;
    private int contentHeight;

    private double scroll;
    private double targetScroll;

    private boolean draggingScrollbar;
    private double scrollbarDragOffset;

    private Component hoveredTooltip;

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

        this.clampScroll();
    }

    /**
     * Renders selected face details and grouped clickable port options.
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
        this.rows.clear();
        this.hoveredTooltip = null;
        this.smoothScroll();

        graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, 0xEE151515);
        graphics.fill(this.x, this.y, this.x + this.width, this.y + 24, 0xEE202020);
        graphics.fill(this.x, this.y + 24, this.x + this.width, this.y + 25, 0xFF303030);

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
                    this.y + 30,
                    this.width - PADDING * 2,
                    0xFFB0B0B0
            );
            return;
        }

        int lineY = this.renderDetails(graphics, font, scene, selectedFace);
        lineY += 8;

        graphics.drawString(
                font,
                Component.translatable(Constant.TranslationKey.PORT_CONFIG_ALLOWED),
                this.x + PADDING,
                lineY,
                0xFFFFFFFF,
                false
        );

        this.contentTop = lineY + 14;
        this.contentBottom = this.y + this.height - PADDING;

        graphics.enableScissor(
                this.x + PADDING,
                this.contentTop,
                this.x + this.width - PADDING,
                this.contentBottom
        );

        final int endY = this.renderOptions(
                graphics,
                font,
                scene.optionsFor(selectedFace),
                mouseX,
                mouseY,
                this.contentTop - (int) Math.round(this.scroll)
        );

        graphics.disableScissor();

        this.contentHeight = Math.max(0, endY - this.contentTop + (int) Math.round(this.scroll));
        this.clampScroll();
        this.renderScrollbar(graphics, mouseX, mouseY);

        if (this.hoveredTooltip != null) {
            graphics.renderTooltip(font, this.hoveredTooltip, mouseX, mouseY);
        }
    }

    /**
     * Renders selected face detail lines.
     *
     * @param graphics GUI graphics
     * @param font font
     * @param scene preview scene
     * @param selectedFace selected face
     * @return next y position after detail text
     */
    private int renderDetails(
            final GuiGraphics graphics,
            final Font font,
            final PortPreviewScene scene,
            final PreviewPortFace selectedFace
    ) {
        int lineY = this.y + 30;
        final int textWidth = this.width - PADDING * 2;
        final int detailBottom = this.y + this.height - DETAIL_RESERVED_HEIGHT;

        for (final Component line : scene.detailsFor(selectedFace)) {
            final int lineHeight = font.split(line, textWidth).size() * font.lineHeight;

            if (lineY + lineHeight > detailBottom) {
                break;
            }

            graphics.drawWordWrap(font, line, this.x + PADDING, lineY, textWidth, 0xFFCFCFCF);
            lineY += lineHeight + 3;
        }

        return lineY;
    }

    /**
     * Renders all option rows.
     *
     * @param graphics GUI graphics
     * @param font font
     * @param options options
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param startY start y
     * @return final content y
     */
    private int renderOptions(
            final GuiGraphics graphics,
            final Font font,
            final List<PreviewPortOption> options,
            final int mouseX,
            final int mouseY,
            final int startY
    ) {
        final Map<String, List<PreviewPortOption>> groupedOptions = new LinkedHashMap<>();
        PreviewPortOption clearOption = null;

        for (final PreviewPortOption option : options) {
            if (option.clearsPort()) {
                clearOption = option;
                continue;
            }

            groupedOptions.computeIfAbsent(option.groupKey(), ignored -> new ArrayList<>()).add(option);
        }

        int rowY = startY;

        if (clearOption != null) {
            rowY = this.renderOptionRow(graphics, font, clearOption, mouseX, mouseY, rowY, false);
        }

        for (final Map.Entry<String, List<PreviewPortOption>> entry : groupedOptions.entrySet()) {
            final String groupKey = entry.getKey();
            final List<PreviewPortOption> groupOptions = entry.getValue();

            if (groupOptions.isEmpty()) {
                continue;
            }

            rowY = this.renderGroupRow(
                    graphics,
                    font,
                    groupKey,
                    groupOptions.get(0).groupLabel(),
                    mouseX,
                    mouseY,
                    rowY
            );

            if (!this.expandedGroups.contains(groupKey)) {
                continue;
            }

            for (final PreviewPortOption option : groupOptions) {
                rowY = this.renderOptionRow(graphics, font, option, mouseX, mouseY, rowY, true);
            }
        }

        return rowY;
    }

    /**
     * Renders one expandable group row.
     *
     * @param graphics GUI graphics
     * @param font font
     * @param groupKey group key
     * @param label label
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param rowY row y
     * @return next row y
     */
    private int renderGroupRow(
            final GuiGraphics graphics,
            final Font font,
            final String groupKey,
            final Component label,
            final int mouseX,
            final int mouseY,
            final int rowY
    ) {
        final int rowX = this.x + PADDING;
        final int rowWidth = this.width - PADDING * 2 - this.scrollbarAllowance();
        final boolean expanded = this.expandedGroups.contains(groupKey);
        final boolean hovered = this.mouseIn(mouseX, mouseY, rowX, rowY, rowWidth, GROUP_ROW_HEIGHT - 2);

        if (rowY + GROUP_ROW_HEIGHT >= this.contentTop && rowY <= this.contentBottom) {
            graphics.fill(rowX, rowY, rowX + rowWidth, rowY + GROUP_ROW_HEIGHT - 2, hovered ? 0xFF323232 : 0xFF242424);
            graphics.fill(rowX, rowY, rowX + 2, rowY + GROUP_ROW_HEIGHT - 2, expanded ? 0xFF5C8DFF : 0xFF444444);

            graphics.drawString(font, expanded ? "▾" : "▸", rowX + 6, rowY + 5, 0xFFE6E6E6, false);
            graphics.drawString(font, label, rowX + 18, rowY + 5, 0xFFFFFFFF, false);

            this.rows.add(new Row(rowX, rowY, rowWidth, GROUP_ROW_HEIGHT - 2, groupKey, null, null));
        }

        return rowY + GROUP_ROW_HEIGHT;
    }

    /**
     * Renders one selectable option row.
     *
     * @param graphics GUI graphics
     * @param font font
     * @param option option
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param rowY row y
     * @param child whether this is a child row
     * @return next row y
     */
    private int renderOptionRow(
            final GuiGraphics graphics,
            final Font font,
            final PreviewPortOption option,
            final int mouseX,
            final int mouseY,
            final int rowY,
            final boolean child
    ) {
        final int rowHeight = child ? CHILD_ROW_HEIGHT : GROUP_ROW_HEIGHT;
        final int rowX = this.x + PADDING + (child ? 12 : 0);
        final int rowWidth = this.width - PADDING * 2 - (child ? 12 : 0) - this.scrollbarAllowance();
        final boolean hovered = this.mouseIn(mouseX, mouseY, rowX, rowY, rowWidth, rowHeight - 2);

        if (rowY + rowHeight >= this.contentTop && rowY <= this.contentBottom) {
            graphics.fill(rowX, rowY, rowX + rowWidth, rowY + rowHeight - 2, hovered ? 0xFF2E2E2E : child ? 0xCC1F1F1F : 0xFF242424);

            graphics.drawString(
                    font,
                    child ? option.compactTargetLabel() : option.label(),
                    rowX + 6,
                    rowY + (child ? 4 : 5),
                    option.clearsPort() ? 0xFFAAAAAA : 0xFFEAEAEA,
                    false
            );

            final Component tooltip = child ? option.fullTargetLabel() : null;

            if (hovered && tooltip != null) {
                this.hoveredTooltip = tooltip;
            }

            this.rows.add(new Row(rowX, rowY, rowWidth, rowHeight - 2, null, option, tooltip));
        }

        return rowY + rowHeight;
    }

    /**
     * Handles clicking a sidebar row or scrollbar.
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
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            return true;
        }

        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        if (this.mouseInScrollbarThumb(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scrollbarDragOffset = mouseY - this.scrollbarThumbY();
            return true;
        }

        final PreviewPortFace selectedFace = widget.selectedFace();

        if (selectedFace == null) {
            return true;
        }

        for (final Row row : this.rows) {
            if (!row.contains(mouseX, mouseY)) {
                continue;
            }

            if (row.groupKey() != null) {
                if (!this.expandedGroups.remove(row.groupKey())) {
                    this.expandedGroups.add(row.groupKey());
                }

                return true;
            }

            if (row.option() != null) {
                scene.setPort(selectedFace, row.option());
                widget.refreshSelection(scene);
                return true;
            }
        }

        return true;
    }

    /**
     * Handles scroll wheel or trackpad scrolling.
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
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }

        this.targetScroll -= amount * SCROLL_SPEED;
        this.clampScroll();
        return true;
    }

    /**
     * Handles scrollbar dragging and middle-button drag scrolling.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button mouse button
     * @param deltaX mouse x delta
     * @param deltaY mouse y delta
     * @return {@code true} if handled
     */
    public boolean mouseDragged(
            final double mouseX,
            final double mouseY,
            final int button,
            final double deltaX,
            final double deltaY
    ) {
        if (this.draggingScrollbar) {
            final int trackTop = this.contentTop;
            final int trackHeight = Math.max(1, this.contentBottom - this.contentTop);
            final int thumbHeight = this.scrollbarThumbHeight();
            final double maxScroll = this.maxScroll();
            final double draggableHeight = Math.max(1.0D, trackHeight - thumbHeight);

            this.targetScroll = ((mouseY - this.scrollbarDragOffset - trackTop) / draggableHeight) * maxScroll;
            this.scroll = this.targetScroll;
            this.clampScroll();
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && this.contains(mouseX, mouseY)) {
            this.targetScroll += deltaY;
            this.clampScroll();
            return true;
        }

        return false;
    }

    /**
     * Handles releasing the scrollbar.
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
        if (this.draggingScrollbar && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.draggingScrollbar = false;
            return true;
        }

        return false;
    }

    /**
     * Smoothly interpolates current scroll toward target scroll.
     */
    private void smoothScroll() {
        this.scroll += (this.targetScroll - this.scroll) * 0.35D;

        if (Math.abs(this.targetScroll - this.scroll) < 0.2D) {
            this.scroll = this.targetScroll;
        }
    }

    /**
     * Clamps scroll values to valid content bounds.
     */
    private void clampScroll() {
        final double maxScroll = this.maxScroll();

        this.targetScroll = Math.max(0.0D, Math.min(maxScroll, this.targetScroll));
        this.scroll = Math.max(0.0D, Math.min(maxScroll, this.scroll));
    }

    /**
     * Gets max scroll offset.
     *
     * @return max scroll
     */
    private double maxScroll() {
        return Math.max(0, this.contentHeight - Math.max(0, this.contentBottom - this.contentTop));
    }

    /**
     * Renders the scrollbar.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    private void renderScrollbar(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY
    ) {
        if (this.maxScroll() <= 0.0D) {
            return;
        }

        final int trackX = this.x + this.width - PADDING + 1;
        final int trackTop = this.contentTop;
        final int trackBottom = this.contentBottom;
        final int thumbY = this.scrollbarThumbY();
        final int thumbHeight = this.scrollbarThumbHeight();
        final boolean hovered = this.mouseIn(mouseX, mouseY, trackX - 1, thumbY, SCROLLBAR_WIDTH + 2, thumbHeight);

        graphics.fill(trackX, trackTop, trackX + SCROLLBAR_WIDTH, trackBottom, 0xAA0D0D0D);
        graphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, hovered || this.draggingScrollbar ? 0xFF6F6F6F : 0xFF4A4A4A);
    }

    /**
     * Gets the scrollbar thumb y.
     *
     * @return thumb y
     */
    private int scrollbarThumbY() {
        final int trackTop = this.contentTop;
        final int trackHeight = Math.max(1, this.contentBottom - this.contentTop);
        final int thumbHeight = this.scrollbarThumbHeight();
        final double maxScroll = this.maxScroll();

        if (maxScroll <= 0.0D) {
            return trackTop;
        }

        return trackTop + (int) Math.round((trackHeight - thumbHeight) * (this.scroll / maxScroll));
    }

    /**
     * Gets scrollbar thumb height.
     *
     * @return thumb height
     */
    private int scrollbarThumbHeight() {
        final int viewportHeight = Math.max(1, this.contentBottom - this.contentTop);
        final int totalHeight = Math.max(viewportHeight, this.contentHeight);

        return Math.max(18, (int) Math.round((double) viewportHeight / totalHeight * viewportHeight));
    }

    /**
     * Checks whether a point hits the scrollbar thumb.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @return {@code true} if hit
     */
    private boolean mouseInScrollbarThumb(
            final double mouseX,
            final double mouseY
    ) {
        if (this.maxScroll() <= 0.0D) {
            return false;
        }

        final int trackX = this.x + this.width - PADDING + 1;
        return this.mouseIn(mouseX, mouseY, trackX - 2, this.scrollbarThumbY(), SCROLLBAR_WIDTH + 4, this.scrollbarThumbHeight());
    }

    /**
     * Gets extra row width reserved for scrollbar.
     *
     * @return scrollbar allowance
     */
    private int scrollbarAllowance() {
        return this.maxScroll() > 0.0D ? SCROLLBAR_WIDTH + 4 : 0;
    }

    /**
     * Checks whether a point is inside this sidebar.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @return {@code true} if inside
     */
    private boolean contains(
            final double mouseX,
            final double mouseY
    ) {
        return this.mouseIn(mouseX, mouseY, this.x, this.y, this.width, this.height);
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

    /**
     * Rendered sidebar row with matching click bounds.
     *
     * @param x row x
     * @param y row y
     * @param width row width
     * @param height row height
     * @param groupKey group key, or {@code null}
     * @param option option, or {@code null}
     * @param tooltip tooltip, or {@code null}
     */
    private record Row(
            int x,
            int y,
            int width,
            int height,
            String groupKey,
            PreviewPortOption option,
            Component tooltip
    ) {

        /**
         * Checks whether a point is inside this row.
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
}