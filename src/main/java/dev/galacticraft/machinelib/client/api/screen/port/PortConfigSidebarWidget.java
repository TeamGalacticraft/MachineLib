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
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private static final int COLOR_PANEL = 0xEE151515;
    private static final int COLOR_HEADER = 0xEE202020;
    private static final int COLOR_DIVIDER = 0xFF303030;
    private static final int COLOR_GROUP = 0xFF242424;
    private static final int COLOR_GROUP_HOVER = 0xFF323232;
    private static final int COLOR_CHILD = 0xCC1F1F1F;
    private static final int COLOR_CHILD_HOVER = 0xFF2E2E2E;
    private static final int COLOR_ACTIVE = 0xFF23405F;
    private static final int COLOR_ACTIVE_HOVER = 0xFF2E5278;
    private static final int COLOR_DISABLED = 0xAA1A1A1A;
    private static final int COLOR_CONFLICT = 0xFF5F3A23;

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

        graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, COLOR_PANEL);
        graphics.fill(this.x, this.y, this.x + this.width, this.y + 24, COLOR_HEADER);
        graphics.fill(this.x, this.y + 24, this.x + this.width, this.y + 25, COLOR_DIVIDER);

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
                scene,
                scene.optionsFor(selectedFace),
                selectedFace,
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
     * @param selectedFace selected face
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param startY start y
     * @return final content y
     */
    private int renderOptions(
            final GuiGraphics graphics,
            final Font font,
            final PortPreviewScene scene,
            final List<PreviewPortOption> options,
            final PreviewPortFace selectedFace,
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

            if (option.matches(selectedFace)) {
                this.expandedGroups.add(option.groupKey());
            }
        }

        int rowY = startY;

        if (clearOption != null) {
            rowY = this.renderOptionRow(graphics, font, scene, clearOption, selectedFace, mouseX, mouseY, rowY, false);
        }

        for (final Map.Entry<String, List<PreviewPortOption>> entry : groupedOptions.entrySet()) {
            final String groupKey = entry.getKey();
            final List<PreviewPortOption> groupOptions = entry.getValue();

            if (groupOptions.isEmpty()) {
                continue;
            }

            final boolean groupHasActiveOption = groupOptions.stream().anyMatch(option -> option.matches(selectedFace));

            rowY = this.renderGroupRow(
                    graphics,
                    font,
                    groupKey,
                    groupOptions.get(0).groupLabel(),
                    groupHasActiveOption,
                    mouseX,
                    mouseY,
                    rowY
            );

            if (!this.expandedGroups.contains(groupKey)) {
                continue;
            }

            for (final PreviewPortOption option : groupOptions) {
                rowY = this.renderOptionRow(
                        graphics,
                        font,
                        scene,
                        option,
                        selectedFace,
                        mouseX,
                        mouseY,
                        rowY,
                        true
                );
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
     * @param active whether this group contains the current active option
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
            final boolean active,
            final int mouseX,
            final int mouseY,
            final int rowY
    ) {
        final int rowX = this.x + PADDING;
        final int rowWidth = this.width - PADDING * 2 - this.scrollbarAllowance();
        final boolean expanded = this.expandedGroups.contains(groupKey);
        final boolean hovered = this.mouseIn(mouseX, mouseY, rowX, rowY, rowWidth, GROUP_ROW_HEIGHT - 2);

        if (rowY + GROUP_ROW_HEIGHT >= this.contentTop && rowY <= this.contentBottom) {
            final int background = active
                    ? hovered ? COLOR_ACTIVE_HOVER : COLOR_ACTIVE
                    : hovered ? COLOR_GROUP_HOVER : COLOR_GROUP;

            graphics.fill(rowX, rowY, rowX + rowWidth, rowY + GROUP_ROW_HEIGHT - 2, background);
            graphics.fill(rowX, rowY, rowX + 2, rowY + GROUP_ROW_HEIGHT - 2, active ? 0xFF7AB7FF : expanded ? 0xFF5C8DFF : 0xFF444444);

            graphics.drawString(font, expanded ? "▾" : "▸", rowX + 6, rowY + 5, 0xFFE6E6E6, false);
            graphics.drawString(font, label, rowX + 18, rowY + 5, active ? 0xFFFFFFFF : 0xFFEAEAEA, false);

            this.rows.add(new Row(rowX, rowY, rowWidth, GROUP_ROW_HEIGHT - 2, groupKey, null, null, PreviewPortOptionState.VALID));
        }

        return rowY + GROUP_ROW_HEIGHT;
    }

    /**
     * Renders one selectable option row.
     *
     * @param graphics GUI graphics
     * @param font font
     * @param option option
     * @param selectedFace selected face
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param rowY row y
     * @param child whether this is a child row
     * @return next row y
     */
    private int renderOptionRow(
            final GuiGraphics graphics,
            final Font font,
            final PortPreviewScene scene,
            final PreviewPortOption option,
            final PreviewPortFace selectedFace,
            final int mouseX,
            final int mouseY,
            final int rowY,
            final boolean child
    ) {
        final int rowHeight = child ? CHILD_ROW_HEIGHT : GROUP_ROW_HEIGHT;
        final int rowX = this.x + PADDING + (child ? 12 : 0);
        final int rowWidth = this.width - PADDING * 2 - (child ? 12 : 0) - this.scrollbarAllowance();
        final boolean hovered = this.mouseIn(mouseX, mouseY, rowX, rowY, rowWidth, rowHeight - 2);
        final PreviewPortOptionState state = this.stateFor(scene, option, selectedFace);

        if (rowY + rowHeight >= this.contentTop && rowY <= this.contentBottom) {
            final int background = this.backgroundColor(state, hovered, child);

            graphics.fill(rowX, rowY, rowX + rowWidth, rowY + rowHeight - 2, background);

            if (state == PreviewPortOptionState.CURRENT) {
                graphics.fill(rowX, rowY, rowX + 2, rowY + rowHeight - 2, 0xFF7AB7FF);
                graphics.drawString(font, "✓", rowX + 5, rowY + (child ? 4 : 5), 0xFFFFFFFF, false);
            }

            final int labelX = rowX + (state == PreviewPortOptionState.CURRENT ? 17 : 6);

            graphics.drawString(
                    font,
                    child ? option.compactTargetLabel() : option.label(),
                    labelX,
                    rowY + (child ? 4 : 5),
                    this.textColor(state, option),
                    false
            );

            final Component tooltip = child ? option.fullTargetLabel() : null;

            if (hovered && tooltip != null) {
                this.hoveredTooltip = tooltip;
            }

            this.rows.add(new Row(rowX, rowY, rowWidth, rowHeight - 2, null, option, tooltip, state));
        }

        return rowY + rowHeight;
    }

    /**
     * Gets the visual state for an option row.
     *
     * @param scene preview scene
     * @param option option
     * @param selectedFace selected face
     * @return row state
     */
    private PreviewPortOptionState stateFor(
            final PortPreviewScene scene,
            final PreviewPortOption option,
            final PreviewPortFace selectedFace
    ) {
        return scene.optionStateFor(
                selectedFace,
                option
        );
    }

    /**
     * Gets a row background colour.
     *
     * @param state row state
     * @param hovered whether row is hovered
     * @param child whether row is a child row
     * @return ARGB colour
     */
    private int backgroundColor(
            final PreviewPortOptionState state,
            final boolean hovered,
            final boolean child
    ) {
        return switch (state) {
            case CURRENT -> hovered ? COLOR_ACTIVE_HOVER : COLOR_ACTIVE;
            case DISABLED -> COLOR_DISABLED;
            case CONFLICT -> hovered ? 0xFF73482C : COLOR_CONFLICT;
            case VALID -> hovered ? COLOR_CHILD_HOVER : child ? COLOR_CHILD : COLOR_GROUP;
        };
    }

    /**
     * Gets row text colour.
     *
     * @param state row state
     * @param option option
     * @return ARGB colour
     */
    private int textColor(
            final PreviewPortOptionState state,
            final PreviewPortOption option
    ) {
        if (option.clearsPort()) {
            return 0xFFAAAAAA;
        }

        return switch (state) {
            case CURRENT -> 0xFFFFFFFF;
            case DISABLED -> 0xFF777777;
            case CONFLICT -> 0xFFFFC08A;
            case VALID -> 0xFFEAEAEA;
        };
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

            if (row.option() != null
                    && row.state() != PreviewPortOptionState.DISABLED
                    && row.state() != PreviewPortOptionState.CONFLICT) {
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
     * @param state row state
     */
    private record Row(
            int x,
            int y,
            int width,
            int height,
            String groupKey,
            PreviewPortOption option,
            Component tooltip,
            PreviewPortOptionState state
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