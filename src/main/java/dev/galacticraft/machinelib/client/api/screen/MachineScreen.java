/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.AccessLevel;
import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.menu.Tank;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.api.util.DisplayUtil;
import dev.galacticraft.machinelib.client.api.util.GraphicsUtil;
import dev.galacticraft.machinelib.client.impl.model.MachineBakedModel;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.compat.vanilla.StorageSlot;
import dev.galacticraft.machinelib.impl.network.c2s.AccessLevelPayload;
import dev.galacticraft.machinelib.impl.network.c2s.RedstoneModePayload;
import dev.galacticraft.machinelib.impl.network.c2s.SideConfigurationClickPayload;
import dev.galacticraft.machinelib.impl.network.c2s.TankInteractionPayload;
import lol.bai.badpackets.api.PacketSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static dev.galacticraft.machinelib.impl.Constant.TextureCoordinate.*;

/**
 * Handles most of the boilerplate code for machine screens.
 * Handles the rendering of tanks, configuration panels and capacitors.
 *
 * @param <Machine> The type of machine block entity.
 * @param <Menu> The type of machine menu.
 */
@Environment(EnvType.CLIENT)
public class MachineScreen<Machine extends MachineBlockEntity, Menu extends MachineMenu<Machine>> extends AbstractContainerScreen<Menu> {
    private static final ItemStack REDSTONE = new ItemStack(Items.REDSTONE);
    private static final ItemStack GUNPOWDER = new ItemStack(Items.GUNPOWDER);
    private static final ItemStack UNLIT_TORCH = new ItemStack(getOptionalItem(ResourceLocation.fromNamespaceAndPath("galacticraft", "unlit_torch"), Items.TORCH));
    private static final ItemStack REDSTONE_TORCH = new ItemStack(Items.REDSTONE_TORCH);
    private static final ItemStack WRENCH = new ItemStack(getOptionalItem(ResourceLocation.fromNamespaceAndPath("galacticraft", "standard_wrench"), Items.HOPPER));
    private static final ItemStack ALUMINUM_WIRE = new ItemStack(getOptionalItem(ResourceLocation.fromNamespaceAndPath("galacticraft", "aluminum_wire"), Items.MOJANG_BANNER_PATTERN));
    private static final ItemStack IRON_CHESTPLATE = new ItemStack(Items.IRON_CHESTPLATE);

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
     * The skin of the owner of this machine.
     * Defaults to steve if the skin cannot be found.
     */
    private final @NotNull CompletableFuture<PlayerSkin> ownerSkin;
    private final @NotNull CompletableFuture<GameProfile> owner;
    /**
     * The tank that is currently hovered over.
     */
    public @Nullable Tank hoveredTank = null;
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
    private @Nullable MachineBakedModel model;
    private BlockState previousState;

    /**
     * Creates a new screen from the given screen handler.
     *
     * @param menu The screen handler to create the screen from.
     * @param title The title of the screen.
     * @param texture The texture of the background screen.
     */
    protected MachineScreen(@NotNull Menu menu, @NotNull Component title, @NotNull ResourceLocation texture) {
        super(menu, menu.playerInventory, title);

        this.texture = texture;

        UUID owner = this.menu.security.getOwner() == null ? this.menu.player.getUUID() : this.menu.security.getOwner();
        this.owner = SkullBlockEntity.fetchGameProfile(owner).thenApply(o -> o.orElse(new GameProfile(owner, "???")));
        this.ownerSkin = this.owner.thenCompose(profile -> Minecraft.getInstance().getSkinManager().getOrLoad(profile));
    }

    /**
     * {@return the item requested item, or the fallback item if the item is unavailable}
     *
     * @param id the id of the item
     * @param fallback the fallback item
     */
    private static Item getOptionalItem(ResourceLocation id, Item fallback) {
        return BuiltInRegistries.ITEM.getOptional(id).orElse(fallback);
    }

    private static boolean mouseIn(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    @Override
    protected void init() {
        super.init();
        assert this.minecraft != null;
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        if (this.minecraft.getModelManager().getBlockModelShaper().getBlockModel(this.menu.be.getBlockState()) instanceof MachineBakedModel model) {
            this.previousState = this.menu.be.getBlockState();
            this.model = model;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!this.menu.be.getBlockState().equals(this.previousState)) {
            this.previousState = this.menu.be.getBlockState();
            if (this.minecraft.getModelManager().getBlockModelShaper().getBlockModel(this.menu.be.getBlockState()) instanceof MachineBakedModel model) {
                this.model = model;
            }
        }
    }

    /**
     * Appends additional information to the capacitor's tooltip.
     *
     * @param lines The list to append to.
     */
    public void appendEnergyTooltip(List<Component> lines) {
        lines.add(Component.translatable(Constant.TranslationKey.STATUS).setStyle(Constant.Text.GRAY_STYLE).append(this.menu.state.getStatusText(this.menu.redstoneMode)));
        lines.add(DisplayUtil.createEnergyTooltip(this.menu.energyStorage.getAmount(), this.menu.energyStorage.getCapacity()));
    }

    /**
     * Draws the configuration panels and their contents.
     *
     * @param graphics The gui graphics.
     * @param mouseX The mouse's x-position.
     * @param mouseY The mouse's y-position.
     */
    protected void drawConfigurationPanels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        assert this.minecraft != null;
        boolean secondary = false;
        PoseStack poseStack = graphics.pose();

        for (Tab tab : Tab.values()) { // 0, 1, 2, 3
            poseStack.pushPose();
            if (secondary) poseStack.translate(0, SPACING, 0);
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, this.leftPos + (tab.isLeft() ? tab.isOpen() ? -PANEL_WIDTH : -22 : this.imageWidth), this.topPos + (secondary ? Tab.values()[tab.ordinal() - 1].isOpen() ? PANEL_HEIGHT : TAB_HEIGHT : 0) + SPACING, tab.getU(), tab.getV(), tab.isOpen() ? PANEL_WIDTH : TAB_WIDTH, tab.isOpen() ? PANEL_HEIGHT : TAB_HEIGHT);
            if (!tab.isOpen()) {
                graphics.renderFakeItem(tab.getItem(), this.leftPos + (tab.isLeft() ? -22 : this.imageWidth) + (tab.isLeft() ? 4 : 2), this.topPos + (secondary ? Tab.values()[tab.ordinal() - 1].isOpen() ? PANEL_HEIGHT : TAB_HEIGHT : 0) + SPACING + 3);
            }
            secondary = !secondary;
            poseStack.popPose();
        }
        poseStack.pushPose();
        poseStack.translate(this.leftPos, this.topPos, 0);

        if (Tab.REDSTONE.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(-PANEL_WIDTH, SPACING, 0);
            this.drawButton(graphics, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, mouseX + PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, menu.redstoneMode == RedstoneMode.IGNORE);
            this.drawButton(graphics, REDSTONE_LOW_X, REDSTONE_LOW_Y, mouseX + PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, menu.redstoneMode == RedstoneMode.LOW);
            this.drawButton(graphics, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, mouseX + PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, menu.redstoneMode == RedstoneMode.HIGH);
            graphics.renderFakeItem(REDSTONE, PANEL_ICON_X, PANEL_ICON_Y);
            graphics.renderFakeItem(GUNPOWDER, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y);
            graphics.renderFakeItem(UNLIT_TORCH, REDSTONE_LOW_X, REDSTONE_LOW_Y - 2);
            graphics.renderFakeItem(REDSTONE_TORCH, REDSTONE_HIGH_X, REDSTONE_HIGH_Y - 2);

            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_MODE)
                    .setStyle(Constant.Text.GRAY_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_STATE,
                    menu.redstoneMode.getName()).setStyle(Constant.Text.GRAY_STYLE), REDSTONE_STATE_TEXT_X, REDSTONE_STATE_TEXT_Y, 0xFFFFFFFF);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_STATUS,
                            this.menu.redstoneMode.isActive(this.menu.state.isPowered()) ?
                                    Component.translatable(Constant.TranslationKey.REDSTONE_ACTIVE).setStyle(Constant.Text.GREEN_STYLE)
                                    : Component.translatable(Constant.TranslationKey.REDSTONE_DISABLED).setStyle(Constant.Text.DARK_RED_STYLE))
                    .setStyle(Constant.Text.GRAY_STYLE), REDSTONE_STATUS_TEXT_X, REDSTONE_STATUS_TEXT_Y + this.font.lineHeight, 0xFFFFFFFF);

            poseStack.popPose();
        }
        if (Tab.CONFIGURATION.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(-PANEL_WIDTH, TAB_HEIGHT + SPACING + SPACING, 0);
            graphics.renderFakeItem(WRENCH, PANEL_ICON_X, PANEL_ICON_Y);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.CONFIGURATION)
                    .setStyle(Constant.Text.GRAY_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);

            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            this.drawMachineFace(graphics, TOP_FACE_X, TOP_FACE_Y, this.menu.configuration, BlockFace.TOP);
            this.drawMachineFace(graphics, LEFT_FACE_X, LEFT_FACE_Y, this.menu.configuration, BlockFace.LEFT);
            this.drawMachineFace(graphics, FRONT_FACE_X, FRONT_FACE_Y, this.menu.configuration, BlockFace.FRONT);
            this.drawMachineFace(graphics, RIGHT_FACE_X, RIGHT_FACE_Y, this.menu.configuration, BlockFace.RIGHT);
            this.drawMachineFace(graphics, BACK_FACE_X, BACK_FACE_Y, this.menu.configuration, BlockFace.BACK);
            this.drawMachineFace(graphics, BOTTOM_FACE_X, BOTTOM_FACE_Y, this.menu.configuration, BlockFace.BOTTOM);
            poseStack.popPose();
        }
        if (Tab.STATS.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(this.imageWidth, SPACING, 0);
            graphics.renderFakeItem(ALUMINUM_WIRE, PANEL_ICON_X, PANEL_ICON_Y);
            PlayerFaceRenderer.draw(graphics, this.ownerSkin.getNow(DefaultPlayerSkin.get(this.menu.security.getOwner() == null ? this.menu.player.getUUID() : this.menu.security.getOwner())), OWNER_FACE_X, OWNER_FACE_Y, OWNER_FACE_SIZE);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.STATISTICS)
                    .setStyle(Constant.Text.GREEN_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);
            List<FormattedCharSequence> text = this.font.split(this.menu.be.getBlockState().getBlock().getName(), 64);
            int offsetY = 0;
            for (FormattedCharSequence orderedText : text) {
                graphics.drawString(this.font, orderedText, 40, 22 + offsetY, 0xFFFFFFFF, false);
                offsetY += this.font.lineHeight + 2;
            }
            poseStack.popPose();
        }

        if (Tab.SECURITY.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(this.imageWidth, TAB_HEIGHT + SPACING + SPACING, 0);
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, PANEL_ICON_X, PANEL_ICON_Y, ICON_LOCK_PRIVATE_U, ICON_LOCK_PRIVATE_V, ICON_WIDTH, ICON_HEIGHT);

            this.drawButton(graphics, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (TAB_HEIGHT + SPACING + SPACING) - this.topPos, this.menu.security.getAccessLevel() == AccessLevel.PUBLIC || !this.menu.security.isOwner(this.menu.player));
            this.drawButton(graphics, SECURITY_TEAM_X, SECURITY_TEAM_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (TAB_HEIGHT + SPACING + SPACING) - this.topPos, this.menu.security.getAccessLevel() == AccessLevel.TEAM || !this.menu.security.isOwner(this.menu.player));
            this.drawButton(graphics, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (TAB_HEIGHT + SPACING + SPACING) - this.topPos, this.menu.security.getAccessLevel() == AccessLevel.PRIVATE || !this.menu.security.isOwner(this.menu.player));
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, ICON_LOCK_PRIVATE_U, ICON_LOCK_PRIVATE_V, ICON_WIDTH, ICON_HEIGHT);
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, SECURITY_TEAM_X, SECURITY_TEAM_Y, ICON_LOCK_PARTY_U, ICON_LOCK_PARTY_V, ICON_WIDTH, ICON_HEIGHT);
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, ICON_LOCK_PUBLIC_U, ICON_LOCK_PUBLIC_V, ICON_WIDTH, ICON_HEIGHT);

            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.SECURITY)
                    .setStyle(Constant.Text.GRAY_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.ACCESS_LEVEL,
                    this.menu.security.getAccessLevel().getName()).setStyle(Constant.Text.GRAY_STYLE), SECURITY_STATE_TEXT_X, SECURITY_STATE_TEXT_Y, 0xFFFFFFFF);

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
     * @param x the x position to draw at
     * @param y the y position to draw at
     * @param ioConfig the machine's extra render data
     * @param face the face to draw
     */
    private void drawMachineFace(@NotNull GuiGraphics graphics, int x, int y, @NotNull IOConfig ioConfig, @NotNull BlockFace face) {
        if (this.model != null) {
            graphics.blit(x, y, 0, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE, this.model.getSprite(this.menu.be.getBlockState(), face, ioConfig));
        }
    }

    /**
     * Draws a 16x16 button at the given position.
     * The button will be highlighted if the mouse is hovering over it.
     *
     * @param graphics the gui graphics instance
     * @param x the x-position to draw at
     * @param y the y-position to draw at
     * @param mouseX the mouse's x-position
     * @param mouseY the mouse's y-position
     * @param pressed whether the button is pressed
     */
    public void drawButton(GuiGraphics graphics, int x, int y, double mouseX, double mouseY, boolean pressed) {
        if (pressed) {
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, x, y, BUTTON_U, BUTTON_PRESSED_V, BUTTON_WIDTH, BUTTON_HEIGHT);
        } else if (mouseIn(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
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
            if (mouseIn(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.REDSTONE.toggle();
                return true;
            }
            if (mouseIn(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                this.setRedstone(RedstoneMode.IGNORE);
                this.playButtonSound();
                return true;
            }
            if (mouseIn(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                this.setRedstone(RedstoneMode.LOW);
                this.playButtonSound();
                return true;
            }
            if (mouseIn(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                this.setRedstone(RedstoneMode.HIGH);
                this.playButtonSound();
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (mouseIn(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_HEIGHT)) {
                    return true;
                }
            }
        } else {
            mouseX += TAB_WIDTH;
            mouseY -= SPACING;
            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.REDSTONE.toggle();
                return true;
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= TAB_HEIGHT + SPACING + SPACING;
            if (mouseIn(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.CONFIGURATION.toggle();
                return true;
            }
            if (button >= GLFW.GLFW_MOUSE_BUTTON_LEFT && button <= GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                if (mouseIn(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.TOP);
                    return true;
                } else if (mouseIn(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.LEFT);
                    return true;
                } else if (mouseIn(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.FRONT);
                    return true;
                } else if (mouseIn(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.RIGHT);
                    return true;
                } else if (mouseIn(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
                    this.modifyFace(button, BlockFace.BACK);
                    return true;
                } else if (mouseIn(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
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
            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.CONFIGURATION.toggle();
                return true;
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        mouseX -= this.imageWidth;
        mouseY -= SPACING;
        if (Tab.STATS.isOpen()) {
            if (mouseIn(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.STATS.toggle();
                return true;
            }
        } else {
            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.STATS.toggle();
                return true;
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        mouseX -= this.imageWidth;
        if (Tab.SECURITY.isOpen()) {
            mouseY -= TAB_HEIGHT + SPACING + SPACING;
            if (mouseIn(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.SECURITY.toggle();
                return true;
            }

            if (this.menu.security.isOwner(this.menu.player)) {
                if (mouseIn(mouseX, mouseY, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    this.setAccessibility(AccessLevel.PRIVATE);
                    this.playButtonSound();
                    return true;
                }
                if (mouseIn(mouseX, mouseY, SECURITY_TEAM_X, SECURITY_TEAM_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    this.setAccessibility(AccessLevel.TEAM);
                    this.playButtonSound();
                    return true;
                }
                if (mouseIn(mouseX, mouseY, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
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
            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.SECURITY.toggle();
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
        this.menu.security.setAccessLevel(accessLevel);
        PacketSender.c2s().send(new AccessLevelPayload(accessLevel));
    }

    /**
     * Sets the redstone level of the machine and syncs it to the server.
     *
     * @param redstone The redstone level to set.
     */
    protected void setRedstone(@NotNull RedstoneMode redstone) {
        this.menu.redstoneMode = redstone;
        PacketSender.c2s().send(new RedstoneModePayload(redstone));
    }

    /**
     * Draws the tooltips of the configuration panel.
     *
     * @param graphics The gui graphics to use.
     * @param mouseX The mouse's x-position.
     * @param mouseY The mouse's y-position.
     */
    protected void drawConfigurationPanelTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        final int mX = mouseX, mY = mouseY;
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.REDSTONE.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= SPACING;
            if (mouseIn(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                graphics.renderTooltip(this.font, RedstoneMode.IGNORE.getName(), mX, mY);
            }
            if (mouseIn(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                graphics.renderTooltip(this.font, RedstoneMode.LOW.getName(), mX, mY);
            }
            if (mouseIn(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                graphics.renderTooltip(this.font, RedstoneMode.HIGH.getName(), mX, mY);
            }
        } else {
            mouseX += TAB_WIDTH;
            mouseY -= SPACING;
            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_MODE).setStyle(Constant.Text.RED_STYLE), mX, mY);
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= TAB_HEIGHT + SPACING + SPACING;
            if (mouseIn(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.TOP, mX, mY);
            }
            if (mouseIn(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.LEFT, mX, mY);
            }
            if (mouseIn(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.FRONT, mX, mY);
            }
            if (mouseIn(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.RIGHT, mX, mY);
            }
            if (mouseIn(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.BACK, mX, mY);
            }
            if (mouseIn(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.BOTTOM, mX, mY);
            }
        } else {
            mouseX += TAB_WIDTH;
            if (Tab.REDSTONE.isOpen()) {
                mouseY -= PANEL_HEIGHT + SPACING;
            } else {
                mouseY -= TAB_HEIGHT + SPACING;
            }
            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.CONFIGURATION).setStyle(Constant.Text.BLUE_STYLE), mX, mY);
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        mouseX -= this.imageWidth;
        mouseY -= SPACING;
        if (Tab.STATS.isOpen()) {
            GameProfile ownerProfile = this.owner.getNow(null);
            if (ownerProfile != null) {
                if (mouseIn(mouseX, mouseY, OWNER_FACE_X, OWNER_FACE_Y, OWNER_FACE_SIZE, OWNER_FACE_SIZE)) {
                    assert this.menu.security.getOwner() != null;
                    graphics.renderTooltip(this.font, Component.literal(ownerProfile.getName()), mX, mY);
                }
            }
        } else {
            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.STATISTICS).setStyle(Constant.Text.YELLOW_STYLE), mX, mY);
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.SECURITY.isOpen()) {
            mouseX -= this.imageWidth;
            mouseY -= TAB_HEIGHT + SPACING + SPACING;

            if (this.menu.security.isOwner(this.menu.player)) {
                if (mouseIn(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    graphics.renderTooltip(this.font, AccessLevel.PRIVATE.getName(), mX, mY);
                }
                if (mouseIn(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    graphics.renderTooltip(this.font, AccessLevel.TEAM.getName(), mX, mY);
                }
                if (mouseIn(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    graphics.renderTooltip(this.font, AccessLevel.PUBLIC.getName(), mX, mY);
                }
            } else {
                if (mouseIn(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                        || mouseIn(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                        || mouseIn(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
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
            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.SECURITY).setStyle(Constant.Text.BLUE_STYLE), mX, mY);
            }
        }
    }

    /**
     * Renders the tooltip for the given face.
     *
     * @param graphics The gui graphics
     * @param face The face to render the tooltip for
     * @param mouseX The mouse's x-position
     * @param mouseY The mouse's y-position
     */
    protected void renderFaceTooltip(GuiGraphics graphics, @NotNull BlockFace face, int mouseX, int mouseY) {
        TOOLTIP_ARRAY.add(face.getName());
        IOFace configuredFace = this.menu.configuration.get(face);
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
     * @param mouseX The mouse's x-position
     * @param mouseY The mouse's y-position
     * @param delta The delta time
     */
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected final void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        this.renderMachineBackground(graphics, mouseX, mouseY, delta);
        this.drawTanks(graphics, mouseX, mouseY);
        this.drawCapacitor(graphics, mouseX, mouseY);
        this.handleSlotHighlight(graphics, mouseX, mouseY);
        this.drawConfigurationPanels(graphics, mouseX, mouseY);
    }

    /**
     * Draws the capacitor of this machine.
     * If the machine has no capacitor, this method does nothing.
     *
     * @param graphics The gui graphics
     * @param mouseX The mouse's x-position
     * @param mouseY The mouse's y-position
     */
    protected void drawCapacitor(GuiGraphics graphics, int mouseX, int mouseY) {
        long capacity = this.menu.energyStorage.getCapacity();
        if (capacity > 0 && this.capacitorHeight != 0) {
            int x = this.leftPos + this.capacitorX;
            int y = this.topPos + this.capacitorY;
            long amount = this.menu.energyStorage.getAmount();
            float scale = (float) ((double) amount / (double) capacity);
            graphics.blit(Constant.ScreenTexture.OVERLAY_BARS, x, y, ENERGY_X, ENERGY_Y, OVERLAY_WIDTH, OVERLAY_HEIGHT, OVERLAY_TEX_WIDTH, OVERLAY_TEX_HEIGHT);
            graphics.blit(Constant.ScreenTexture.OVERLAY_BARS, x, y, ENERGY_BACKGROUND_X, ENERGY_BACKGROUND_Y, OVERLAY_WIDTH, (int) (OVERLAY_HEIGHT * (1 - scale)), OVERLAY_TEX_WIDTH, OVERLAY_TEX_HEIGHT);

            if (mouseIn(mouseX, mouseY, this.leftPos + this.capacitorX, this.topPos + this.capacitorY, 16, this.capacitorHeight)) {
                List<Component> lines = new ArrayList<>();
                this.appendEnergyTooltip(lines);
                this.setTooltipForNextRenderPass(Lists.transform(lines, Component::getVisualOrderText));
            }
        }
    }

    /**
     * Draws the background of this machine.
     *
     * @param graphics The gui graphics
     * @param mouseX The mouse's x-position
     * @param mouseY The mouse's y-position
     * @param delta The delta time
     */
    protected void renderMachineBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    }

    /**
     * Draws the (fluid and gas) tanks of this machine.
     *
     * @param graphics The gui graphics
     * @param mouseX The mouse's x-position
     * @param mouseY The mouse's y-position
     */
    protected void drawTanks(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(this.leftPos, this.topPos, 0);
        this.hoveredTank = null;
        for (Tank tank : this.menu.tanks) {
            if (tank.getHeight() > 0 && tank.getWidth() > 0) {
                if (tank.getAmount() > 0) {
                    GraphicsUtil.drawFluid(graphics, tank.getX(), tank.getY(), tank.getWidth(), tank.getHeight(),
                            tank.getCapacity(), tank.createVariant(), tank.getAmount());
                }

                boolean primary = true;
                for (int y = tank.getY() + tank.getHeight() - 2; y > tank.getY(); y -= 3) {
                    graphics.hLine(tank.getX(), tank.getX() + Mth.ceil(primary ? tank.getWidth() / 2.5 : tank.getWidth() / 3.5), y, 0xFFB31212);
                    primary = !primary;
                }

                if (this.hoveredTank == null && mouseIn(mouseX, mouseY, this.leftPos + tank.getX(), this.topPos + tank.getY(), tank.getWidth(), tank.getHeight())) {
                    this.hoveredTank = tank;
                    RenderSystem.disableDepthTest();
                    graphics.fill(tank.getX(), tank.getY(), tank.getX() + tank.getWidth(), tank.getY() + tank.getHeight(), 0x80ffffff);
                    RenderSystem.enableDepthTest();
                }
            }
        }
        graphics.pose().popPose();

        for (Tank tank : this.menu.tanks) {
            if (mouseIn(mouseX, mouseY, this.leftPos + tank.getX(), this.topPos + tank.getY(), tank.getWidth(), tank.getHeight())) {
                this.setTooltipForNextRenderPass(Lists.transform(tank.getTooltip(), Component::getVisualOrderText));
                break;
            }
        }
    }

    @ApiStatus.Internal
    private void handleSlotHighlight(GuiGraphics graphics, int mouseX, int mouseY) {
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX -= (this.leftPos - PANEL_WIDTH);
            mouseY -= (this.topPos + (TAB_HEIGHT + SPACING + SPACING));
            IOFace config = null;
            if (mouseIn(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.get(BlockFace.TOP);
            } else if (mouseIn(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.get(BlockFace.LEFT);
            } else if (mouseIn(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.get(BlockFace.FRONT);
            } else if (mouseIn(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.get(BlockFace.RIGHT);
            } else if (mouseIn(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.get(BlockFace.BACK);
            } else if (mouseIn(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                config = this.menu.configuration.get(BlockFace.BOTTOM);
            }
            if (config != null) {
                ResourceType resource = config.getType();
                if (resource.willAcceptResource(ResourceType.ITEM)) {
                    for (Slot slot : this.menu.slots) {
                        if (slot instanceof StorageSlot slot1) {
                            TransferType type = slot1.getWrapped().transferMode();
                            if (type.getExternalFlow() != null && type.getExternalFlow().canFlowIn(config.getFlow())) {
                                GraphicsUtil.highlightElement(graphics, this.leftPos, this.topPos, slot.x, slot.y, 16, 16, type.color());
                            }
                        }
                    }
                }

                if (resource.willAcceptResource(ResourceType.FLUID)) {
                    for (Tank tank : this.menu.tanks) {
                        TransferType type = tank.getInputType();
                        if (type.getExternalFlow() != null && type.getExternalFlow().canFlowIn(config.getFlow())) {
                            GraphicsUtil.highlightElement(graphics, this.leftPos, this.topPos, tank.getX(), tank.getY(), tank.getWidth(), tank.getHeight(), type.color());
                        }
                    }
                }

                if (resource.willAcceptResource(ResourceType.ENERGY)) {
                    RenderSystem.enableBlend();
                    GraphicsUtil.highlightElement(graphics, this.leftPos, this.topPos, this.capacitorX, this.capacitorY, 16, this.capacitorHeight, 0x00f6ff00);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hoveredTank != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (this.hoveredTank.acceptStack(ContainerItemContext.ofPlayerCursor(this.menu.playerInventory.player, this.menu))) {
                PacketSender.c2s().send(new TankInteractionPayload(this.menu.containerId, this.hoveredTank.getIndex()));
            }
            return true;
        }
        return this.checkConfigurationPanelClick(mouseX, mouseY, button) | super.mouseClicked(mouseX, mouseY, button);
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
     * {@return the x offset of the screen}
     */
    public int getX() {
        return this.leftPos;
    }

    /**
     * {@return the y offset of the screen}
     */
    public int getY() {
        return this.topPos;
    }

    /**
     * {@return the width of the background image}
     */
    public int getImageWidth() {
        return this.imageWidth;
    }

    /**
     * {@return the height of the background image}
     */
    public int getImageHeight() {
        return this.imageHeight;
    }

    private void modifyFace(int button, BlockFace face) {
        if (this.menu.isFaceLocked(face)) return;
        if (button == 0) {
            ClientPlayNetworking.send(new SideConfigurationClickPayload(face, Screen.hasShiftDown(), Screen.hasControlDown()));
            this.menu.cycleFaceConfig(face, Screen.hasShiftDown(), Screen.hasControlDown());
        }
        this.playButtonSound();
    }

    /**
     * The four different types of configuration panels.
     */
    public enum Tab {
        REDSTONE(TAB_REDSTONE_U, TAB_REDSTONE_V, PANEL_REDSTONE_U, PANEL_REDSTONE_V, true, MachineScreen.REDSTONE),
        CONFIGURATION(TAB_CONFIG_U, TAB_CONFIG_V, PANEL_CONFIG_U, PANEL_CONFIG_V, true, WRENCH),
        STATS(TAB_STATS_U, TAB_STATS_V, PANEL_STATS_U, PANEL_STATS_V, false, ALUMINUM_WIRE),
        SECURITY(TAB_SECURITY_U, TAB_SECURITY_V, PANEL_SECURITY_U, PANEL_SECURITY_V, false, IRON_CHESTPLATE);

        private final int tabU;
        private final int tabV;
        private final int panelU;
        private final int panelV;
        private final boolean left;
        private final ItemStack item;

        private boolean open = false;

        Tab(int tabU, int tabV, int panelU, int panelV, boolean left, ItemStack item) {
            this.tabU = tabU;
            this.tabV = tabV;
            this.panelU = panelU;
            this.panelV = panelV;
            this.left = left;
            this.item = item;
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

        public void toggle() {
            this.open = !this.open;
            if (this.open) {
                Tab.values()[this.ordinal() + 1 - this.ordinal() % 2 * 2].open = false;
            }
        }

        public ItemStack getItem() {
            return this.item;
        }
    }
}
