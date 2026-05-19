package dev.galacticraft.machinelib.client.impl.schematic;

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockRegistry;
import dev.galacticraft.machinelib.client.api.screen.MachineScreen;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.GltfVisualModel;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.GltfVisualModelManager;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.gui.GltfGuiPreviewRenderer;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.multiblock.MachineLibMultiblocks;
import dev.galacticraft.machinelib.impl.network.c2s.SchematicWorkbenchWritePayload;
import dev.galacticraft.machinelib.impl.schematic.block.entity.SchematicWorkbenchBlockEntity;
import dev.galacticraft.machinelib.impl.schematic.menu.SchematicWorkbenchMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Screen for the schematic workbench.
 */
public final class SchematicWorkbenchScreen extends MachineScreen<SchematicWorkbenchBlockEntity, SchematicWorkbenchMenu> {
    private static final ResourceLocation TEXTURE = Constant.id("textures/gui/schematic_workbench.png");

    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 220;

    private static final int WRITE_BUTTON_X = 32;
    private static final int WRITE_BUTTON_Y = 23;
    private static final int WRITE_BUTTON_WIDTH = 96;
    private static final int WRITE_BUTTON_HEIGHT = 18;
    private static final int WRITE_BUTTON_TEXT_PADDING = 6;

    private static final int INFO_PANEL_X = 9;
    private static final int INFO_PANEL_Y = 44;
    private static final int INFO_PANEL_WIDTH = 121;
    private static final int INFO_PANEL_HEIGHT = 87;

    private static final int PREVIEW_X = INFO_PANEL_X + 56;
    private static final int PREVIEW_Y = INFO_PANEL_Y + 18;
    private static final int PREVIEW_RIGHT_PADDING = 6;
    private static final int PREVIEW_BOTTOM_PADDING = 6;

    private static final int INFO_TEXT_X = INFO_PANEL_X + 3;
    private static final int INFO_TEXT_Y = INFO_PANEL_Y + 2;
    private static final int INFO_TEXT_PREVIEW_GAP = 3;

    private static final int SEARCH_X = 139;
    private static final int SEARCH_Y = 26;
    private static final int SEARCH_WIDTH = 104;

    private static final int SEARCH_TEXT_X = SEARCH_X + 4;
    private static final int SEARCH_TEXT_Y = SEARCH_Y + 4;
    private static final int SEARCH_TEXT_WIDTH = SEARCH_WIDTH - 18;

    private static final int SEARCH_ICON_U = 12;
    private static final int SEARCH_ICON_V = 242;
    private static final int SEARCH_ICON_WIDTH = 10;
    private static final int SEARCH_ICON_HEIGHT = 10;
    private static final int SEARCH_ICON_X = SEARCH_X + SEARCH_WIDTH - 13;
    private static final int SEARCH_ICON_Y = SEARCH_Y + 2;

    private static final int LIST_X = 136;
    private static final int LIST_Y = 42;
    private static final int LIST_WIDTH = 110;
    private static final int LIST_HEIGHT = 87;
    private static final int LIST_PADDING_X = 1;
    private static final int LIST_PADDING_Y = 1;
    private static final int LIST_PREVIEW_SPACE = 13;
    private static final int ROW_HEIGHT = 12;

    private static final int SCROLLBAR_X = 241;
    private static final int SCROLLBAR_Y = 43;
    private static final int SCROLLBAR_WIDTH = 5;
    private static final int SCROLLBAR_HEIGHT = 85;

    private static final int SCROLLBAR_TEXTURE_U = 221;
    private static final int SCROLLBAR_TEXTURE_V = 43;
    private static final int SCROLLBAR_TEXTURE_HEIGHT = 69;

    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 8;

    private final List<MultiblockDefinition> allDefinitions = new ArrayList<>();
    private final List<MultiblockDefinition> visibleDefinitions = new ArrayList<>();

    private @Nullable ResourceLocation selectedMultiblock;
    private @Nullable EditBox searchBox;
    private @Nullable Button writeButton;

    private int scroll;

    /**
     * Overrides removes default configuration panels due to not being necessary for this specific machine and also need more space for the GUI.
     */
    @Override
    protected void drawConfigurationPanels(GuiGraphics graphics, int mouseX, int mouseY) {
        //super.drawConfigurationPanels(graphics, mouseX, mouseY);
    }

    @Override
    protected void drawConfigurationPanelTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        //super.drawConfigurationPanelTooltips(graphics, mouseX, mouseY);
    }

    @Override
    public boolean checkConfigurationPanelClick(double mouseX, double mouseY, int button) {
        //return super.checkConfigurationPanelClick(mouseX, mouseY, button);
        return false;
    }

    @Override
    public List<Rect2i> getExclusionZones() {
        return List.of();
    }

    public SchematicWorkbenchScreen(
            final @NotNull SchematicWorkbenchMenu menu,
            final @NotNull Inventory inventory,
            final @NotNull Component title
    ) {
        super(menu, title, TEXTURE);
        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        this.allDefinitions.clear();
        this.allDefinitions.addAll(MachineLibMultiblocks.INSTANCE.definitions());
        this.allDefinitions.sort(Comparator.comparing(definition -> definition.id().toString()));

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + SEARCH_TEXT_X,
                this.topPos + SEARCH_TEXT_Y,
                SEARCH_TEXT_WIDTH,
                this.font.lineHeight,
                Component.translatable(Constant.TranslationKey.SCHEMATIC_WORKBENCH_SEARCH)
        );
        this.searchBox.setBordered(false);
        this.searchBox.setMaxLength(128);
        this.searchBox.setResponder(value -> {
            this.scroll = 0;
            this.rebuildVisibleDefinitions();
        });

        this.addRenderableWidget(this.searchBox);

        this.writeButton = Button.builder(Component.empty(), button -> this.writeSelectedSchematic())
                .bounds(this.leftPos + WRITE_BUTTON_X, this.topPos + WRITE_BUTTON_Y, WRITE_BUTTON_WIDTH, WRITE_BUTTON_HEIGHT)
                .build();

        this.addRenderableWidget(this.writeButton);

        this.rebuildVisibleDefinitions();
        this.updateButtonState();
    }

    private void rebuildVisibleDefinitions() {
        final String query = this.searchBox == null ? "" : this.searchBox.getValue().trim().toLowerCase(Locale.ROOT);

        this.visibleDefinitions.clear();

        for (final MultiblockDefinition definition : this.allDefinitions) {
            final String id = definition.id().toString().toLowerCase(Locale.ROOT);
            if (query.isEmpty() || id.contains(query)) {
                this.visibleDefinitions.add(definition);
            }
        }

        if (this.selectedMultiblock != null && this.visibleDefinitions.stream().noneMatch(definition -> definition.id().equals(this.selectedMultiblock))) {
            this.selectedMultiblock = null;
        }

        this.clampScroll();
        this.updateButtonState();
    }

    private void clampScroll() {
        final int visibleRows = this.visibleRowCount();
        final int maxScroll = Math.max(0, this.visibleDefinitions.size() - visibleRows);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));
    }

    private int visibleRowCount() {
        return (LIST_HEIGHT - LIST_PADDING_Y * 2) / ROW_HEIGHT;
    }

    private void updateButtonState() {
        if (this.writeButton != null) {
            this.writeButton.active = this.selectedMultiblock != null && this.menu.hasPaper();
        }
    }

    private void writeSelectedSchematic() {
        if (this.selectedMultiblock == null || !this.menu.hasPaper()) {
            return;
        }

        ClientPlayNetworking.send(new SchematicWorkbenchWritePayload(this.menu.containerId, this.selectedMultiblock));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.updateButtonState();
    }

    @Override
    protected void drawTitle(final GuiGraphics graphics) {
        this.drawFittedString(
                graphics,
                this.title,
                TITLE_X,
                TITLE_Y,
                118,
                0xFF404040,
                1.2F,
                0.55F
        );
    }

    @Override
    protected void renderMachineBackground(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY,
            final float delta
    ) {
        this.renderSearchPlaceholder(graphics);
        this.renderSearchIcon(graphics);
        this.renderMultiblockList(graphics, mouseX, mouseY);
        this.renderScrollbar(graphics);
        this.renderSelectedInfo(graphics);
    }

    @Override
    protected void renderForeground(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY,
            final float delta
    ) {
        super.renderForeground(graphics, mouseX, mouseY, delta);
        this.renderWriteButtonText(graphics);
    }

    private void renderWriteButtonText(final GuiGraphics graphics) {
        this.drawCenteredFittedString(
                graphics,
                Component.translatable(Constant.TranslationKey.SCHEMATIC_WORKBENCH_WRITE),
                this.leftPos + WRITE_BUTTON_X,
                this.topPos + WRITE_BUTTON_Y,
                WRITE_BUTTON_WIDTH,
                WRITE_BUTTON_HEIGHT,
                WRITE_BUTTON_WIDTH - WRITE_BUTTON_TEXT_PADDING * 2,
                this.writeButton != null && this.writeButton.active ? 0xFFFFFFFF : 0xFFA0A0A0,
                1.0F,
                0.45F
        );
    }

    private void renderSearchPlaceholder(final GuiGraphics graphics) {
        if (this.searchBox == null || this.searchBox.isFocused() || !this.searchBox.getValue().isEmpty()) {
            return;
        }

        graphics.drawString(
                this.font,
                Component.translatable(Constant.TranslationKey.SCHEMATIC_WORKBENCH_SEARCH),
                this.leftPos + SEARCH_TEXT_X,
                this.topPos + SEARCH_TEXT_Y,
                0xFF909090,
                false
        );
    }

    private void renderSearchIcon(final GuiGraphics graphics) {
        graphics.blit(
                TEXTURE,
                this.leftPos + SEARCH_ICON_X,
                this.topPos + SEARCH_ICON_Y,
                SEARCH_ICON_U,
                SEARCH_ICON_V,
                SEARCH_ICON_WIDTH,
                SEARCH_ICON_HEIGHT
        );
    }

    private void renderMultiblockList(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY
    ) {
        final int x = this.leftPos + LIST_X + LIST_PADDING_X;
        final int y = this.topPos + LIST_Y + LIST_PADDING_Y;
        final int rowRight = this.leftPos + LIST_X + LIST_WIDTH - SCROLLBAR_WIDTH - 1;
        final int textX = x + LIST_PREVIEW_SPACE;
        final int textWidth = rowRight - textX - 1;

        graphics.enableScissor(
                this.leftPos + LIST_X,
                this.topPos + LIST_Y,
                this.leftPos + LIST_X + LIST_WIDTH - SCROLLBAR_WIDTH,
                this.topPos + LIST_Y + LIST_HEIGHT
        );

        final int visibleRows = this.visibleRowCount();
        final int max = Math.min(this.visibleDefinitions.size(), this.scroll + visibleRows);

        for (int i = this.scroll; i < max; i++) {
            final MultiblockDefinition definition = this.visibleDefinitions.get(i);
            final int row = i - this.scroll;
            final int rowY = y + row * ROW_HEIGHT;

            final boolean selected = definition.id().equals(this.selectedMultiblock);
            final boolean hovered = mouseIn(mouseX, mouseY, x, rowY, rowRight - x, ROW_HEIGHT);

            if (selected) {
                graphics.fill(x - 2, rowY, rowRight, rowY + ROW_HEIGHT, 0x804A6CFF);
            } else if (hovered) {
                graphics.fill(x - 2, rowY, rowRight, rowY + ROW_HEIGHT, 0x30FFFFFF);
            }

            graphics.fill(x + 2, rowY + 2, x + 9, rowY + 9, 0x30404040);

            this.drawFittedString(
                    graphics,
                    Component.literal(this.displayName(definition.id())),
                    textX,
                    rowY + 1,
                    textWidth,
                    selected ? 0xFFFFFFFF : 0xFFE0E0E0,
                    1.0F,
                    0.55F
            );
        }

        if (this.visibleDefinitions.isEmpty()) {
            this.drawFittedString(
                    graphics,
                    Component.translatable(Constant.TranslationKey.SCHEMATIC_WORKBENCH_NO_RESULTS),
                    x,
                    y + 2,
                    rowRight - x,
                    0xFF909090,
                    0.8F,
                    0.45F
            );
        }

        graphics.disableScissor();
    }

    private void renderScrollbar(final GuiGraphics graphics) {
        if (this.visibleDefinitions.size() <= this.visibleRowCount()) {
            return;
        }

        final int visibleRows = this.visibleRowCount();
        final int maxScroll = Math.max(1, this.visibleDefinitions.size() - visibleRows);

        final int thumbHeight = Math.max(12, (int) ((float) visibleRows / (float) this.visibleDefinitions.size() * SCROLLBAR_HEIGHT));
        final int thumbTravel = SCROLLBAR_HEIGHT - thumbHeight;
        final int thumbY = this.topPos + SCROLLBAR_Y + Math.round((float) this.scroll / (float) maxScroll * thumbTravel);

        this.blitScrollbarThumb(graphics, this.leftPos + SCROLLBAR_X, thumbY, SCROLLBAR_WIDTH, thumbHeight);
    }

    private void blitScrollbarThumb(
            final GuiGraphics graphics,
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        if (height <= 2) {
            graphics.blit(TEXTURE, x, y, SCROLLBAR_TEXTURE_U, SCROLLBAR_TEXTURE_V, width, height);
            return;
        }

        graphics.blit(TEXTURE, x, y, SCROLLBAR_TEXTURE_U, SCROLLBAR_TEXTURE_V, width, 1);
        graphics.blit(TEXTURE, x, y + 1, SCROLLBAR_TEXTURE_U, SCROLLBAR_TEXTURE_V + 1, width, height - 2);
        graphics.blit(TEXTURE, x, y + height - 1, SCROLLBAR_TEXTURE_U, SCROLLBAR_TEXTURE_V + SCROLLBAR_TEXTURE_HEIGHT - 1, width, 1);
    }

    /**
     * Renders information and a glTF preview for the selected multiblock.
     *
     * @param graphics GUI graphics
     */
    private void renderSelectedInfo(final GuiGraphics graphics) {
        final int baseX = this.leftPos + INFO_TEXT_X;
        int y = this.topPos + INFO_TEXT_Y;

        final int panelRight = this.leftPos + INFO_PANEL_X + INFO_PANEL_WIDTH - 4;
        final int previewLeft = this.leftPos + PREVIEW_X;
        final int previewTop = this.topPos + PREVIEW_Y;
        final int previewRight = this.leftPos + INFO_PANEL_X + INFO_PANEL_WIDTH - PREVIEW_RIGHT_PADDING;
        final int previewBottom = this.topPos + INFO_PANEL_Y + INFO_PANEL_HEIGHT - PREVIEW_BOTTOM_PADDING;

        final int detailWidth = previewLeft - baseX - INFO_TEXT_PREVIEW_GAP;
        final int nameWidth = panelRight - baseX;

        if (this.selectedMultiblock == null) {
            this.drawFittedString(
                    graphics,
                    Component.translatable(Constant.TranslationKey.SCHEMATIC_WORKBENCH_NO_SELECTION),
                    baseX,
                    y,
                    nameWidth,
                    0xFF606060,
                    0.7F,
                    0.4F
            );
            return;
        }

        final MultiblockDefinition definition = MachineLibMultiblocks.getDefinition(this.selectedMultiblock);

        this.drawFittedString(
                graphics,
                Component.translatable(Constant.TranslationKey.SCHEMATIC_WORKBENCH_SELECTED),
                baseX,
                y,
                nameWidth,
                0xFF404040,
                0.8F,
                0.45F
        );

        y += 8;

        this.drawFittedString(
                graphics,
                Component.literal(this.selectedMultiblock.getPath()),
                baseX,
                y,
                nameWidth,
                0xFF2040A0,
                0.8F,
                0.35F
        );

        y += 14;

        if (definition != null) {
            final int sizeX = definition.pattern().sizeX();
            final int sizeY = definition.pattern().sizeY();
            final int sizeZ = definition.pattern().sizeZ();

            int requiredBlocks = 0;

            for (int x = 0; x < sizeX; x++) {
                for (int py = 0; py < sizeY; py++) {
                    for (int z = 0; z < sizeZ; z++) {
                        if (definition.pattern().predicateAt(x, py, z) != null) {
                            requiredBlocks++;
                        }
                    }
                }
            }

            this.drawFittedString(
                    graphics,
                    Component.literal("Size: " + sizeX + " x " + sizeY + " x " + sizeZ),
                    baseX,
                    y,
                    detailWidth,
                    0xFF404040,
                    0.7F,
                    0.35F
            );

            y += 8;

            this.drawFittedString(
                    graphics,
                    Component.literal("Blocks: " + requiredBlocks),
                    baseX,
                    y,
                    detailWidth,
                    0xFF404040,
                    0.7F,
                    0.35F
            );

            y += 8;

            this.drawFittedString(
                    graphics,
                    Component.literal("Layers: 0 - " + (sizeY - 1)),
                    baseX,
                    y,
                    detailWidth,
                    0xFF404040,
                    0.7F,
                    0.35F
            );

            y += 8;

            this.drawFittedString(
                    graphics,
                    Component.literal("Origin: Center"),
                    baseX,
                    y,
                    detailWidth,
                    0xFF404040,
                    0.7F,
                    0.35F
            );
        }

        final ResourceLocation previewModelId = definition == null ? null : definition.previewVisualModelId();
        final GltfVisualModel model = previewModelId == null ? null : GltfVisualModelManager.INSTANCE.get(previewModelId);

        if (model != null) {
            GltfGuiPreviewRenderer.render(
                    graphics,
                    model,
                    previewLeft,
                    previewTop,
                    previewRight - previewLeft,
                    previewBottom - previewTop,
                    0.0F
            );
        }
    }

    private void drawFittedString(
            final GuiGraphics graphics,
            final Component text,
            final int x,
            final int y,
            final int maxWidth,
            final int color,
            final float preferredScale,
            final float minimumScale
    ) {
        final int width = this.font.width(text);
        final float scale = this.fitScale(width, maxWidth, preferredScale, minimumScale);

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(
                this.font,
                text,
                Math.round(x / scale),
                Math.round(y / scale),
                color,
                false
        );
        graphics.pose().popPose();
    }

    private void drawCenteredFittedString(
            final GuiGraphics graphics,
            final Component text,
            final int x,
            final int y,
            final int width,
            final int height,
            final int maxTextWidth,
            final int color,
            final float preferredScale,
            final float minimumScale
    ) {
        final int textWidth = this.font.width(text);
        final float scale = this.fitScale(textWidth, maxTextWidth, preferredScale, minimumScale);
        final int scaledWidth = Math.round(textWidth * scale);
        final int scaledHeight = Math.round(this.font.lineHeight * scale);
        final int textX = x + (width - scaledWidth) / 2;
        final int textY = y + (height - scaledHeight) / 2 + 1;

        this.drawFittedString(graphics, text, textX, textY, maxTextWidth, color, scale, scale);
    }

    private float fitScale(
            final int textWidth,
            final int maxWidth,
            final float preferredScale,
            final float minimumScale
    ) {
        if (textWidth <= 0 || maxWidth <= 0) {
            return minimumScale;
        }

        return Math.max(minimumScale, Math.min(preferredScale, (float) maxWidth / (float) textWidth));
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final int listX = this.leftPos + LIST_X;
        final int listY = this.topPos + LIST_Y;

        if (mouseIn(mouseX, mouseY, listX, listY, LIST_WIDTH - SCROLLBAR_WIDTH, LIST_HEIGHT)) {
            final int row = ((int) mouseY - listY - LIST_PADDING_Y) / ROW_HEIGHT;
            final int index = this.scroll + row;

            if (index >= 0 && index < this.visibleDefinitions.size()) {
                this.selectedMultiblock = this.visibleDefinitions.get(index).id();
                this.updateButtonState();
                this.playButtonSound();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(
            final double mouseX,
            final double mouseY,
            final double horizontalAmount,
            final double verticalAmount
    ) {
        final int listX = this.leftPos + LIST_X;
        final int listY = this.topPos + LIST_Y;

        if (mouseIn(mouseX, mouseY, listX, listY, LIST_WIDTH, LIST_HEIGHT)) {
            this.scroll -= (int) Math.signum(verticalAmount);
            this.clampScroll();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private String displayName(final ResourceLocation id) {
        return id.getPath();
    }
}