/*
 * Copyright (c) 2021-2023 Team Galacticraft
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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.configuration.AccessLevel;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOFace;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneActivation;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.api.util.DisplayUtil;
import dev.galacticraft.machinelib.client.impl.model.MachineBakedModel;
import dev.galacticraft.machinelib.client.impl.util.DrawableUtil;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.compat.vanilla.StorageSlot;
import io.netty.buffer.ByteBufAllocator;
import lol.bai.badpackets.api.PacketSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.impl.transfer.context.PlayerContainerItemContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.galacticraft.machinelib.impl.Constant.TextureCoordinate.*;

/**
 * Handles most of the boilerplate code for machine screens.
 * Handles the rendering of tanks, configuration panels and capacitors.
 */
@Environment(EnvType.CLIENT)
public class MachineScreen<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> extends AbstractContainerScreen<Menu> {
    private static final ItemStack REDSTONE = new ItemStack(Items.REDSTONE);
    private static final ItemStack GUNPOWDER = new ItemStack(Items.GUNPOWDER);
    private static final ItemStack UNLIT_TORCH = new ItemStack(getOptionalItem(new ResourceLocation("galacticraft", "unlit_torch")));
    private static final ItemStack REDSTONE_TORCH = new ItemStack(Items.REDSTONE_TORCH);
    private static final ItemStack WRENCH = new ItemStack(getOptionalItem(new ResourceLocation("galacticraft", "standard_wrench")));
    private static final ItemStack ALUMINUM_WIRE = new ItemStack(getOptionalItem(new ResourceLocation("galacticraft", "aluminum_wire")));

    private static final int SPACING = 4;

    private static final int PANEL_ICON_X = 3;
    private static final int PANEL_ICON_Y = 3;

    private static final int PANEL_TITLE_X = 19;
    private static final int PANEL_TITLE_Y = 7;

    private static final int REDSTONE_IGNORE_X = 18;
    private static final int REDSTONE_IGNORE_Y = 30;

    private static final int REDSTONE_LOW_X = 43;
    private static final int REDSTONE_LOW_Y = 30;

    private static final int REDSTONE_HIGH_X = 68;
    private static final int REDSTONE_HIGH_Y = 30;

    private static final int SECURITY_PUBLIC_X = 18;
    private static final int SECURITY_PUBLIC_Y = 30;

    private static final int SECURITY_TEAM_X = 43;
    private static final int SECURITY_TEAM_Y = 30;

    private static final int SECURITY_PRIVATE_X = 68;
    private static final int SECURITY_PRIVATE_Y = 30;

    private static final int TOP_FACE_X = 33;
    private static final int TOP_FACE_Y = 26;

    private static final int LEFT_FACE_X = 52;
    private static final int LEFT_FACE_Y = 45;

    private static final int FRONT_FACE_X = 33;
    private static final int FRONT_FACE_Y = 45;

    private static final int RIGHT_FACE_X = 14;
    private static final int RIGHT_FACE_Y = 45;

    private static final int BACK_FACE_X = 71;
    private static final int BACK_FACE_Y = 45;

    private static final int BOTTOM_FACE_X = 33;
    private static final int BOTTOM_FACE_Y = 64;

    private static final int OWNER_FACE_X = 6;
    private static final int OWNER_FACE_Y = 20;

    private static final int REDSTONE_STATE_TEXT_X = 11;
    private static final int REDSTONE_STATE_TEXT_Y = 53;

    private static final int REDSTONE_STATUS_TEXT_X = 11;
    private static final int REDSTONE_STATUS_TEXT_Y = 57; //add font height

    private static final int SECURITY_STATE_TEXT_X = 11;
    private static final int SECURITY_STATE_TEXT_Y = 53;

    private static final int MACHINE_FACE_SIZE = 16;
    private static final int BUTTON_SIZE = 16;

    /**
     * An array used for ordering tooltip text to avoid re-allocating multiple times per frame.
     * Not thread safe.
     */
    @ApiStatus.Internal
    private static final List<Component> TOOLTIP_ARRAY = new ArrayList<>();

    /**
     * The texture of the background screen.
     */
    private final @NotNull ResourceLocation texture;
    /**
     * The tank that is currently hovered over.
     */
    protected @Nullable Tank focusedTank = null;
    /**
     * The x-position of the capacitor.
     */
    protected int capacitorX = 8;
    /**
     * The y-position of the capacitor.
     */
    protected int capacitorY = 8;
    /**
     * The height of the capacitor.
     */
    protected int capacitorHeight = 48;
    /**
     * The skin of the owner of this machine.
     * Defaults to steve if the skin cannot be found.
     */
    private @NotNull ResourceLocation ownerSkin = DefaultPlayerSkin.getDefaultSkin(UUID.randomUUID());

    private @Nullable MachineBakedModel model;

    /**
     * Creates a new screen from the given screen handler.
     *
     * @param handler The screen handler to create the screen from.
     * @param title   The title of the screen.
     * @param texture The texture of the background screen.
     */
    protected MachineScreen(@NotNull Menu handler, @NotNull Component title, @NotNull ResourceLocation texture) {
        super(handler, handler.playerInventory, title);

        this.texture = texture;

        Minecraft.getInstance().getSkinManager().registerSkins(new GameProfile(this.menu.configuration.getSecurity().getOwner(), this.menu.configuration.getSecurity().getUsername()), (type, skin, tex) -> {
            if (type == MinecraftProfileTexture.Type.SKIN && skin != null) {
                MachineScreen.this.ownerSkin = skin;
            }
        }, true);
    }

    /**
     * Returns the requested item based on the id, or defaults to a barrier if nto found.
     *
     * @param id the id of the item
     * @return the item stack
     */
    private static Item getOptionalItem(ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id).orElse(Items.BARRIER);
    }

    @Override
    protected void init() {
        super.init();
        assert this.minecraft != null;
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        if (this.minecraft.getModelManager().getBlockModelShaper().getBlockModel(this.menu.type.getBlock().defaultBlockState()) instanceof MachineBakedModel model) {
            this.model = model;
        }
    }

    /**
     * Appends additional information to the capacitor's tooltip.
     *
     * @param list The list to append to.
     */
    public void appendEnergyTooltip(List<Component> list) {
    }

    /**
     * Draws the configuration panels and their contents.
     *
     * @param graphics The gui graphics.
     * @param mouseX   The mouse's x-position.
     * @param mouseY   The mouse's y-position.
     * @param delta    The delta time.
     */
    protected void drawConfigurationPanels(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        assert this.minecraft != null;
        boolean secondary = false;
        PoseStack poseStack = graphics.pose();

        for (Tab tab : Tab.values()) { // 0, 1, 2, 3
            poseStack.pushPose();
            if (secondary) poseStack.translate(0, SPACING, 0);
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, this.leftPos + (tab.isLeft() ? tab.isOpen() ? -PANEL_WIDTH : -22 : this.imageWidth), this.topPos + (secondary ? Tab.values()[tab.ordinal() - 1].isOpen() ? PANEL_HEIGHT : TAB_HEIGHT : 0) + SPACING, tab.getU(), tab.getV(), tab.isOpen() ? PANEL_WIDTH : TAB_WIDTH, tab.isOpen() ? PANEL_HEIGHT : TAB_HEIGHT);
            secondary = !secondary;
            poseStack.popPose();
        }
        poseStack.pushPose();
        poseStack.translate(this.leftPos, this.topPos, 0);

        if (Tab.REDSTONE.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(-PANEL_WIDTH, SPACING, 0);
            this.drawButton(graphics, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, mouseX + PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, delta, menu.configuration.getRedstoneActivation() == RedstoneActivation.IGNORE);
            this.drawButton(graphics, REDSTONE_LOW_X, REDSTONE_LOW_Y, mouseX + PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, delta, menu.configuration.getRedstoneActivation() == RedstoneActivation.LOW);
            this.drawButton(graphics, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, mouseX + PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, delta, menu.configuration.getRedstoneActivation() == RedstoneActivation.HIGH);
            this.renderItemIcon(graphics, PANEL_ICON_X, PANEL_ICON_Y, REDSTONE);
            this.renderItemIcon(graphics, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, GUNPOWDER);
            this.renderItemIcon(graphics, REDSTONE_LOW_X, REDSTONE_LOW_Y - 2, UNLIT_TORCH);
            this.renderItemIcon(graphics, REDSTONE_HIGH_X, REDSTONE_HIGH_Y - 2, REDSTONE_TORCH);

            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_ACTIVATION)
                    .setStyle(Constant.Text.GRAY_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_STATE,
                    menu.configuration.getRedstoneActivation().getName()).setStyle(Constant.Text.DARK_GRAY_STYLE), REDSTONE_STATE_TEXT_X, REDSTONE_STATE_TEXT_Y, 0xFFFFFFFF);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_STATUS,
                            this.menu.configuration.getRedstoneActivation().isActive(() -> this.menu.levelAccess.evaluate(Level::hasNeighborSignal).orElse(false)) ?
                                    Component.translatable(Constant.TranslationKey.REDSTONE_ACTIVE).setStyle(Constant.Text.GREEN_STYLE)
                                    : Component.translatable(Constant.TranslationKey.REDSTONE_DISABLED).setStyle(Constant.Text.DARK_RED_STYLE))
                    .setStyle(Constant.Text.DARK_GRAY_STYLE), REDSTONE_STATUS_TEXT_X, REDSTONE_STATUS_TEXT_Y + this.font.lineHeight, 0xFFFFFFFF);

            poseStack.popPose();
        }
        if (Tab.CONFIGURATION.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(-PANEL_WIDTH, TAB_HEIGHT + SPACING + SPACING, 0);
            this.renderItemIcon(graphics, PANEL_ICON_X, PANEL_ICON_Y, WRENCH);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.CONFIGURATION)
                    .setStyle(Constant.Text.GRAY_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);

            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            this.drawMachineFace(graphics, TOP_FACE_X, TOP_FACE_Y, this.menu.machine, BlockFace.TOP);
            this.drawMachineFace(graphics, LEFT_FACE_X, LEFT_FACE_Y, this.menu.machine, BlockFace.LEFT);
            this.drawMachineFace(graphics, FRONT_FACE_X, FRONT_FACE_Y, this.menu.machine, BlockFace.FRONT);
            this.drawMachineFace(graphics, RIGHT_FACE_X, RIGHT_FACE_Y, this.menu.machine, BlockFace.RIGHT);
            this.drawMachineFace(graphics, BACK_FACE_X, BACK_FACE_Y, this.menu.machine, BlockFace.BACK);
            this.drawMachineFace(graphics, BOTTOM_FACE_X, BOTTOM_FACE_Y, this.menu.machine, BlockFace.BOTTOM);
            poseStack.popPose();
        }
        if (Tab.STATS.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(this.imageWidth, SPACING, 0);
            this.renderItemIcon(graphics, PANEL_ICON_X, PANEL_ICON_Y, ALUMINUM_WIRE);
            graphics.blit(this.ownerSkin, OWNER_FACE_X, OWNER_FACE_Y, OWNER_FACE_WIDTH, OWNER_FACE_HEIGHT, 8, 8, 8, 8, 64, 64);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.STATISTICS)
                    .setStyle(Constant.Text.GREEN_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);
            List<FormattedCharSequence> text = this.font.split(this.menu.type.getBlock().getName(), 64);
            int offsetY = 0;
            for (FormattedCharSequence orderedText : text) {
                graphics.drawString(this.font, orderedText, 40, 22 + offsetY, 0xFFFFFFFF, false);
                offsetY += this.font.lineHeight + 2;
            }
//                this.textRenderer.draw(matrices, Text.translatable("ui.galacticraft.machine.stats.gjt", "N/A")
//                        .setStyle(Constants.Text.GRAY_STYLE), 11, 54, ColorUtils.WHITE);
            //                this.textRenderer.draw(matrices, Text.translatable("ui.galacticraft.machine.stats.todo", "N/A")
//                        .setStyle(Constants.Text.GRAY_STYLE), 11, 54, ColorUtils.WHITE);
            poseStack.popPose();
        }

        if (Tab.SECURITY.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(this.imageWidth, TAB_HEIGHT + SPACING + SPACING, 0);
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, PANEL_ICON_X, PANEL_ICON_Y, ICON_LOCK_PRIVATE_U, ICON_LOCK_PRIVATE_V, ICON_WIDTH, ICON_HEIGHT);

            this.drawButton(graphics, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (TAB_HEIGHT + SPACING + SPACING) - this.topPos, delta, this.menu.configuration.getSecurity().getAccessLevel() == AccessLevel.PUBLIC || !this.menu.configuration.getSecurity().isOwner(this.menu.playerUUID));
            this.drawButton(graphics, SECURITY_TEAM_X, SECURITY_TEAM_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (TAB_HEIGHT + SPACING + SPACING) - this.topPos, delta, this.menu.configuration.getSecurity().getAccessLevel() == AccessLevel.TEAM || !this.menu.configuration.getSecurity().isOwner(this.menu.playerUUID));
            this.drawButton(graphics, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (TAB_HEIGHT + SPACING + SPACING) - this.topPos, delta, this.menu.configuration.getSecurity().getAccessLevel() == AccessLevel.PRIVATE || !this.menu.configuration.getSecurity().isOwner(this.menu.playerUUID));
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, ICON_LOCK_PRIVATE_U, ICON_LOCK_PRIVATE_V, ICON_WIDTH, ICON_HEIGHT);
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, SECURITY_TEAM_X, SECURITY_TEAM_Y, ICON_LOCK_PARTY_U, ICON_LOCK_PARTY_V, ICON_WIDTH, ICON_HEIGHT);
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, ICON_LOCK_PUBLIC_U, ICON_LOCK_PUBLIC_V, ICON_WIDTH, ICON_HEIGHT);

            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.SECURITY)
                    .setStyle(Constant.Text.GRAY_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.ACCESS_LEVEL,
                    this.menu.configuration.getSecurity().getAccessLevel().getName()).setStyle(Constant.Text.GRAY_STYLE), SECURITY_STATE_TEXT_X, SECURITY_STATE_TEXT_Y, 0xFFFFFFFF);
//                assert this.menu.configuration.getSecurity().getOwner() != null;
//                this.textRenderer.drawWithShadow(matrices, Text.translatable("ui.galacticraft.machine.security.owned_by", this.menu.configuration.getSecurity().getOwner().getName())
//                        .setStyle(Constants.Text.GRAY_STYLE), SECURITY_STATE_TEXT_X, SECURITY_STATE_TEXT_Y + this.textRenderer.fontHeight + 4, ColorUtils.WHITE);

            poseStack.popPose();
        }
        poseStack.popPose();
    }

    /**
     * Draws the title of the machine.
     *
     * @param graphics the gui graphics
     * @see #titleLabelX
     * @see #titleLabelY
     */
    protected void drawTitle(@NotNull GuiGraphics graphics) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFFFF, false);
    }

    /**
     * Draws the sprite of a given machine face.
     *
     * @param graphics the gui graphics
     * @param x        the x position to draw at
     * @param y        the y position to draw at
     * @param machine  the machine to draw
     * @param face     the face to draw
     */
    private void drawMachineFace(@NotNull GuiGraphics graphics, int x, int y, @NotNull MachineBlockEntity machine, @NotNull BlockFace face) {
        MachineIOFace machineFace = menu.configuration.getIOConfiguration().get(face);
        if (this.model != null) {
            graphics.blit(x, y, 0, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE, model.getSprite(face, machine, null, machineFace.getType(), machineFace.getFlow()));
        }
    }

    /**
     * Renders the icon of the given item, without any extra effects.
     *
     * @param graphics the gui graphics
     * @param x        the x position to draw at
     * @param y        the y position to draw at
     * @param stack    the item to render
     */
    private void renderItemIcon(@NotNull GuiGraphics graphics, int x, int y, @NotNull ItemStack stack) {
        assert this.minecraft != null;
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, this.menu.player, 8910823);
        graphics.pose().pushPose();
        this.minecraft.getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        graphics.pose().translate(x + 8, y + 8, 100.0F);
        graphics.pose().scale(16, -16, 16);
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean bl = !model.usesBlockLight();
        if (bl) {
            Lighting.setupForFlatItems();
        }

        Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.GUI, false, graphics.pose(), immediate, 15728880, OverlayTexture.NO_OVERLAY, model);
        immediate.endBatch();
        if (bl) {
            Lighting.setupFor3DItems();
        }
        graphics.pose().popPose();
    }

    /**
     * Draws a 16x16 button at the given position.
     * The button will be highlighted if the mouse is hovering over it.
     *
     * @param graphics the gui graphics instance
     * @param x        the x-position to draw at
     * @param y        the y-position to draw at
     * @param mouseX   the mouse's x-position
     * @param mouseY   the mouse's y-position
     * @param delta    the delta time
     * @param pressed  whether the button is pressed
     */
    public void drawButton(GuiGraphics graphics, int x, int y, double mouseX, double mouseY, float delta, boolean pressed) {
        assert this.minecraft != null;
        if (pressed) {
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, x, y, BUTTON_U, BUTTON_PRESSED_V, BUTTON_WIDTH, BUTTON_HEIGHT);
            return;
        }
        if (DrawableUtil.isWithin(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, x, y, BUTTON_U, BUTTON_HOVERED_V, BUTTON_WIDTH, BUTTON_HEIGHT);
        } else {
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, x, y, BUTTON_U, BUTTON_V, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    /**
     * Handles mouse input for the configuration panels.
     *
     * @param mouseX the mouse's x-position
     * @param mouseY the mouse's y-position
     * @param button the button code that was pressed
     * @return whether the button was handled
     * @see GLFW
     */
    public boolean checkConfigurationPanelClick(double mouseX, double mouseY, int button) {
        assert this.minecraft != null;

        final double mX = mouseX, mY = mouseY;
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.REDSTONE.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.REDSTONE.click();
                return true;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                this.setRedstone(RedstoneActivation.IGNORE);
                this.playButtonSound();
                return true;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                this.setRedstone(RedstoneActivation.LOW);
                this.playButtonSound();
                return true;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                this.setRedstone(RedstoneActivation.HIGH);
                this.playButtonSound();
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_HEIGHT)) {
                    return true;
                }
            }
        } else {
            mouseX += TAB_WIDTH;
            mouseY -= SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.REDSTONE.click();
                return true;
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= TAB_HEIGHT + SPACING + SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.CONFIGURATION.click();
                return true;
            }
            if (button >= GLFW.GLFW_MOUSE_BUTTON_LEFT && button <= GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                if (DrawableUtil.isWithin(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.TOP);
                    return true;
                } else if (DrawableUtil.isWithin(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.LEFT);
                    return true;
                } else if (DrawableUtil.isWithin(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.FRONT);
                    return true;
                } else if (DrawableUtil.isWithin(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.RIGHT);
                    return true;
                } else if (DrawableUtil.isWithin(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.BACK);
                    return true;
                } else if (DrawableUtil.isWithin(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.BOTTOM);
                    return true;
                }
            }
        } else {
            mouseX += TAB_WIDTH;
            if (Tab.REDSTONE.isOpen()) {
                mouseY -= PANEL_HEIGHT + SPACING + SPACING;
            } else {
                mouseY -= TAB_HEIGHT + SPACING + SPACING;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.CONFIGURATION.click();
                return true;
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        mouseX -= this.imageWidth;
        mouseY -= SPACING;
        if (Tab.STATS.isOpen()) {
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.STATS.click();
                return true;
            }
        } else {
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.STATS.click();
                return true;
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        mouseX -= this.imageWidth;
        if (Tab.SECURITY.isOpen()) {
            mouseY -= TAB_HEIGHT + SPACING + SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.SECURITY.click();
                return true;
            }

            if (this.menu.configuration.getSecurity().isOwner(this.menu.playerUUID)) {
                if (DrawableUtil.isWithin(mouseX, mouseY, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    this.setAccessibility(AccessLevel.PRIVATE);
                    this.playButtonSound();
                    return true;
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, SECURITY_TEAM_X, SECURITY_TEAM_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    this.setAccessibility(AccessLevel.TEAM);
                    this.playButtonSound();
                    return true;
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    this.setAccessibility(AccessLevel.PUBLIC);
                    this.playButtonSound();
                    return true;
                }
            }
        } else {
            if (Tab.STATS.isOpen()) {
                mouseY -= PANEL_HEIGHT + SPACING + SPACING;
            } else {
                mouseY -= TAB_HEIGHT + SPACING + SPACING;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.SECURITY.click();
            }
        }
        return false;
    }

    /**
     * Sets the accessibility of the machine and syncs it to the server.
     *
     * @param accessLevel The accessibility to set.
     */
    protected void setAccessibility(@NotNull AccessLevel accessLevel) {
        this.menu.configuration.getSecurity().setAccessLevel(accessLevel);
        PacketSender.c2s().send(Constant.id("security_config"), new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(1, 1).writeByte(accessLevel.ordinal())));
    }

    /**
     * Sets the redstone mode of the machine and syncs it to the server.
     *
     * @param redstone The redstone mode to set.
     */
    protected void setRedstone(@NotNull RedstoneActivation redstone) {
        this.menu.configuration.setRedstoneActivation(redstone);
        PacketSender.c2s().send(Constant.id("redstone_config"), new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(1, 1).writeByte(redstone.ordinal())));
    }

    /**
     * Draws the tooltips of the configuration panel.
     *
     * @param graphics The gui graphics to use.
     * @param mouseX   The mouse's x-position.
     * @param mouseY   The mouse's y-position.
     */
    protected void drawConfigurationPanelTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        final int mX = mouseX, mY = mouseY;
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.REDSTONE.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                graphics.renderTooltip(this.font, RedstoneActivation.IGNORE.getName(), mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                graphics.renderTooltip(this.font, RedstoneActivation.LOW.getName(), mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                graphics.renderTooltip(this.font, RedstoneActivation.HIGH.getName(), mX, mY);
            }
        } else {
            mouseX += TAB_WIDTH;
            mouseY -= SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_ACTIVATION).setStyle(Constant.Text.RED_STYLE), mX, mY);
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= TAB_HEIGHT + SPACING + SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.TOP, mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.LEFT, mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.FRONT, mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.RIGHT, mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.BACK, mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.BOTTOM, mX, mY);
            }
        } else {
            mouseX += TAB_WIDTH;
            if (Tab.REDSTONE.isOpen()) {
                mouseY -= PANEL_HEIGHT + SPACING;
            } else {
                mouseY -= TAB_HEIGHT + SPACING;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.CONFIGURATION).setStyle(Constant.Text.BLUE_STYLE), mX, mY);
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        mouseX -= this.imageWidth;
        mouseY -= SPACING;
        if (Tab.STATS.isOpen()) {
            if (this.menu.configuration.getSecurity().getUsername() != null) {
                if (DrawableUtil.isWithin(mouseX, mouseY, OWNER_FACE_X, OWNER_FACE_Y, OWNER_FACE_WIDTH, OWNER_FACE_HEIGHT)) {
                    assert this.menu.configuration.getSecurity().getOwner() != null;
                    graphics.renderTooltip(this.font, Component.literal(this.menu.configuration.getSecurity().getUsername()), mX, mY);
                }
            }
        } else {
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.STATISTICS).setStyle(Constant.Text.YELLOW_STYLE), mX, mY);
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.SECURITY.isOpen()) {
            mouseX -= this.imageWidth;
            mouseY -= TAB_HEIGHT + SPACING + SPACING;

            if (this.menu.configuration.getSecurity().isOwner(this.menu.playerUUID)) {
                if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    graphics.renderTooltip(this.font, AccessLevel.PRIVATE.getName(), mX, mY);
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    graphics.renderTooltip(this.font, AccessLevel.TEAM.getName(), mX, mY);
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    graphics.renderTooltip(this.font, AccessLevel.PUBLIC.getName(), mX, mY);
                }
            } else {
                if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                        || DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                        || DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.ACCESS_DENIED), mX, mY);
                }
            }
        } else {
            mouseX -= this.imageWidth;
            if (Tab.STATS.isOpen()) {
                mouseY -= PANEL_HEIGHT + SPACING + SPACING;
            } else {
                mouseY -= TAB_HEIGHT + SPACING + SPACING;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.SECURITY).setStyle(Constant.Text.BLUE_STYLE), mX, mY);
            }
        }
    }

    /**
     * Renders the tooltip for the given face.
     *
     * @param graphics The gui graphics
     * @param face     The face to render the tooltip for
     * @param mouseX   The mouse's x-position
     * @param mouseY   The mouse's y-position
     */
    protected void renderFaceTooltip(GuiGraphics graphics, @NotNull BlockFace face, int mouseX, int mouseY) {
        TOOLTIP_ARRAY.add(face.getName());
        MachineIOFace configuredFace = this.menu.configuration.getIOConfiguration().get(face);
        if (configuredFace.getType() != ResourceType.NONE) {
            TOOLTIP_ARRAY.add(configuredFace.getType().getName().copy().append(" ").append(configuredFace.getFlow().getName()));
        }
        graphics.renderComponentTooltip(this.font, TOOLTIP_ARRAY, mouseX, mouseY);

        TOOLTIP_ARRAY.clear();
    }

    @Override
    public final void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        assert this.minecraft != null;

        super.render(graphics, mouseX, mouseY, delta);

        this.renderForeground(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * Renders the foreground of the screen.
     *
     * @param graphics The gui graphics
     * @param mouseX   The mouse's x-position
     * @param mouseY   The mouse's y-position
     * @param delta    The delta time
     */
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected final void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        this.renderBackground(graphics);

        graphics.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        this.renderBackground(graphics, mouseX, mouseY, delta);
        this.drawTanks(graphics, mouseX, mouseY, delta);
        this.drawCapacitor(graphics, mouseX, mouseY, delta);
        this.handleSlotHighlight(graphics, mouseX, mouseY);
        this.drawConfigurationPanels(graphics, mouseX, mouseY, delta);
    }

    /**
     * Draws the capacitor of this machine.
     * If the machine has no capacitor, this method does nothing.
     *
     * @param graphics The gui graphics
     * @param mouseX   The mouse's x-position
     * @param mouseY   The mouse's y-position
     * @param delta    The delta time
     */
    protected void drawCapacitor(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        long capacity = this.menu.energyStorage.getCapacity();
        if (capacity > 0 && this.capacitorHeight != 0) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, Constant.ScreenTexture.OVERLAY_BARS);
            DrawableUtil.drawProgressTexture(graphics.pose(), this.leftPos + this.capacitorX, this.topPos + this.capacitorY, 0.01f, ENERGY_BACKGROUND_X, ENERGY_BACKGROUND_Y, OVERLAY_WIDTH, OVERLAY_HEIGHT, OVERLAY_TEX_WIDTH, OVERLAY_TEX_HEIGHT);
            long amount = this.menu.energyStorage.getAmount();
            float scale = (float) ((double) amount / (double) capacity);
            DrawableUtil.drawProgressTexture(graphics.pose(), this.leftPos + this.capacitorX, (this.topPos + this.capacitorY + this.capacitorHeight - (this.capacitorHeight * scale)), 0.02f, ENERGY_X, ENERGY_Y, OVERLAY_WIDTH, OVERLAY_HEIGHT * scale, OVERLAY_TEX_WIDTH, OVERLAY_TEX_HEIGHT);

            if (DrawableUtil.isWithin(mouseX, mouseY, this.leftPos + this.capacitorX, this.topPos + this.capacitorY, 16, this.capacitorHeight)) {
                List<Component> lines = new ArrayList<>();
                MachineStatus status = this.menu.configuration.getStatus();
                lines.add(Component.translatable(Constant.TranslationKey.STATUS).setStyle(Constant.Text.GRAY_STYLE).append(status.getName()));
                lines.add(Component.translatable(Constant.TranslationKey.CURRENT_ENERGY, DisplayUtil.formatEnergy(amount).setStyle(Style.EMPTY.withColor(DisplayUtil.colorScale(amount, capacity))), DisplayUtil.formatEnergy(capacity).setStyle(Constant.Text.GRAY_STYLE).setStyle(Constant.Text.LIGHT_PURPLE_STYLE)));
                this.appendEnergyTooltip(lines);

                assert this.minecraft != null;
                assert this.minecraft.screen != null;
                graphics.pose().translate(0.0D, 0.0D, 1.0D);
                graphics.renderComponentTooltip(this.minecraft.font, lines, mouseX, mouseY);
                graphics.pose().translate(0.0D, 0.0D, -1.0D);
            }
        }
    }

    /**
     * Draws the background of this machine.
     *
     * @param graphics The gui graphics
     * @param mouseX   The mouse's x-position
     * @param mouseY   The mouse's y-position
     * @param delta    The delta time
     */
    protected void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    }

    /**
     * Draws the (fluid and gas) tanks of this machine.
     *
     * @param graphics The gui graphics
     * @param mouseX   The mouse's x-position
     * @param mouseY   The mouse's y-position
     * @param delta    The delta time
     */
    protected void drawTanks(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        assert this.minecraft != null;

        graphics.pose().pushPose();
        graphics.pose().translate(this.leftPos, this.topPos, 0);
        this.focusedTank = null;
        for (Tank tank : this.menu.tanks) {
            if (tank.getHeight() > 0) {
                graphics.fill(tank.getX(), tank.getY(), tank.getX() + tank.getWidth(), tank.getY() + tank.getHeight(), 0xFF8B8B8B);

                if (tank.getAmount() > 0) {
                    FluidVariant resource = tank.createVariant();
                    if (resource.isBlank()) continue;
                    boolean fillFromTop = FluidVariantAttributes.isLighterThanAir(resource);
                    TextureAtlasSprite sprite = FluidVariantRendering.getSprite(resource);
                    int fluidColor = FluidVariantRendering.getColor(resource);

                    if (sprite == null || sprite.atlasLocation().equals(MissingTextureAtlasSprite.getLocation())) {
                        sprite = FluidVariantRendering.getSprite(FluidVariant.of(Fluids.WATER));
                        fluidColor = -1;
                        if (sprite == null) throw new IllegalStateException("Water sprite is null");
                    }

                    double v = (1.0 - ((double) tank.getAmount() / (double) tank.getCapacity()));
                    int airHeight = (int) (v * tank.getHeight());
                    int fluidHeight = tank.getHeight() - airHeight;

                    if (!fillFromTop) {
                        graphics.blit(tank.getX(), tank.getY() + airHeight, 0, tank.getWidth(), fluidHeight, sprite, FastColor.ARGB32.red(fluidColor) / 255.0f, FastColor.ARGB32.green(fluidColor) / 255.0f, FastColor.ARGB32.alpha(fluidColor) / 255.0f, 1.0f);
                    } else {
                        graphics.blit(tank.getX(), tank.getY(), 0, tank.getWidth(), fluidHeight, sprite, FastColor.ARGB32.red(fluidColor) / 255.0f, FastColor.ARGB32.green(fluidColor) / 255.0f, FastColor.ARGB32.alpha(fluidColor) / 255.0f, 1.0f);
                    }
                }

                boolean shorten = true;
                for (int y = tank.getY() + tank.getHeight() - 2; y > tank.getY(); y -= 3) {
                    graphics.fill(tank.getX(), y, tank.getX() + (tank.getWidth() / 2) + ((shorten = !shorten) ? -(tank.getWidth() / 8) : 0), y - 1, 0xFFB31212);
                }

                if (this.focusedTank == null && DrawableUtil.isWithin(mouseX, mouseY, this.leftPos + tank.getX(), this.topPos + tank.getY(), tank.getWidth(), tank.getHeight())) {
                    this.focusedTank = tank;
                    RenderSystem.disableDepthTest();
                    graphics.fill(tank.getX(), tank.getY(), tank.getX() + tank.getWidth(), tank.getY() + tank.getHeight(), 0x70ffffff);
                    RenderSystem.enableDepthTest();
                }
            }
        }
        graphics.pose().popPose();

        for (Tank tank : this.menu.tanks) {
            tank.drawTooltip(graphics, this.minecraft, this.leftPos, this.topPos, mouseX, mouseY);
        }
    }

    @ApiStatus.Internal
    private void handleSlotHighlight(GuiGraphics graphics, int mouseX, int mouseY) {
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX -= (this.leftPos - PANEL_WIDTH);
            mouseY -= (this.topPos + (TAB_HEIGHT + SPACING + SPACING));
            MachineIOFace config = null;
            if (DrawableUtil.isWithin(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.getIOConfiguration().get(BlockFace.TOP);
            } else if (DrawableUtil.isWithin(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.getIOConfiguration().get(BlockFace.LEFT);
            } else if (DrawableUtil.isWithin(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.getIOConfiguration().get(BlockFace.FRONT);
            } else if (DrawableUtil.isWithin(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.getIOConfiguration().get(BlockFace.RIGHT);
            } else if (DrawableUtil.isWithin(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.getIOConfiguration().get(BlockFace.BACK);
            } else if (DrawableUtil.isWithin(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.getIOConfiguration().get(BlockFace.BOTTOM);
            }
            if (config != null && config.getType().willAcceptResource(ResourceType.ITEM)) {
                for (StorageSlot slot : this.menu.machineSlots) {
                    InputType type = slot.getSlot().inputType();
                    if (type.getExternalFlow() != null && type.getExternalFlow().canFlowIn(config.getFlow())) {
                        this.drawSlotOverlay(graphics, slot.x, slot.y, type.colour());
                    }
                }
            }
        }
    }

    /**
     * Draws a coloured box around the slot, based on the slot's type.
     *
     * @param graphics the gui graphics
     */
    protected void drawSlotOutline(@NotNull GuiGraphics graphics, int x, int y, int color) {
        graphics.renderOutline(x - 1, y - 1, x + 17, y + 17, color | (0xFF << 24));
    }

    protected void drawSlotOverlay(@NotNull GuiGraphics graphics, int x, int y, int color) {
        color <<= 8;
        color >>= 8;
        color |= (0x70 << 24);
        graphics.fill(this.leftPos + x, this.topPos + y, this.leftPos + x + 16, this.topPos + y + 16, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean tankMod = false;
        if (this.focusedTank != null && button == 0) {
            tankMod = this.focusedTank.acceptStack(new PlayerContainerItemContext(this.menu.playerInventory.player, PlayerInventoryStorage.getCursorStorage(this.menu)));
            if (tankMod) {
                FriendlyByteBuf buf = new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(Integer.BYTES * 2)).writeVarInt(this.menu.containerId);
                buf.writeInt(this.focusedTank.getId());
                PacketSender.c2s().send(Constant.id("tank_modify"), buf);
            }
        }
        return this.checkConfigurationPanelClick(mouseX, mouseY, button) | super.mouseClicked(mouseX, mouseY, button) | tankMod;
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        this.drawConfigurationPanelTooltips(graphics, mouseX, mouseY);
    }

    /**
     * Plays a button click sound.
     */
    private void playButtonSound() {
        assert this.minecraft != null;
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    protected final void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        this.drawTitle(graphics);
    }

    /**
     * Returns the x offset of the screen.
     *
     * @return the x offset of the screen
     */
    public int getX() {
        return this.leftPos;
    }

    /**
     * Returns the y offset of the screen.
     *
     * @return the y offset of the screen
     */
    public int getY() {
        return this.topPos;
    }

    /**
     * Returns the width of the background image.
     *
     * @return the width of the screen
     */
    public int getImageWidth() {
        return this.imageWidth;
    }

    /**
     * Returns the height of the background image.
     *
     * @return the height of the screen
     */
    public int getImageHeight() {
        return this.imageHeight;
    }

    private void modifyFace(int button, BlockFace face) {
        if (button == 0) {
            cycleFace(face, Screen.hasShiftDown(), Screen.hasControlDown());
        }
        this.playButtonSound();
    }

    private void cycleFace(BlockFace face, boolean reverse, boolean reset) {
        MachineIOFace option = this.menu.configuration.getIOConfiguration().get(face);
        if (reset) {
            option.setOption(ResourceType.NONE, ResourceFlow.BOTH);
            PacketSender.c2s().send(Constant.id("reset_face"),
                    new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(2, 2)
                            .writeByte(face.ordinal())
                            .writeBoolean(true)
                    )
            );
            return;
        }

        // Format: NONE, any [any][out][in], fluid [any][out][in], item [any][out][in], energy [any][out][in]
        short bits = 0b0_000_000_000_000;

        for (FluidResourceSlot slot : this.menu.fluidStorage) {
            InputType inputType = slot.inputType();
            if (inputType.externalInsertion()) bits |= 0b001;
            if (inputType.externalExtraction()) bits |= 0b010;
            if ((bits & 0b011) == 0b011) break;
        }
        if ((bits & 0b011) == 0b011) bits |= 0b100;
        bits <<= 3;

        for (ItemResourceSlot slot : this.menu.itemStorage) {
            InputType inputType = slot.inputType();
            if (inputType.externalInsertion()) bits |= 0b001;
            if (inputType.externalExtraction()) bits |= 0b010;
            if ((bits & 0b011) == 0b011) break;
        }
        if ((bits & 0b011) == 0b011) bits |= 0b100;
        bits <<= 3;

        if (this.menu.energyStorage.canExposedInsert()) bits |= 0b001;
        if (this.menu.energyStorage.canExposedExtract()) bits |= 0b010;
        if ((bits & 0b011) == 0b011) bits |= 0b100;

        if (Integer.bitCount(bits & 0b000_001_001_001) > 1) {
            bits |= 0b001_000_000_000;
        }
        if (Integer.bitCount(bits & 0b000_010_010_010) > 1) {
            bits |= 0b010_000_000_000;
        }
        if (Integer.bitCount(bits & 0b000_100_100_100) > 1) {
            bits |= 0b100_000_000_000;
        }

        bits |= 0b1_000_000_000_000; //set NONE bit

        if (bits != 0b1_000_000_000_000) {
            ResourceType type = option.getType();
            ResourceFlow flow = option.getFlow();
            int index = switch (type) {
                case NONE -> 12;
                case ENERGY, ITEM, FLUID, ANY -> (type.ordinal() - 1) * 3 + flow.ordinal();
            };
            int i = index + (reverse ? -1 : 1);
            while (i != index) {
                if (i == -1) {
                    i = 12;
                } else if (i == 13) {
                    i = 0;
                }

                if ((bits >>> i & 0b1) != 0) {
                    break;
                }
                if (reverse) {
                    i--;
                } else {
                    i++;
                }
            }
            if (i == 12) {
                option.setOption(ResourceType.NONE, ResourceFlow.BOTH);
                PacketSender.c2s().send(Constant.id("reset_face"),
                        new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(2, 2)
                                .writeByte(face.ordinal())
                                .writeBoolean(true)
                        )
                );
            } else {
                byte iflow = (byte) (i % 3);
                byte itype = (byte) ((i - iflow) / 3 + 1);
                type = ResourceType.getFromOrdinal(itype);
                flow = ResourceFlow.getFromOrdinal(iflow);
                option.setOption(type, flow);
                PacketSender.c2s().send(Constant.id("face_type"),
                        new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(3, 3)
                                .writeByte(face.ordinal())
                                .writeByte(itype)
                                .writeByte(iflow)
                        )
                );
            }
        } else {
            option.setOption(ResourceType.NONE, ResourceFlow.BOTH);
            PacketSender.c2s().send(Constant.id("reset_face"),
                    new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(2, 2)
                            .writeByte(face.ordinal())
                            .writeBoolean(true)
                    )
            );
        }
    }

    /**
     * The four different types of configuration panel.
     */
    public enum Tab {
        REDSTONE(TAB_REDSTONE_U, TAB_REDSTONE_V, PANEL_REDSTONE_U, PANEL_REDSTONE_V, true),
        CONFIGURATION(TAB_CONFIG_U, TAB_CONFIG_V, PANEL_CONFIG_U, PANEL_CONFIG_V, true),
        STATS(TAB_STATS_U, TAB_STATS_V, PANEL_STATS_U, PANEL_STATS_V, false),
        SECURITY(TAB_SECURITY_U, TAB_SECURITY_V, PANEL_SECURITY_U, PANEL_SECURITY_V, false);

        private final int tabU;
        private final int tabV;
        private final int panelU;
        private final int panelV;
        private final boolean left;
        private boolean open = false;

        Tab(int tabU, int tabV, int panelU, int panelV, boolean left) {
            this.tabU = tabU;
            this.tabV = tabV;
            this.panelU = panelU;
            this.panelV = panelV;
            this.left = left;
        }

        public int getU() {
            return open ? this.panelU : this.tabU;
        }

        public boolean isLeft() {
            return left;
        }

        public int getV() {
            return open ? this.panelV : this.tabV;
        }

        public boolean isOpen() {
            return open;
        }

        public void click() {
            this.open = !this.open;
            if (this.open) {
                Tab.values()[this.ordinal() + 1 - this.ordinal() % 2 * 2].open = false;
            }
        }
    }
}
