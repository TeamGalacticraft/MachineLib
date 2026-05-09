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

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.configuration.*;
import dev.galacticraft.machinelib.api.menu.Tank;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.api.screen.port.PortPreviewScene;
import dev.galacticraft.machinelib.client.api.screen.port.PortPreviewWidget;
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
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static dev.galacticraft.machinelib.impl.Constant.TextureCoordinate.*;

@Environment(EnvType.CLIENT)
public abstract class AbstractMachineScreen<Menu extends AbstractContainerMenu> extends AbstractContainerScreen<Menu> {

    protected static final ItemStack REDSTONE = new ItemStack(Items.REDSTONE);
    protected static final ItemStack GUNPOWDER = new ItemStack(Items.GUNPOWDER);
    protected static final ItemStack UNLIT_TORCH = new ItemStack(getOptionalItem(ResourceLocation.fromNamespaceAndPath("galacticraft", "unlit_torch"), Items.TORCH));
    protected static final ItemStack REDSTONE_TORCH = new ItemStack(Items.REDSTONE_TORCH);
    protected static final ItemStack WRENCH = new ItemStack(getOptionalItem(ResourceLocation.fromNamespaceAndPath("galacticraft", "standard_wrench"), Items.HOPPER));
    protected static final ItemStack ALUMINUM_WIRE = new ItemStack(getOptionalItem(ResourceLocation.fromNamespaceAndPath("galacticraft", "aluminum_wire"), Items.MOJANG_BANNER_PATTERN));
    protected static final ItemStack IRON_CHESTPLATE = new ItemStack(Items.IRON_CHESTPLATE);

    public static final int SPACING = 4;

    private static final int PANEL_ICON_X_LEFT = 4;
    private static final int PANEL_ICON_X_RIGHT = 2;
    private static final int PANEL_ICON_Y = 3;

    private static final int PANEL_TITLE_X = 18;
    private static final int PANEL_TITLE_Y = 7;

    private static final int REDSTONE_IGNORE_X = 14;
    private static final int REDSTONE_IGNORE_Y = 26;
    private static final int REDSTONE_LOW_X = 41;
    private static final int REDSTONE_LOW_Y = 26;
    private static final int REDSTONE_HIGH_X = 68;
    private static final int REDSTONE_HIGH_Y = 26;

    private static final int SECURITY_PUBLIC_X = 12;
    private static final int SECURITY_PUBLIC_Y = 26;
    private static final int SECURITY_TEAM_X = 39;
    private static final int SECURITY_TEAM_Y = 26;
    private static final int SECURITY_PRIVATE_X = 66;
    private static final int SECURITY_PRIVATE_Y = 26;

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

    private static final int OWNER_FACE_X = 33;
    private static final int OWNER_FACE_Y = 30;
    private static final int OWNER_TEXT_X = 49;
    private static final int OWNER_TEXT_Y = 66;

    private static final int REDSTONE_STATE_TEXT_X = 11;
    private static final int REDSTONE_STATE_TEXT_Y = 54;
    private static final int REDSTONE_STATUS_TEXT_X = 11;
    private static final int REDSTONE_STATUS_TEXT_Y = 59;

    private static final int SECURITY_STATE_TEXT_X = 9;
    private static final int SECURITY_STATE_TEXT_Y = 54;

    private static final int MACHINE_FACE_SIZE = 16;
    private static final int BUTTON_SIZE = 16;

    @ApiStatus.Internal
    private static final List<Component> TOOLTIP_ARRAY = new ArrayList<>();

    private final ResourceLocation texture;
    private final CompletableFuture<PlayerSkin> ownerSkin;
    private final CompletableFuture<GameProfile> owner;

    public Tank hoveredTank = null;

    protected int capacitorX = 8;
    protected int capacitorY = 8;
    protected int capacitorHeight = 48;

    /**
     * Creates a shared MachineLib machine screen.
     *
     * @param menu menu backing this screen
     * @param inventory player inventory
     * @param player player viewing the screen
     * @param security security settings used for owner display
     * @param title screen title
     * @param texture screen background texture
     */
    protected AbstractMachineScreen(
            final Menu menu,
            final Inventory inventory,
            final Player player,
            final SecuritySettings security,
            final Component title,
            final ResourceLocation texture
    ) {
        super(menu, inventory, title);

        this.texture = texture;

        final UUID ownerUuid = security.getOwner() != null
                ? security.getOwner()
                : player.getUUID();

        this.owner = SkullBlockEntity.fetchGameProfile(ownerUuid)
                .thenApply(profile -> profile.orElse(new GameProfile(ownerUuid, "???")));

        this.ownerSkin = this.owner.thenCompose(profile ->
                Minecraft.getInstance().getSkinManager().getOrLoad(profile)
        );
    }

    /**
     * Gets the item requested item, or the fallback item if unavailable.
     *
     * @param id item id
     * @param fallback fallback item
     * @return found item, or fallback
     */
    private static Item getOptionalItem(
            final ResourceLocation id,
            final Item fallback
    ) {
        return BuiltInRegistries.ITEM.getOptional(id).orElse(fallback);
    }

    /**
     * Checks whether the mouse is inside a rectangle.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param x rectangle x
     * @param y rectangle y
     * @param width rectangle width
     * @param height rectangle height
     * @return true if inside
     */
    protected static boolean mouseIn(
            final double mouseX,
            final double mouseY,
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    protected abstract Inventory playerInventory();

    protected abstract Player machinePlayer();

    protected abstract SecuritySettings security();

    protected abstract IOConfig configuration();

    protected abstract MachineState state();

    protected abstract RedstoneMode redstoneMode();

    protected abstract void setLocalRedstoneMode(RedstoneMode mode);

    protected abstract MachineEnergyStorage energyStorage();

    protected abstract List<Tank> tanks();

    protected abstract boolean isFaceLocked(BlockFace face);

    protected abstract void cycleFaceConfig(BlockFace face, boolean reverse, boolean reset);

    protected abstract MachineBakedModel machineModel();

    protected abstract BlockState machineBlockState();

    protected abstract void refreshMachineModel();

    private PortPreviewWidget portPreviewWidget;
    private PortPreviewScene portPreviewScene;

    /**
     * Initializes the screen.
     */
    @Override
    protected void init() {
        super.init();

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.refreshMachineModel();

        this.portPreviewScene = this.createPortPreviewScene();

        if (this.portPreviewScene != null) {
            this.portPreviewWidget = new PortPreviewWidget(
                    this.leftPos + this.imageWidth + SPACING,
                    this.topPos + SPACING,
                    170,
                    120
            );
        } else {
            this.portPreviewWidget = null;
        }
    }

    /**
     * Refreshes model data each client tick.
     */
    @Override
    protected void containerTick() {
        super.containerTick();

        this.refreshMachineModel();
    }

    /**
     * Appends additional information to the capacitor tooltip.
     *
     * @param lines tooltip lines
     */
    public void appendEnergyTooltip(final List<Component> lines) {
        lines.add(Component.translatable(Constant.TranslationKey.STATUS)
                .setStyle(Constant.Text.GRAY_STYLE)
                .append(this.state().getStatusText(this.redstoneMode())));

        lines.add(DisplayUtil.createEnergyTooltip(
                this.energyStorage().getAmount(),
                this.energyStorage().getCapacity()
        ));
    }

    /**
     * Draws configuration panels.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    protected void drawConfigurationPanels(
            final GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        final PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 10);

        int leftX = this.leftPos;
        int rightX = this.leftPos + this.imageWidth;
        int leftY = this.topPos + SPACING;
        int rightY = this.topPos + SPACING;

        for (final Tab tab : Tab.values()) {
            final int width = tab.isOpen() ? PANEL_WIDTH : TAB_WIDTH;
            final int height = tab.isOpen() ? PANEL_HEIGHT : TAB_HEIGHT;

            if (tab.isLeft()) {
                graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, leftX - width, leftY, tab.getU(), tab.getV(), width, height);

                if (!tab.isOpen()) {
                    graphics.renderFakeItem(tab.getItem(), leftX - TAB_WIDTH + 4, leftY + 3);
                }

                leftY += height + SPACING;
            } else {
                graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, rightX, rightY, tab.getU(), tab.getV(), width, height);

                if (!tab.isOpen()) {
                    graphics.renderFakeItem(tab.getItem(), rightX + 2, rightY + 3);
                }

                rightY += height + SPACING;
            }
        }

        poseStack.translate(this.leftPos, this.topPos, 0);

        if (Tab.REDSTONE.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(-PANEL_WIDTH, SPACING, 0);

            this.drawButton(graphics, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, mouseX + PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, this.redstoneMode() == RedstoneMode.IGNORE);
            this.drawButton(graphics, REDSTONE_LOW_X, REDSTONE_LOW_Y, mouseX + PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, this.redstoneMode() == RedstoneMode.LOW);
            this.drawButton(graphics, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, mouseX + PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, this.redstoneMode() == RedstoneMode.HIGH);

            graphics.renderFakeItem(REDSTONE, Tab.REDSTONE.isLeft() ? PANEL_ICON_X_LEFT : PANEL_ICON_X_RIGHT, PANEL_ICON_Y);
            graphics.renderFakeItem(GUNPOWDER, REDSTONE_IGNORE_X + 2, REDSTONE_IGNORE_Y + 2);
            graphics.renderFakeItem(UNLIT_TORCH, REDSTONE_LOW_X + 2, REDSTONE_LOW_Y);
            graphics.renderFakeItem(REDSTONE_TORCH, REDSTONE_HIGH_X + 2, REDSTONE_HIGH_Y);

            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_MODE).setStyle(Constant.Text.GRAY_STYLE), (Tab.REDSTONE.isLeft() ? PANEL_ICON_X_LEFT : PANEL_ICON_X_RIGHT) + PANEL_TITLE_X - 1, PANEL_TITLE_Y, 0xFFFFFFFF);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_STATE, this.redstoneMode().getName()).setStyle(Constant.Text.GRAY_STYLE), REDSTONE_STATE_TEXT_X, REDSTONE_STATE_TEXT_Y, 0xFFFFFFFF);
            graphics.drawString(this.font, Component.translatable(
                    Constant.TranslationKey.REDSTONE_STATUS,
                    this.redstoneMode().isActive(this.state().isPowered())
                            ? Component.translatable(Constant.TranslationKey.REDSTONE_ACTIVE).setStyle(Constant.Text.GREEN_STYLE)
                            : Component.translatable(Constant.TranslationKey.REDSTONE_DISABLED).setStyle(Constant.Text.DARK_RED_STYLE)
            ).setStyle(Constant.Text.GRAY_STYLE), REDSTONE_STATUS_TEXT_X, REDSTONE_STATUS_TEXT_Y + this.font.lineHeight, 0xFFFFFFFF);

            poseStack.popPose();
        }

        if (Tab.CONFIGURATION.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(-PANEL_WIDTH, TAB_HEIGHT + SPACING * 2, 0);

            graphics.renderFakeItem(WRENCH, Tab.CONFIGURATION.isLeft() ? PANEL_ICON_X_LEFT : PANEL_ICON_X_RIGHT, PANEL_ICON_Y);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.CONFIGURATION).setStyle(Constant.Text.GRAY_STYLE), (Tab.CONFIGURATION.isLeft() ? PANEL_ICON_X_LEFT : PANEL_ICON_X_RIGHT) + PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);

            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            this.drawMachineFace(graphics, TOP_FACE_X, TOP_FACE_Y, this.configuration(), BlockFace.TOP);
            this.drawMachineFace(graphics, LEFT_FACE_X, LEFT_FACE_Y, this.configuration(), BlockFace.LEFT);
            this.drawMachineFace(graphics, FRONT_FACE_X, FRONT_FACE_Y, this.configuration(), BlockFace.FRONT);
            this.drawMachineFace(graphics, RIGHT_FACE_X, RIGHT_FACE_Y, this.configuration(), BlockFace.RIGHT);
            this.drawMachineFace(graphics, BACK_FACE_X, BACK_FACE_Y, this.configuration(), BlockFace.BACK);
            this.drawMachineFace(graphics, BOTTOM_FACE_X, BOTTOM_FACE_Y, this.configuration(), BlockFace.BOTTOM);

            poseStack.popPose();
        }

        if (Tab.STATS.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(this.imageWidth, SPACING, 0);

            graphics.renderFakeItem(ALUMINUM_WIRE, Tab.STATS.isLeft() ? PANEL_ICON_X_LEFT : PANEL_ICON_X_RIGHT, PANEL_ICON_Y);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.STATISTICS).setStyle(Constant.Text.WHITE_STYLE), (Tab.STATS.isLeft() ? PANEL_ICON_X_LEFT : PANEL_ICON_X_RIGHT) + PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);

            PlayerFaceRenderer.draw(graphics, this.ownerSkin.getNow(DefaultPlayerSkin.get(this.security().getOwner() != null ? this.security().getOwner() : this.machinePlayer().getUUID())), OWNER_FACE_X, OWNER_FACE_Y, OWNER_FACE_SIZE);
            graphics.drawCenteredString(this.font, Component.translatable(Constant.TranslationKey.OWNER).setStyle(Constant.Text.WHITE_STYLE), OWNER_TEXT_X, OWNER_TEXT_Y, 0xFFFFFFFF);

            poseStack.popPose();
        }

        if (Tab.SECURITY.isOpen()) {
            poseStack.pushPose();
            poseStack.translate(this.imageWidth, TAB_HEIGHT + SPACING * 2, 0);

            graphics.renderFakeItem(IRON_CHESTPLATE, Tab.SECURITY.isLeft() ? PANEL_ICON_X_LEFT : PANEL_ICON_X_RIGHT, PANEL_ICON_Y);

            this.drawButton(graphics, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (TAB_HEIGHT + SPACING * 2) - this.topPos, this.security().getAccessLevel() == AccessLevel.PUBLIC || !this.security().isOwner(this.machinePlayer()));
            this.drawButton(graphics, SECURITY_TEAM_X, SECURITY_TEAM_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (TAB_HEIGHT + SPACING * 2) - this.topPos, this.security().getAccessLevel() == AccessLevel.TEAM || !this.security().isOwner(this.machinePlayer()));
            this.drawButton(graphics, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (TAB_HEIGHT + SPACING * 2) - this.topPos, this.security().getAccessLevel() == AccessLevel.PRIVATE || !this.security().isOwner(this.machinePlayer()));

            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, ICON_LOCK_PUBLIC_U, ICON_LOCK_PUBLIC_V, ICON_WIDTH, ICON_HEIGHT);
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, SECURITY_TEAM_X, SECURITY_TEAM_Y, ICON_LOCK_PARTY_U, ICON_LOCK_PARTY_V, ICON_WIDTH, ICON_HEIGHT);
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, ICON_LOCK_PRIVATE_U, ICON_LOCK_PRIVATE_V, ICON_WIDTH, ICON_HEIGHT);

            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.SECURITY).setStyle(Constant.Text.GRAY_STYLE), (Tab.SECURITY.isLeft() ? PANEL_ICON_X_LEFT : PANEL_ICON_X_RIGHT) + PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);
            graphics.drawString(this.font, Component.translatable(Constant.TranslationKey.ACCESS_LEVEL, this.security().getAccessLevel().getName()).setStyle(Constant.Text.GRAY_STYLE), SECURITY_STATE_TEXT_X, SECURITY_STATE_TEXT_Y, 0xFFFFFFFF);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    /**
     * Draws the title.
     *
     * @param graphics GUI graphics
     */
    protected void drawTitle(final GuiGraphics graphics) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
    }

    /**
     * Draws one machine face icon.
     *
     * @param graphics GUI graphics
     * @param x x position
     * @param y y position
     * @param ioConfig IO config
     * @param face logical face
     */
    private void drawMachineFace(
            final GuiGraphics graphics,
            final int x,
            final int y,
            final IOConfig ioConfig,
            final BlockFace face
    ) {
        final MachineBakedModel model = this.machineModel();
        final BlockState blockState = this.machineBlockState();

        if (model != null && blockState != null) {
            graphics.blit(x, y, 0, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE, model.getItemOverride(blockState, face, ioConfig));
            return;
        }

        this.drawButton(graphics, x, y, -1000, -1000, ioConfig.get(face).getType() != ResourceType.NONE);
    }

    /**
     * Draws a button.
     *
     * @param graphics GUI graphics
     * @param x x position
     * @param y y position
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param pressed pressed state
     */
    public void drawButton(
            final GuiGraphics graphics,
            final int x,
            final int y,
            final double mouseX,
            final double mouseY,
            final boolean pressed
    ) {
        if (pressed) {
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, x, y, BUTTON_U, BUTTON_PRESSED_V, BUTTON_WIDTH, BUTTON_HEIGHT);
        } else if (mouseIn(mouseX, mouseY, x, y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, x, y, BUTTON_U, BUTTON_HOVERED_V, BUTTON_WIDTH, BUTTON_HEIGHT);
        } else {
            graphics.blit(Constant.ScreenTexture.MACHINE_CONFIG_PANELS, x, y, BUTTON_U, BUTTON_V, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    /**
     * Handles configuration panel clicks.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button mouse button
     * @return true if handled
     */
    public boolean checkConfigurationPanelClick(
            double mouseX,
            double mouseY,
            final int button
    ) {
        final double originalMouseX = mouseX;
        final double originalMouseY = mouseY;

        mouseX = originalMouseX - this.leftPos;
        mouseY = originalMouseY - this.topPos;

        if (Tab.REDSTONE.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= SPACING;

            if (mouseIn(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.REDSTONE.toggle();
                this.playButtonSound();
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

            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && mouseIn(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_HEIGHT)) {
                return true;
            }
        } else {
            mouseX += TAB_WIDTH;
            mouseY -= SPACING;

            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.REDSTONE.toggle();
                this.playButtonSound();
                return true;
            }
        }

        mouseX = originalMouseX - this.leftPos;
        mouseY = originalMouseY - this.topPos;

        if (Tab.CONFIGURATION.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= TAB_HEIGHT + SPACING * 2;

            if (mouseIn(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.CONFIGURATION.toggle();
                this.playButtonSound();
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
            mouseY -= (Tab.REDSTONE.isOpen() ? PANEL_HEIGHT : TAB_HEIGHT) + SPACING * 2;

            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.CONFIGURATION.toggle();
                this.playButtonSound();
                return true;
            }
        }

        mouseX = originalMouseX - this.leftPos - this.imageWidth;
        mouseY = originalMouseY - this.topPos - SPACING;

        if (Tab.STATS.isOpen()) {
            if (mouseIn(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.STATS.toggle();
                this.playButtonSound();
                return true;
            }
        } else if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
            Tab.STATS.toggle();
            this.playButtonSound();
            return true;
        }

        mouseX = originalMouseX - this.leftPos - this.imageWidth;
        mouseY = originalMouseY - this.topPos;

        if (Tab.SECURITY.isOpen()) {
            mouseY -= TAB_HEIGHT + SPACING * 2;

            if (mouseIn(mouseX, mouseY, 0, 0, PANEL_WIDTH, PANEL_UPPER_HEIGHT)) {
                Tab.SECURITY.toggle();
                this.playButtonSound();
                return true;
            }

            if (this.security().isOwner(this.machinePlayer())) {
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
            mouseY -= (Tab.STATS.isOpen() ? PANEL_HEIGHT : TAB_HEIGHT) + SPACING * 2;

            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                Tab.SECURITY.toggle();
                this.playButtonSound();
                return true;
            }
        }

        return false;
    }

    /**
     * Creates the 3D port preview scene for this screen.
     *
     * @return port preview scene, or {@code null} if this screen does not use one
     */
    protected PortPreviewScene createPortPreviewScene() {
        return null;
    }

    /**
     * Gets exclusion zones for recipe viewers.
     *
     * @return exclusion rectangles
     */
    public List<Rect2i> getExclusionZones() {
        final List<Rect2i> areas = new ArrayList<>();

        int leftX = this.getX();
        int rightX = this.getX() + this.getImageWidth();
        int leftY = this.getY() + SPACING;
        int rightY = this.getY() + SPACING;

        for (final Tab tab : Tab.values()) {
            final int width = tab.isOpen() ? PANEL_WIDTH : TAB_WIDTH;
            final int height = tab.isOpen() ? PANEL_HEIGHT : TAB_HEIGHT;

            if (tab.isLeft()) {
                areas.add(new Rect2i(leftX - width, leftY, width, height));
                leftY += height + SPACING;
            } else {
                areas.add(new Rect2i(rightX, rightY, width, height));
                rightY += height + SPACING;
            }
        }

        return areas;
    }

    /**
     * Sets access level locally and sends it to the server.
     *
     * @param accessLevel access level
     */
    protected void setAccessibility(final AccessLevel accessLevel) {
        this.security().setAccessLevel(accessLevel);
        PacketSender.c2s().send(new AccessLevelPayload(accessLevel));
    }

    /**
     * Sets redstone mode locally and sends it to the server.
     *
     * @param redstone redstone mode
     */
    protected void setRedstone(final RedstoneMode redstone) {
        this.setLocalRedstoneMode(redstone);
        PacketSender.c2s().send(new RedstoneModePayload(redstone));
    }

    /**
     * Draws configuration panel tooltips.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    protected void drawConfigurationPanelTooltips(
            final GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        final int originalMouseX = mouseX;
        final int originalMouseY = mouseY;

        mouseX = originalMouseX - this.leftPos;
        mouseY = originalMouseY - this.topPos;

        if (Tab.REDSTONE.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= SPACING;

            if (mouseIn(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                graphics.renderTooltip(this.font, RedstoneMode.IGNORE.getName(), originalMouseX, originalMouseY);
            }

            if (mouseIn(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                graphics.renderTooltip(this.font, RedstoneMode.LOW.getName(), originalMouseX, originalMouseY);
            }

            if (mouseIn(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                graphics.renderTooltip(this.font, RedstoneMode.HIGH.getName(), originalMouseX, originalMouseY);
            }
        } else {
            mouseX += TAB_WIDTH;
            mouseY -= SPACING;

            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.REDSTONE_MODE).setStyle(Constant.Text.RED_STYLE), originalMouseX, originalMouseY);
            }
        }

        mouseX = originalMouseX - this.leftPos;
        mouseY = originalMouseY - this.topPos;

        if (Tab.CONFIGURATION.isOpen()) {
            mouseX += PANEL_WIDTH;
            mouseY -= TAB_HEIGHT + SPACING * 2;

            if (mouseIn(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.TOP, originalMouseX, originalMouseY);
            }

            if (mouseIn(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.LEFT, originalMouseX, originalMouseY);
            }

            if (mouseIn(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.FRONT, originalMouseX, originalMouseY);
            }

            if (mouseIn(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.RIGHT, originalMouseX, originalMouseY);
            }

            if (mouseIn(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.BACK, originalMouseX, originalMouseY);
            }

            if (mouseIn(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
                this.renderFaceTooltip(graphics, BlockFace.BOTTOM, originalMouseX, originalMouseY);
            }
        } else {
            mouseX += TAB_WIDTH;
            mouseY -= (Tab.REDSTONE.isOpen() ? PANEL_HEIGHT : TAB_HEIGHT) + SPACING * 2;

            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.CONFIGURATION).setStyle(Constant.Text.BLUE_STYLE), originalMouseX, originalMouseY);
            }
        }

        mouseX = originalMouseX - this.leftPos - this.imageWidth;
        mouseY = originalMouseY - this.topPos - SPACING;

        if (Tab.STATS.isOpen()) {
            if (mouseIn(mouseX, mouseY, OWNER_FACE_X, OWNER_FACE_Y, OWNER_FACE_SIZE, OWNER_FACE_SIZE)) {
                final GameProfile ownerProfile = this.owner.getNow(null);

                if (ownerProfile != null) {
                    TOOLTIP_ARRAY.add(Component.literal(ownerProfile.getName()));

                    if (Screen.hasControlDown()) {
                        TOOLTIP_ARRAY.add(Component.literal(ownerProfile.getId().toString()).withStyle(Constant.Text.DARK_GRAY_STYLE));
                    }

                    graphics.renderComponentTooltip(this.font, TOOLTIP_ARRAY, originalMouseX, originalMouseY);
                    TOOLTIP_ARRAY.clear();
                }
            }
        } else if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
            graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.STATISTICS).setStyle(Constant.Text.YELLOW_STYLE), originalMouseX, originalMouseY);
        }

        mouseX = originalMouseX - this.leftPos - this.imageWidth;
        mouseY = originalMouseY - this.topPos;

        if (Tab.SECURITY.isOpen()) {
            mouseY -= TAB_HEIGHT + SPACING * 2;

            if (this.security().isOwner(this.machinePlayer())) {
                if (mouseIn(mouseX, mouseY, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    graphics.renderTooltip(this.font, AccessLevel.PUBLIC.getName(), originalMouseX, originalMouseY);
                }

                if (mouseIn(mouseX, mouseY, SECURITY_TEAM_X, SECURITY_TEAM_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    graphics.renderTooltip(this.font, AccessLevel.TEAM.getName(), originalMouseX, originalMouseY);
                }

                if (mouseIn(mouseX, mouseY, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    graphics.renderTooltip(this.font, AccessLevel.PRIVATE.getName(), originalMouseX, originalMouseY);
                }
            } else if (mouseIn(mouseX, mouseY, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    || mouseIn(mouseX, mouseY, SECURITY_TEAM_X, SECURITY_TEAM_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    || mouseIn(mouseX, mouseY, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.ACCESS_DENIED), originalMouseX, originalMouseY);
            }
        } else {
            mouseY -= (Tab.STATS.isOpen() ? PANEL_HEIGHT : TAB_HEIGHT) + SPACING * 2;

            if (mouseIn(mouseX, mouseY, 0, 0, TAB_WIDTH, TAB_HEIGHT)) {
                graphics.renderTooltip(this.font, Component.translatable(Constant.TranslationKey.SECURITY).setStyle(Constant.Text.GREEN_STYLE), originalMouseX, originalMouseY);
            }
        }
    }

    /**
     * Renders a face tooltip.
     *
     * @param graphics GUI graphics
     * @param face face
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    protected void renderFaceTooltip(
            final GuiGraphics graphics,
            final BlockFace face,
            final int mouseX,
            final int mouseY
    ) {
        TOOLTIP_ARRAY.add(face.getName());

        final IOFace configuredFace = this.configuration().get(face);

        if (configuredFace.getType() != ResourceType.NONE) {
            TOOLTIP_ARRAY.add(configuredFace.getType().getName().copy().append(" ").append(configuredFace.getFlow().getName()));
        }

        graphics.renderComponentTooltip(this.font, TOOLTIP_ARRAY, mouseX, mouseY);
        TOOLTIP_ARRAY.clear();
    }

    /**
     * Renders the screen.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param delta tick delta
     */
    @Override
    public final void render(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY,
            final float delta
    ) {
        super.render(graphics, mouseX, mouseY, delta);

        this.renderForeground(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * Renders the shared 3D port preview widget.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    protected void renderPortPreview(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY
    ) {
        if (this.portPreviewWidget == null || this.portPreviewScene == null) {
            return;
        }

        this.portPreviewWidget.render(
                graphics,
                this.portPreviewScene,
                mouseX,
                mouseY
        );
    }

    /**
     * Renders additional foreground content.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param delta tick delta
     */
    protected void renderForeground(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY,
            final float delta
    ) {
        this.renderPortPreview(
                graphics,
                mouseX,
                mouseY
        );
    }

    /**
     * Renders the background.
     *
     * @param graphics GUI graphics
     * @param delta tick delta
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    @Override
    protected final void renderBg(
            final GuiGraphics graphics,
            final float delta,
            final int mouseX,
            final int mouseY
    ) {
        graphics.blit(this.texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        this.renderMachineBackground(graphics, mouseX, mouseY, delta);
        this.drawTanks(graphics, mouseX, mouseY);
        this.drawCapacitor(graphics, mouseX, mouseY);
        this.handleSlotHighlight(graphics, mouseX, mouseY);
        this.drawConfigurationPanels(graphics, mouseX, mouseY);
    }

    /**
     * Draws the capacitor.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    protected void drawCapacitor(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY
    ) {
        final long capacity = this.energyStorage().getCapacity();

        if (capacity > 0 && this.capacitorHeight != 0) {
            final int x = this.leftPos + this.capacitorX;
            final int y = this.topPos + this.capacitorY;
            final long amount = this.energyStorage().getAmount();

            GraphicsUtil.drawCapacitor(graphics, x, y, capacity, amount, false);

            if (mouseIn(mouseX, mouseY, x - 1, y - 1, OVERLAY_WIDTH + 2, this.capacitorHeight + 2)) {
                final List<Component> lines = new ArrayList<>();

                this.appendEnergyTooltip(lines);
                this.setTooltipForNextRenderPass(Lists.transform(lines, Component::getVisualOrderText));
            }
        }
    }

    /**
     * Renders additional machine background content.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param delta tick delta
     */
    protected void renderMachineBackground(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY,
            final float delta
    ) {

    }

    /**
     * Draws fluid tanks.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    protected void drawTanks(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(this.leftPos, this.topPos, 0);

        this.hoveredTank = null;

        for (final Tank tank : this.tanks()) {
            if (tank.getHeight() > 0 && tank.getWidth() > 0) {
                if (tank.getAmount() > 0) {
                    GraphicsUtil.drawFluid(
                            graphics,
                            tank.getX(),
                            tank.getY(),
                            tank.getWidth(),
                            tank.getHeight(),
                            tank.getCapacity(),
                            tank.createVariant(),
                            tank.getAmount()
                    );
                }

                if (tank.isMarked()) {
                    boolean primary = true;

                    for (int y = tank.getY() + tank.getHeight() - 2; y > tank.getY(); y -= 3) {
                        graphics.hLine(tank.getX(), tank.getX() + Mth.ceil(primary ? tank.getWidth() / 2.5 : tank.getWidth() / 3.5), y, 0xFFB31212);
                        primary = !primary;
                    }
                }

                if (this.hoveredTank == null && mouseIn(mouseX, mouseY, this.leftPos + tank.getX() - 1, this.topPos + tank.getY() - 1, tank.getWidth() + 2, tank.getHeight() + 2)) {
                    this.hoveredTank = tank;

                    RenderSystem.disableDepthTest();
                    graphics.fill(tank.getX(), tank.getY(), tank.getX() + tank.getWidth(), tank.getY() + tank.getHeight(), 0x80FFFFFF);
                    RenderSystem.enableDepthTest();
                }
            }
        }

        graphics.pose().popPose();

        for (final Tank tank : this.tanks()) {
            if (mouseIn(mouseX, mouseY, this.leftPos + tank.getX() - 1, this.topPos + tank.getY() - 1, tank.getWidth() + 2, tank.getHeight() + 2)) {
                this.setTooltipForNextRenderPass(Lists.transform(tank.getTooltip(), Component::getVisualOrderText));
                break;
            }
        }
    }

    @ApiStatus.Internal
    private void handleSlotHighlight(
            final GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        if (!Tab.CONFIGURATION.isOpen()) {
            return;
        }

        mouseX -= this.leftPos - PANEL_WIDTH;
        mouseY -= this.topPos + TAB_HEIGHT + SPACING * 2;

        IOFace config = null;

        if (mouseIn(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
            config = this.configuration().get(BlockFace.TOP);
        } else if (mouseIn(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
            config = this.configuration().get(BlockFace.LEFT);
        } else if (mouseIn(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
            config = this.configuration().get(BlockFace.FRONT);
        } else if (mouseIn(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
            config = this.configuration().get(BlockFace.RIGHT);
        } else if (mouseIn(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
            config = this.configuration().get(BlockFace.BACK);
        } else if (mouseIn(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, MACHINE_FACE_SIZE, MACHINE_FACE_SIZE)) {
            config = this.configuration().get(BlockFace.BOTTOM);
        }

        if (config == null) {
            return;
        }

        final ResourceType resource = config.getType();

        if (resource.willAcceptResource(ResourceType.ITEM)) {
            for (final Slot slot : this.menu.slots) {
                if (slot instanceof StorageSlot storageSlot) {
                    final TransferType type = storageSlot.getWrapped().transferMode();

                    if (type.getExternalFlow() != null && type.getExternalFlow().canFlowIn(config.getFlow())) {
                        GraphicsUtil.highlightElement(graphics, this.leftPos, this.topPos, slot.x, slot.y, 16, 16, type.color());
                    }
                }
            }
        }

        if (resource.willAcceptResource(ResourceType.FLUID)) {
            for (final Tank tank : this.tanks()) {
                final TransferType type = tank.getInputType();

                if (type.getExternalFlow() != null && type.getExternalFlow().canFlowIn(config.getFlow())) {
                    GraphicsUtil.highlightElement(graphics, this.leftPos, this.topPos, tank.getX(), tank.getY(), tank.getWidth(), tank.getHeight(), type.color());
                }
            }
        }

        if (resource.willAcceptResource(ResourceType.ENERGY)) {
            RenderSystem.enableBlend();
            GraphicsUtil.highlightElement(graphics, this.leftPos, this.topPos, this.capacitorX, this.capacitorY, 16, this.capacitorHeight, 0x00F6FF00);
        }
    }

    /**
     * Handles mouse clicks.
     *
     * @param mouseX mouse x
     * @param mouseY mouse y
     * @param button button
     * @return true if handled
     */
    @Override
    public boolean mouseClicked(
            final double mouseX,
            final double mouseY,
            final int button
    ) {
        if (this.hoveredTank != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (this.hoveredTank.acceptStack(ContainerItemContext.ofPlayerCursor(this.playerInventory().player, this.menu))) {
                PacketSender.c2s().send(new TankInteractionPayload(this.menu.containerId, this.hoveredTank.getIndex()));
            }

            return true;
        }

        if (this.portPreviewWidget != null && this.portPreviewWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return this.checkConfigurationPanelClick(mouseX, mouseY, button) | super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Renders tooltips.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    @Override
    protected void renderTooltip(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY
    ) {
        super.renderTooltip(graphics, mouseX, mouseY);
        this.drawConfigurationPanelTooltips(graphics, mouseX, mouseY);
    }

    /**
     * Plays a button click sound.
     */
    protected void playButtonSound() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    /**
     * Renders labels.
     *
     * @param graphics GUI graphics
     * @param mouseX mouse x
     * @param mouseY mouse y
     */
    @Override
    protected final void renderLabels(
            final GuiGraphics graphics,
            final int mouseX,
            final int mouseY
    ) {
        this.drawTitle(graphics);
    }

    public int getX() {
        return this.leftPos;
    }

    public int getY() {
        return this.topPos;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    private void modifyFace(
            final int button,
            final BlockFace face
    ) {
        if (this.isFaceLocked(face)) {
            return;
        }

        final boolean reverse = button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                ? Screen.hasShiftDown()
                : !Screen.hasShiftDown();

        final boolean reset = button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE || Screen.hasControlDown();

        ClientPlayNetworking.send(new SideConfigurationClickPayload(face, reverse, reset));
        this.cycleFaceConfig(face, reverse, reset);
        this.playButtonSound();
    }

    @Override
    public boolean mouseReleased(
            final double mouseX,
            final double mouseY,
            final int button
    ) {
        if (this.portPreviewWidget != null && this.portPreviewWidget.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(
            final double mouseX,
            final double mouseY,
            final int button,
            final double deltaX,
            final double deltaY
    ) {
        if (this.portPreviewWidget != null && this.portPreviewWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(
            final double mouseX,
            final double mouseY,
            final double horizontalAmount,
            final double verticalAmount
    ) {
        if (this.portPreviewWidget != null && this.portPreviewWidget.mouseScrolled(
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

    /**
     * Configuration panel tab state.
     */
    public enum Tab {
        REDSTONE(TAB_REDSTONE_U, TAB_REDSTONE_V, PANEL_REDSTONE_U, PANEL_REDSTONE_V, true, AbstractMachineScreen.REDSTONE),
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

        Tab(
                final int tabU,
                final int tabV,
                final int panelU,
                final int panelV,
                final boolean left,
                final ItemStack item
        ) {
            this.tabU = tabU;
            this.tabV = tabV;
            this.panelU = panelU;
            this.panelV = panelV;
            this.left = left;
            this.item = item;
        }

        public int getU() {
            return this.open ? this.panelU : this.tabU;
        }

        public boolean isLeft() {
            return this.left;
        }

        public int getV() {
            return this.open ? this.panelV : this.tabV;
        }

        public boolean isOpen() {
            return this.open;
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