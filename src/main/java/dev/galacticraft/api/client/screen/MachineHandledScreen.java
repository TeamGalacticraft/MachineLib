/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.api.client.screen;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Either;
import dev.galacticraft.api.block.ConfiguredMachineFace;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.block.util.BlockFace;
import dev.galacticraft.api.client.model.MachineModelRegistry;
import dev.galacticraft.api.machine.AccessLevel;
import dev.galacticraft.api.machine.MachineStatus;
import dev.galacticraft.api.machine.RedstoneActivation;
import dev.galacticraft.api.machine.storage.io.ConfiguredStorage;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.api.screen.MachineScreenHandler;
import dev.galacticraft.impl.MLConstant;
import dev.galacticraft.impl.client.util.DrawableUtil;
import dev.galacticraft.impl.machine.storage.slot.VanillaWrappedItemSlot;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * Handles most of the boilerplate code for machine screens.
 * Handles the rendering of tanks, configuration panels and capacitors.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
@Environment(EnvType.CLIENT)
public abstract class MachineHandledScreen<M extends MachineBlockEntity, H extends MachineScreenHandler<M>> extends AbstractContainerScreen<H> {
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

    /**
     * An array used for ordering tooltip text to avoid re-allocating multiple times per frame.
     * Not thread safe.
     */
    @ApiStatus.Internal
    private static final List<Component> TOOLTIP_ARRAY = new ArrayList<>();

    /**
     * The position of the machine.
     */
    protected final BlockPos pos;

    /**
     * The world the machine is in.
     */
    protected final Level world;

    /**
     * The machine this screen is attached to.
     */
    protected final M machine;

    /**
     * The tank that is currently hovered over.
     */
    protected @Nullable Tank focusedTank = null;

    /**
     * The skin of the owner of this machine.
     * Defaults to steve if the skin cannot be found.
     */
    private @NotNull ResourceLocation ownerSkin = new ResourceLocation("textures/entity/steve.png");

    /**
     * The sprite provider for the machine block. Used to render the machine on the IO configuration panel.
     */
    private final MachineModelRegistry.SpriteProvider spriteProvider;

    /**
     * The texture of the background screen.
     */
    private final @NotNull ResourceLocation texture;

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
     * Creates a new screen from the given screen handler.
     * @param handler The screen handler to create the screen from.
     * @param inv The inventory of the machine.
     * @param title The title of the screen.
     * @param texture The texture of the background screen.
     */
    protected MachineHandledScreen(@NotNull H handler, @NotNull Inventory inv, @NotNull Component title, @NotNull ResourceLocation texture) {
        super(handler, inv, title);
        this.pos = this.menu.machine.getBlockPos();
        this.world = inv.player.level;
        this.machine = this.menu.machine;
        this.texture = texture;

        this.spriteProvider = MachineModelRegistry.getSpriteProviderOrElseGet(this.machine.getBlockState() == null ? world.getBlockState(pos).getBlock() : this.machine.getBlockState().getBlock(), MachineModelRegistry.SpriteProvider.DEFAULT);

        Minecraft.getInstance().getSkinManager().registerSkins(this.machine.getSecurity().getOwner(), (type, identifier, tex) -> {
            if (type == MinecraftProfileTexture.Type.SKIN && identifier != null) {
                MachineHandledScreen.this.ownerSkin = identifier;
            }
        }, true);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    /**
     * Appends additional information to the capacitor's tooltip.
     * @param list The list to append to.
     */
    public void appendEnergyTooltip(List<Component> list) {
    }

    /**
     * Draws the configuration panels and their contents.
     * @param matrices The matrix stack.
     * @param mouseX The mouse's x-position.
     * @param mouseY The mouse's y-position.
     * @param delta The delta time.
     */
    protected void drawConfigurationPanels(@NotNull PoseStack matrices, int mouseX, int mouseY, float delta) {
        assert this.minecraft != null;
        if (this.machine != null) {
            final MachineBlockEntity machine = this.machine;
            boolean secondary = false;
            RenderSystem.setShaderTexture(0, MLConstant.ScreenTexture.MACHINE_CONFIG_PANELS);
            for (Tab tab : Tab.values()) { // 0, 1, 2, 3
                if (secondary) matrices.translate(0, SPACING, 0);
                this.blit(matrices, this.leftPos + (tab.isLeft() ? tab.isOpen() ? -MLConstant.TextureCoordinate.PANEL_WIDTH : -22 : this.imageWidth), this.topPos + (secondary ? Tab.values()[tab.ordinal() - 1].isOpen() ? MLConstant.TextureCoordinate.PANEL_HEIGHT : MLConstant.TextureCoordinate.TAB_HEIGHT : 0) + SPACING, tab.getU(), tab.getV(), tab.isOpen() ? MLConstant.TextureCoordinate.PANEL_WIDTH : MLConstant.TextureCoordinate.TAB_WIDTH, tab.isOpen() ? MLConstant.TextureCoordinate.PANEL_HEIGHT : MLConstant.TextureCoordinate.TAB_HEIGHT);
                if (secondary) matrices.translate(0, -SPACING, 0);
                secondary = !secondary;
            }
            matrices.pushPose();
            matrices.translate(this.leftPos, this.topPos, 0);

            if (Tab.REDSTONE.isOpen()) {
                matrices.pushPose();
                matrices.translate(-MLConstant.TextureCoordinate.PANEL_WIDTH, SPACING, 0);
                this.drawButton(matrices, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, mouseX + MLConstant.TextureCoordinate.PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, delta, machine.getRedstoneActivation() == RedstoneActivation.IGNORE);
                this.drawButton(matrices, REDSTONE_LOW_X, REDSTONE_LOW_Y, mouseX + MLConstant.TextureCoordinate.PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, delta, machine.getRedstoneActivation() == RedstoneActivation.LOW);
                this.drawButton(matrices, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, mouseX + MLConstant.TextureCoordinate.PANEL_WIDTH - this.leftPos, mouseY - SPACING - this.topPos, delta, machine.getRedstoneActivation() == RedstoneActivation.HIGH);
                this.renderItemIcon(matrices, PANEL_ICON_X, PANEL_ICON_Y, REDSTONE);
                this.renderItemIcon(matrices, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, GUNPOWDER);
                this.renderItemIcon(matrices, REDSTONE_LOW_X, REDSTONE_LOW_Y - 2, UNLIT_TORCH);
                this.renderItemIcon(matrices, REDSTONE_HIGH_X, REDSTONE_HIGH_Y - 2, REDSTONE_TORCH);

                this.font.drawShadow(matrices, Component.translatable(MLConstant.TranslationKey.REDSTONE_ACTIVATION)
                        .setStyle(MLConstant.Text.GRAY_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);
                this.font.drawShadow(matrices, Component.translatable(MLConstant.TranslationKey.REDSTONE_STATE,
                        machine.getRedstoneActivation().getName()).setStyle(MLConstant.Text.DARK_GRAY_STYLE), REDSTONE_STATE_TEXT_X, REDSTONE_STATE_TEXT_Y, 0xFFFFFFFF);
                this.font.drawShadow(matrices, Component.translatable(MLConstant.TranslationKey.REDSTONE_STATUS,
                        !machine.isDisabled(this.world) ? Component.translatable(MLConstant.TranslationKey.REDSTONE_ACTIVE).setStyle(MLConstant.Text.GREEN_STYLE)
                                : Component.translatable(MLConstant.TranslationKey.REDSTONE_DISABLED).setStyle(MLConstant.Text.DARK_RED_STYLE))
                        .setStyle(MLConstant.Text.DARK_GRAY_STYLE), REDSTONE_STATUS_TEXT_X, REDSTONE_STATUS_TEXT_Y + this.font.lineHeight, 0xFFFFFFFF);

                matrices.popPose();
            }
            if (Tab.CONFIGURATION.isOpen()) {
                matrices.pushPose();
                matrices.translate(-MLConstant.TextureCoordinate.PANEL_WIDTH, MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING, 0);
                this.renderItemIcon(matrices, PANEL_ICON_X, PANEL_ICON_Y, WRENCH);
                this.font.drawShadow(matrices, Component.translatable(MLConstant.TranslationKey.CONFIGURATION)
                        .setStyle(MLConstant.Text.GRAY_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);

                RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                this.drawMachineFace(matrices, TOP_FACE_X, TOP_FACE_Y, machine, BlockFace.TOP);
                this.drawMachineFace(matrices, LEFT_FACE_X, LEFT_FACE_Y, machine, BlockFace.LEFT);
                this.drawMachineFace(matrices, FRONT_FACE_X, FRONT_FACE_Y, machine, BlockFace.FRONT);
                this.drawMachineFace(matrices, RIGHT_FACE_X, RIGHT_FACE_Y, machine, BlockFace.RIGHT);
                this.drawMachineFace(matrices, BACK_FACE_X, BACK_FACE_Y, machine, BlockFace.BACK);
                this.drawMachineFace(matrices, BOTTOM_FACE_X, BOTTOM_FACE_Y, machine, BlockFace.BOTTOM);
                matrices.popPose();
            }
            if (Tab.STATS.isOpen()) {
                matrices.pushPose();
                matrices.translate(this.imageWidth, SPACING, 0);
                this.renderItemIcon(matrices, PANEL_ICON_X, PANEL_ICON_Y, ALUMINUM_WIRE);
                RenderSystem.setShaderTexture(0, this.ownerSkin);
                blit(matrices, OWNER_FACE_X, OWNER_FACE_Y, MLConstant.TextureCoordinate.OWNER_FACE_WIDTH, MLConstant.TextureCoordinate.OWNER_FACE_HEIGHT, 8, 8, 8, 8, 64, 64);
                this.font.drawShadow(matrices, Component.translatable(MLConstant.TranslationKey.STATISTICS)
                        .setStyle(MLConstant.Text.GREEN_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);
                List<FormattedCharSequence> text = this.font.split(Component.translatable((machine.getBlockState() != null ? machine.getBlockState()
                        : this.machine.getBlockState()).getBlock().getDescriptionId()), 64);
                int offsetY = 0;
                for (FormattedCharSequence orderedText : text) {
                    this.font.draw(matrices, orderedText, 40, 22 + offsetY, 0xFFFFFFFF);
                    offsetY += this.font.lineHeight + 2;
                }
//                this.textRenderer.draw(matrices, Text.translatable("ui.galacticraft.machine.stats.gjt", "N/A")
//                        .setStyle(Constants.Text.GRAY_STYLE), 11, 54, ColorUtils.WHITE);
                //                this.textRenderer.draw(matrices, Text.translatable("ui.galacticraft.machine.stats.todo", "N/A")
//                        .setStyle(Constants.Text.GRAY_STYLE), 11, 54, ColorUtils.WHITE);
                matrices.popPose();
            }

            if (Tab.SECURITY.isOpen()) {
                matrices.pushPose();
                matrices.translate(this.imageWidth, MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING, 0);
                RenderSystem.setShaderTexture(0, MLConstant.ScreenTexture.MACHINE_CONFIG_PANELS);
                this.blit(matrices, PANEL_ICON_X, PANEL_ICON_Y, MLConstant.TextureCoordinate.ICON_LOCK_PRIVATE_U, MLConstant.TextureCoordinate.ICON_LOCK_PRIVATE_V, MLConstant.TextureCoordinate.ICON_WIDTH, MLConstant.TextureCoordinate.ICON_HEIGHT);

                this.drawButton(matrices, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING) - this.topPos, delta, machine.getSecurity().getAccessLevel() == AccessLevel.PUBLIC || !machine.getSecurity().isOwner(this.menu.player));
                this.drawButton(matrices, SECURITY_TEAM_X, SECURITY_TEAM_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING) - this.topPos, delta, machine.getSecurity().getAccessLevel() == AccessLevel.TEAM || !machine.getSecurity().isOwner(this.menu.player));
                this.drawButton(matrices, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, mouseX - this.imageWidth - this.leftPos, mouseY - (MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING) - this.topPos, delta, machine.getSecurity().getAccessLevel() == AccessLevel.PRIVATE || !machine.getSecurity().isOwner(this.menu.player));
                this.blit(matrices, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, MLConstant.TextureCoordinate.ICON_LOCK_PRIVATE_U, MLConstant.TextureCoordinate.ICON_LOCK_PRIVATE_V, MLConstant.TextureCoordinate.ICON_WIDTH, MLConstant.TextureCoordinate.ICON_HEIGHT);
                this.blit(matrices, SECURITY_TEAM_X, SECURITY_TEAM_Y, MLConstant.TextureCoordinate.ICON_LOCK_PARTY_U, MLConstant.TextureCoordinate.ICON_LOCK_PARTY_V, MLConstant.TextureCoordinate.ICON_WIDTH, MLConstant.TextureCoordinate.ICON_HEIGHT);
                this.blit(matrices, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, MLConstant.TextureCoordinate.ICON_LOCK_PUBLIC_U, MLConstant.TextureCoordinate.ICON_LOCK_PUBLIC_V, MLConstant.TextureCoordinate.ICON_WIDTH, MLConstant.TextureCoordinate.ICON_HEIGHT);

                this.font.drawShadow(matrices, Component.translatable(MLConstant.TranslationKey.SECURITY)
                        .setStyle(MLConstant.Text.GRAY_STYLE), PANEL_TITLE_X, PANEL_TITLE_Y, 0xFFFFFFFF);
                this.font.drawShadow(matrices, Component.translatable(MLConstant.TranslationKey.ACCESS_LEVEL,
                        machine.getSecurity().getAccessLevel().getName()).setStyle(MLConstant.Text.GRAY_STYLE), SECURITY_STATE_TEXT_X, SECURITY_STATE_TEXT_Y, 0xFFFFFFFF);
//                assert machine.getSecurity().getOwner() != null;
//                this.textRenderer.drawWithShadow(matrices, Text.translatable("ui.galacticraft.machine.security.owned_by", machine.getSecurity().getOwner().getName())
//                        .setStyle(Constants.Text.GRAY_STYLE), SECURITY_STATE_TEXT_X, SECURITY_STATE_TEXT_Y + this.textRenderer.fontHeight + 4, ColorUtils.WHITE);

                matrices.popPose();
            }
            matrices.popPose();
        }
    }

    /**
     * Draws the title of the machine.
     * @param matrices the matrix stack
     * @see #titleLabelX
     * @see #titleLabelY
     */
    protected void drawTitle(@NotNull PoseStack matrices) {
        this.font.draw(matrices, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFFFF);
    }

    /**
     * Draws the sprite of a given machine face.
     * @param matrices the matrix stack
     * @param x the x position to draw at
     * @param y the y position to draw at
     * @param machine the machine to draw
     * @param face the face to draw
     */
    private void drawMachineFace(@NotNull PoseStack matrices, int x, int y, @NotNull MachineBlockEntity machine, @NotNull BlockFace face) {
        ConfiguredMachineFace machineFace = machine.getIOConfig().get(face);
        blit(matrices, x, y, 0, 16, 16, MachineModelRegistry.getSprite(face, machine, null, this.spriteProvider, machineFace.getType(), machineFace.getFlow()));
    }

    /**
     * Renders the icon of the given item, without any extra effects.
     * @param matrices the matrix stack
     * @param x the x position to draw at
     * @param y the y position to draw at
     * @param stack the item to render
     */
    private void renderItemIcon(@NotNull PoseStack matrices, int x, int y, @NotNull ItemStack stack) {
        assert this.minecraft != null;
        BakedModel model = this.itemRenderer.getModel(stack, this.world, this.menu.player, 8910823);
        matrices.pushPose();
        this.minecraft.getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        matrices.translate(x + 8, y + 8, 100.0F + this.getBlitOffset());
        matrices.scale(16, -16, 16);
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean bl = !model.usesBlockLight();
        if (bl) {
            Lighting.setupForFlatItems();
        }

        this.itemRenderer.render(stack, ItemTransforms.TransformType.GUI, false, matrices, immediate, 15728880, OverlayTexture.NO_OVERLAY, model);
        immediate.endBatch();
        if (bl) {
            Lighting.setupFor3DItems();
        }
        matrices.popPose();
    }

    /**
     * Draws a 16x16 button at the given position.
     * The button will be highlighted if the mouse is hovering over it.
     * @param matrices the matrix stack
     * @param x the x-position to draw at
     * @param y the y-position to draw at
     * @param mouseX the mouse's x-position
     * @param mouseY the mouse's y-position
     * @param delta the delta time
     * @param pressed whether the button is pressed
     */
    public void drawButton(PoseStack matrices, int x, int y, double mouseX, double mouseY, float delta, boolean pressed) {
        assert this.minecraft != null;
        RenderSystem.setShaderTexture(0, MLConstant.ScreenTexture.MACHINE_CONFIG_PANELS);
        if (pressed) {
            this.blit(matrices, x, y, MLConstant.TextureCoordinate.BUTTON_U, MLConstant.TextureCoordinate.BUTTON_PRESSED_V, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT);
            return;
        }
        if (DrawableUtil.isWithin(mouseX, mouseY, x, y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
            this.blit(matrices, x, y, MLConstant.TextureCoordinate.BUTTON_U, MLConstant.TextureCoordinate.BUTTON_HOVERED_V, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT);
        } else {
            this.blit(matrices, x, y, MLConstant.TextureCoordinate.BUTTON_U, MLConstant.TextureCoordinate.BUTTON_V, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT);
        }
    }

    /**
     * Handles mouse input for the configuration panels.
     * @param mouseX the mouse's x-position
     * @param mouseY the mouse's y-position
     * @param button the button code that was pressed
     * @return whether the button was handled
     * @see GLFW
     */
    public boolean checkConfigurationPanelClick(double mouseX, double mouseY, int button) {
        assert this.minecraft != null;
        assert this.machine != null;

        final double mX = mouseX, mY = mouseY;
        final MachineBlockEntity machine = this.machine;
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.REDSTONE.isOpen()) {
            mouseX += MLConstant.TextureCoordinate.PANEL_WIDTH;
            mouseY -= SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.PANEL_WIDTH, MLConstant.TextureCoordinate.PANEL_UPPER_HEIGHT)) {
                Tab.REDSTONE.click();
                return true;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                this.setRedstone(RedstoneActivation.IGNORE);
                this.playButtonSound();
                return true;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                this.setRedstone(RedstoneActivation.LOW);
                this.playButtonSound();
                return true;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                this.setRedstone(RedstoneActivation.HIGH);
                this.playButtonSound();
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.PANEL_WIDTH, MLConstant.TextureCoordinate.PANEL_HEIGHT)) {
                    return true;
                }
            }
        } else {
            mouseX += MLConstant.TextureCoordinate.TAB_WIDTH;
            mouseY -= SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.TAB_WIDTH, MLConstant.TextureCoordinate.TAB_HEIGHT)) {
                Tab.REDSTONE.click();
                return true;
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX += MLConstant.TextureCoordinate.PANEL_WIDTH;
            mouseY -= MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.PANEL_WIDTH, MLConstant.TextureCoordinate.PANEL_UPPER_HEIGHT)) {
                Tab.CONFIGURATION.click();
                return true;
            }
            if (button >= GLFW.GLFW_MOUSE_BUTTON_LEFT && button <= GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                if (DrawableUtil.isWithin(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, 16, 16)) {
                    SideConfigurationAction.VALUES[button].update(this.minecraft.player, machine, BlockFace.TOP, Screen.hasShiftDown(), Screen.hasControlDown());
                    this.playButtonSound();
                    return true;
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, 16, 16)) {
                    SideConfigurationAction.VALUES[button].update(this.minecraft.player, machine, BlockFace.LEFT, Screen.hasShiftDown(), Screen.hasControlDown());
                    this.playButtonSound();
                    return true;
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, 16, 16)) {
                    SideConfigurationAction.VALUES[button].update(this.minecraft.player, machine, BlockFace.FRONT, Screen.hasShiftDown(), Screen.hasControlDown());
                    this.playButtonSound();
                    return true;
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, 16, 16)) {
                    SideConfigurationAction.VALUES[button].update(this.minecraft.player, machine, BlockFace.RIGHT, Screen.hasShiftDown(), Screen.hasControlDown());
                    this.playButtonSound();
                    return true;
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, 16, 16)) {
                    SideConfigurationAction.VALUES[button].update(this.minecraft.player, machine, BlockFace.BACK, Screen.hasShiftDown(), Screen.hasControlDown());
                    this.playButtonSound();
                    return true;
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, 16, 16)) {
                    SideConfigurationAction.VALUES[button].update(this.minecraft.player, machine, BlockFace.BOTTOM, Screen.hasShiftDown(), Screen.hasControlDown());
                    this.playButtonSound();
                    return true;
                }
            }
        } else {
            mouseX += MLConstant.TextureCoordinate.TAB_WIDTH;
            if (Tab.REDSTONE.isOpen()) {
                mouseY -= MLConstant.TextureCoordinate.PANEL_HEIGHT + SPACING + SPACING;
            } else {
                mouseY -= MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.TAB_WIDTH, MLConstant.TextureCoordinate.TAB_HEIGHT)) {
                Tab.CONFIGURATION.click();
                return true;
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        mouseX -= this.imageWidth;
        mouseY -= SPACING;
        if (Tab.STATS.isOpen()) {
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.PANEL_WIDTH, MLConstant.TextureCoordinate.PANEL_UPPER_HEIGHT)) {
                Tab.STATS.click();
                return true;
            }
        } else {
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.TAB_WIDTH, MLConstant.TextureCoordinate.TAB_HEIGHT)) {
                Tab.STATS.click();
                return true;
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        mouseX -= this.imageWidth;
        if (Tab.SECURITY.isOpen()) {
            mouseY -= MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.PANEL_WIDTH, MLConstant.TextureCoordinate.PANEL_UPPER_HEIGHT)) {
                Tab.SECURITY.click();
                return true;
            }

            if (machine.getSecurity().isOwner(this.menu.player)) {
                if (DrawableUtil.isWithin(mouseX, mouseY, SECURITY_PRIVATE_X, SECURITY_PRIVATE_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                    this.setAccessibility(AccessLevel.PRIVATE);
                    this.playButtonSound();
                    return true;
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, SECURITY_TEAM_X, SECURITY_TEAM_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                    this.setAccessibility(AccessLevel.TEAM);
                    this.playButtonSound();
                    return true;
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, SECURITY_PUBLIC_X, SECURITY_PUBLIC_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                    this.setAccessibility(AccessLevel.PUBLIC);
                    this.playButtonSound();
                    return true;
                }
            }
        } else {
            if (Tab.STATS.isOpen()) {
                mouseY -= MLConstant.TextureCoordinate.PANEL_HEIGHT + SPACING + SPACING;
            } else {
                mouseY -= MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.TAB_WIDTH, MLConstant.TextureCoordinate.TAB_HEIGHT)) {
                Tab.SECURITY.click();
            }
        }
        return false;
    }

    /**
     * Sets the accessibility of the machine and syncs it to the server.
     * @param accessLevel The accessibility to set.
     */
    protected void setAccessibility(@NotNull AccessLevel accessLevel) {
        this.machine.getSecurity().setAccessLevel(accessLevel);
        ClientPlayNetworking.send(new ResourceLocation(MLConstant.MOD_ID, "security_config"), new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(1, 1).writeByte(accessLevel.ordinal())));
    }

    /**
     * Sets the redstone mode of the machine and syncs it to the server.
     * @param redstone The redstone mode to set.
     */
    protected void setRedstone(@NotNull RedstoneActivation redstone) {
        this.machine.setRedstone(redstone);
        ClientPlayNetworking.send(new ResourceLocation(MLConstant.MOD_ID, "redstone_config"), new FriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(1, 1).writeByte(redstone.ordinal())));
    }

    /**
     * Draws the tooltips of the configuration panel.
     * @param matrices The matrices to use.
     * @param mouseX The mouse's x-position.
     * @param mouseY The mouse's y-position.
     */
    protected void drawConfigurationPanelTooltips(PoseStack matrices, int mouseX, int mouseY) {
        final MachineBlockEntity machine = this.machine;
        final int mX = mouseX, mY = mouseY;
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.REDSTONE.isOpen()) {
            mouseX += MLConstant.TextureCoordinate.PANEL_WIDTH;
            mouseY -= SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                this.renderTooltip(matrices, RedstoneActivation.IGNORE.getName(), mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                this.renderTooltip(matrices, RedstoneActivation.LOW.getName(), mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                this.renderTooltip(matrices, RedstoneActivation.HIGH.getName(), mX, mY);
            }
        } else {
            mouseX += MLConstant.TextureCoordinate.TAB_WIDTH;
            mouseY -= SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.TAB_WIDTH, MLConstant.TextureCoordinate.TAB_HEIGHT)) {
                this.renderTooltip(matrices, Component.translatable(MLConstant.TranslationKey.REDSTONE_ACTIVATION).setStyle(MLConstant.Text.RED_STYLE), mX, mY);
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX += MLConstant.TextureCoordinate.PANEL_WIDTH;
            mouseY -= MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, 16, 16)) {
                this.renderFaceTooltip(matrices, BlockFace.TOP, mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, 16, 16)) {
                this.renderFaceTooltip(matrices, BlockFace.LEFT, mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, 16, 16)) {
                this.renderFaceTooltip(matrices, BlockFace.FRONT, mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, 16, 16)) {
                this.renderFaceTooltip(matrices, BlockFace.RIGHT, mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, 16, 16)) {
                this.renderFaceTooltip(matrices, BlockFace.BACK, mX, mY);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, 16, 16)) {
                this.renderFaceTooltip(matrices, BlockFace.BOTTOM, mX, mY);
            }
        } else {
            mouseX += MLConstant.TextureCoordinate.TAB_WIDTH;
            if (Tab.REDSTONE.isOpen()) {
                mouseY -= MLConstant.TextureCoordinate.PANEL_HEIGHT + SPACING;
            } else {
                mouseY -= MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.TAB_WIDTH, MLConstant.TextureCoordinate.TAB_HEIGHT)) {
                this.renderTooltip(matrices, Component.translatable(MLConstant.TranslationKey.CONFIGURATION).setStyle(MLConstant.Text.BLUE_STYLE), mX, mY);
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        mouseX -= this.imageWidth;
        mouseY -= SPACING;
        if (Tab.STATS.isOpen()) {
            if (DrawableUtil.isWithin(mouseX, mouseY, OWNER_FACE_X, OWNER_FACE_Y, MLConstant.TextureCoordinate.OWNER_FACE_WIDTH, MLConstant.TextureCoordinate.OWNER_FACE_HEIGHT)) {
                assert machine.getSecurity().getOwner() != null;
                this.renderTooltip(matrices, Component.literal(machine.getSecurity().getOwner().getName()), mX, mY);
            }
        } else {
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.TAB_WIDTH, MLConstant.TextureCoordinate.TAB_HEIGHT)) {
                this.renderTooltip(matrices, Component.translatable(MLConstant.TranslationKey.STATISTICS).setStyle(MLConstant.Text.YELLOW_STYLE), mX, mY);
            }
        }
        mouseX = mX - this.leftPos;
        mouseY = mY - this.topPos;
        if (Tab.SECURITY.isOpen()) {
            mouseX -= this.imageWidth;
            mouseY -= MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING;

            if (machine.getSecurity().isOwner(this.menu.player)) {
                if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                    this.renderTooltip(matrices, AccessLevel.PRIVATE.getName(), mX, mY);
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                    this.renderTooltip(matrices, AccessLevel.TEAM.getName(), mX, mY);
                }
                if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                    this.renderTooltip(matrices, AccessLevel.PUBLIC.getName(), mX, mY);
                }
            } else {
                if (DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_IGNORE_X, REDSTONE_IGNORE_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)
                    || DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_LOW_X, REDSTONE_LOW_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)
                    || DrawableUtil.isWithin(mouseX, mouseY, REDSTONE_HIGH_X, REDSTONE_HIGH_Y, MLConstant.TextureCoordinate.BUTTON_WIDTH, MLConstant.TextureCoordinate.BUTTON_HEIGHT)) {
                    this.renderTooltip(matrices, Component.translatable(MLConstant.TranslationKey.ACCESS_DENIED), mX, mY);
                }
            }
        } else {
            mouseX -= this.imageWidth;
            if (Tab.STATS.isOpen()) {
                mouseY -= MLConstant.TextureCoordinate.PANEL_HEIGHT + SPACING + SPACING;
            } else {
                mouseY -= MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING + SPACING;
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, 0, 0, MLConstant.TextureCoordinate.TAB_WIDTH, MLConstant.TextureCoordinate.TAB_HEIGHT)) {
                this.renderTooltip(matrices, Component.translatable(MLConstant.TranslationKey.SECURITY).setStyle(MLConstant.Text.BLUE_STYLE), mX, mY);
            }
        }
    }

    /**
     * Renders the tooltip for the given face.
     * @param matrices The matrix stack
     * @param face The face to render the tooltip for
     * @param mouseX The mouse's x-position
     * @param mouseY The mouse's y-position
     */
    protected void renderFaceTooltip(PoseStack matrices, @NotNull BlockFace face, int mouseX, int mouseY) {
        TOOLTIP_ARRAY.add(face.getName());
        ConfiguredMachineFace configuredFace = this.machine.getIOConfig().get(face);
        if (configuredFace.getType() != ResourceType.NONE) {
            TOOLTIP_ARRAY.add(configuredFace.getType().getName().copy().append(" ").append(configuredFace.getFlow().getName()));
        }
        if (configuredFace.getMatching() != null) {
            if (configuredFace.getMatching().left().isPresent()) {
                TOOLTIP_ARRAY.add(Component.translatable(MLConstant.TranslationKey.MATCHES, Component.literal(String.valueOf(configuredFace.getMatching().left().get())).setStyle(MLConstant.Text.AQUA_STYLE)).setStyle(MLConstant.Text.GRAY_STYLE));
            } else {
                assert configuredFace.getMatching().right().isPresent();
                TOOLTIP_ARRAY.add(Component.translatable(MLConstant.TranslationKey.MATCHES, configuredFace.getMatching().right().get().getName()).setStyle(MLConstant.Text.GRAY_STYLE));
            }
        }
        this.renderComponentTooltip(matrices, TOOLTIP_ARRAY, mouseX, mouseY);

        TOOLTIP_ARRAY.clear();
    }

    @Override
    public final void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        assert this.minecraft != null;
        if (this.machine == null || !this.machine.getSecurity().hasAccess(menu.player)) {
            this.onClose();
            return;
        }

        super.render(matrices, mouseX, mouseY, delta);

        this.renderForeground(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    /**
     * Renders the foreground of the screen.
     * @param matrices The matrix stack
     * @param mouseX The mouse's x-position
     * @param mouseY The mouse's y-position
     * @param delta The delta time
     */
    protected void renderForeground(PoseStack matrices, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected final void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        this.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.texture);

        this.blit(matrices, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.renderBackground(matrices, mouseX, mouseY, delta);
        this.drawConfigurationPanels(matrices, mouseX, mouseY, delta);
        this.drawTanks(matrices, mouseX, mouseY, delta);
        this.drawCapacitor(matrices, mouseX, mouseY, delta);
        this.handleSlotHighlight(matrices, mouseX, mouseY, delta);
    }

    /**
     * Draws the capacitor of this machine.
     * If the machine has no capacitor, this method does nothing.
     * @param matrices The matrix stack
     * @param mouseX The mouse's x-position
     * @param mouseY The mouse's y-position
     * @param delta The delta time
     */
    protected void drawCapacitor(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (this.machine.energyStorage().getCapacity() > 0) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, MLConstant.ScreenTexture.OVERLAY_BARS);
            DrawableUtil.drawProgressTexture(matrices, this.leftPos + this.capacitorX, this.topPos + this.capacitorY, 0.01f, MLConstant.TextureCoordinate.ENERGY_BACKGROUND_X, MLConstant.TextureCoordinate.ENERGY_BACKGROUND_Y, MLConstant.TextureCoordinate.OVERLAY_WIDTH, MLConstant.TextureCoordinate.OVERLAY_HEIGHT, MLConstant.TextureCoordinate.OVERLAY_TEX_WIDTH, MLConstant.TextureCoordinate.OVERLAY_TEX_HEIGHT);
            float scale = (float) ((double) this.machine.energyStorage().getAmount() / (double) this.machine.energyStorage().getCapacity());
            DrawableUtil.drawProgressTexture(matrices, this.leftPos + this.capacitorX, (this.topPos + this.capacitorY + this.capacitorHeight - (this.capacitorHeight * scale)), 0.02f, MLConstant.TextureCoordinate.ENERGY_X, MLConstant.TextureCoordinate.ENERGY_Y, MLConstant.TextureCoordinate.OVERLAY_WIDTH, MLConstant.TextureCoordinate.OVERLAY_HEIGHT * scale, MLConstant.TextureCoordinate.OVERLAY_TEX_WIDTH, MLConstant.TextureCoordinate.OVERLAY_TEX_HEIGHT);

            if (DrawableUtil.isWithin(mouseX, mouseY, this.leftPos + this.capacitorX, this.topPos + this.capacitorY, 16, this.capacitorHeight)) {
                List<Component> lines = new ArrayList<>();
                MachineStatus status = this.machine.getStatus();
                if (status != MachineStatus.INVALID) {
                    lines.add(Component.translatable(MLConstant.TranslationKey.STATUS).setStyle(MLConstant.Text.GRAY_STYLE).append(status.name()));
                }
                lines.add(Component.translatable(MLConstant.TranslationKey.CURRENT_ENERGY, DrawableUtil.getEnergyDisplay(this.machine.energyStorage().getAmount()).setStyle(MLConstant.Text.BLUE_STYLE)).setStyle(MLConstant.Text.GOLD_STYLE));
                lines.add(Component.translatable(MLConstant.TranslationKey.MAX_ENERGY, DrawableUtil.getEnergyDisplay(this.machine.energyStorage().getCapacity()).setStyle(MLConstant.Text.BLUE_STYLE)).setStyle(MLConstant.Text.RED_STYLE));
                this.appendEnergyTooltip(lines);

                assert this.minecraft != null;
                assert this.minecraft.screen != null;
                matrices.translate(0.0D, 0.0D, 1.0D);
                this.minecraft.screen.renderComponentTooltip(matrices, lines, mouseX, mouseY);
                matrices.translate(0.0D, 0.0D, -1.0D);
            }
        }
    }

    /**
     * Draws the background of this machine.
     * @param matrices The matrix stack
     * @param mouseX The mouse's x-position
     * @param mouseY The mouse's y-position
     * @param delta The delta time
     */
    protected void renderBackground(PoseStack matrices, int mouseX, int mouseY, float delta) {
    }

    /**
     * Draws the (fluid and gas) tanks of this machine.
     * @param matrices The matrix stack
     * @param mouseX The mouse's x-position
     * @param mouseY The mouse's y-position
     * @param delta The delta time
     */
    protected void drawTanks(PoseStack matrices, int mouseX, int mouseY, float delta) {
        assert this.minecraft != null;
        Int2IntArrayMap color = getTankColor(mouseX, mouseY);
        color.defaultReturnValue(0xFFFFFFFF);

        this.focusedTank = null;
        for (Tank tank : this.menu.tanks) {
            fill(matrices, this.leftPos + tank.getX(), this.topPos + tank.getY(), this.leftPos + tank.getX() + tank.getWidth(), this.topPos + tank.getY() + tank.getHeight(), 0xFF8B8B8B);

            if (tank.getAmount() > 0) {
                FluidVariant resource = tank.getResource();
                boolean fillFromTop = FluidVariantAttributes.isLighterThanAir(resource);
                TextureAtlasSprite sprite = FluidVariantRendering.getSprite(resource);
                int fluidColor = FluidVariantRendering.getColor(resource);

                if (sprite == null) {
                    sprite = FluidVariantRendering.getSprite(FluidVariant.of(Fluids.WATER));
                    fluidColor = -1;
                    if (sprite == null) throw new IllegalStateException("Water sprite is null");
                }
                RenderSystem.setShaderTexture(0, sprite.atlas().location());
                RenderSystem.setShaderColor(0xFF, fluidColor >> 16 & 0xFF, fluidColor >> 8 & 0xFF, fluidColor & 0xFF);
                double v = (1.0 - ((double) tank.getAmount() / (double) tank.getCapacity()));
                if (!fillFromTop) {
                    DrawableUtil.drawTexturedQuad_F(matrices.last().pose(), this.leftPos, this.leftPos + tank.getWidth(), this.topPos + tank.getHeight(), (float) (this.topPos + (v * tank.getHeight())), tank.getWidth(), sprite.getU0(), sprite.getU1(), sprite.getV0(), (float) (sprite.getV0() + ((sprite.getV1() - sprite.getV0()) * v)));
                } else {
                    DrawableUtil.drawTexturedQuad_F(matrices.last().pose(), this.leftPos, this.leftPos + tank.getWidth(), this.topPos, (float) (this.topPos + ((1.0 - v) * tank.getHeight())), tank.getWidth(), sprite.getU0(), sprite.getU1(), sprite.getV0(), (float) (sprite.getV0() + ((sprite.getV1() - sprite.getV0()) * v)));
                }
            }

            boolean shorten = true;
            for (int y = this.topPos + tank.getY() + tank.getHeight() - 2; y > this.topPos + tank.getY(); y -= 3) {
                fill(matrices, this.leftPos + tank.getX(), y, this.leftPos + tank.getX() + (tank.getWidth() / 2) + ((shorten = !shorten) ? -(tank.getWidth() / 8) : 0), y - 1, 0xFFB31212);
            }
            if (this.focusedTank == null && DrawableUtil.isWithin(mouseX, mouseY, this.leftPos + tank.getX(), this.topPos + tank.getY(), tank.getWidth(), tank.getHeight())) {
                this.focusedTank = tank;
                RenderSystem.disableDepthTest();
                RenderSystem.colorMask(true, true, true, false);
                GuiComponent.fill(matrices, this.leftPos + tank.getX(), this.topPos + tank.getY(), this.leftPos + tank.getWidth(), this.topPos + tank.getHeight(), 0x80ffffff);
                RenderSystem.colorMask(true, true, true, true);
                RenderSystem.enableDepthTest();
            }
        }

        for (Tank tank : this.menu.tanks) {
            tank.drawTooltip(matrices, this.minecraft, this.leftPos, this.topPos, mouseX, mouseY);
        }

        color = getItemColor(mouseX, mouseY);
        color.defaultReturnValue(-1);
        for (Slot slot : this.menu.slots) {
            if (slot instanceof VanillaWrappedItemSlot) {
                int index = slot.getContainerSlot();
                if (color.get(index) != -1) {
                    RenderSystem.disableDepthTest();
                    int c = color.get(index);
                    c |= (255 << 24);
                    RenderSystem.colorMask(true, true, true, false);
                    fillGradient(matrices, this.leftPos + slot.x, this.topPos + slot.y, this.leftPos + slot.x + 16, this.topPos + slot.y + 16, c, c);
                    RenderSystem.colorMask(true, true, true, true);
                    RenderSystem.enableDepthTest();
                }
            }
        }
    }

    /**
     * Returns a map of the tank slot colors highlighted for the hovered configuration.
     * @param mouseX The mouse's x-position.
     * @param mouseY The mouse's y-position.
     * @return A map of the tank slot colors highlighted for the hovered configuration.
     */
    @ApiStatus.Internal
    private @NotNull Int2IntArrayMap getTankColor(int mouseX, int mouseY) {
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX -= this.leftPos - MLConstant.TextureCoordinate.PANEL_WIDTH;
            mouseY -= this.topPos + MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING;
            Int2IntArrayMap out = new Int2IntArrayMap();
            if (DrawableUtil.isWithin(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.TOP).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.TOP).getMatching(this.machine.fluidStorage()));
                groupFluid(out, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.LEFT).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.LEFT).getMatching(this.machine.fluidStorage()));
                groupFluid(out, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.FRONT).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.FRONT).getMatching(this.machine.fluidStorage()));
                groupFluid(out, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.RIGHT).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.RIGHT).getMatching(this.machine.fluidStorage()));
                groupFluid(out, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.BACK).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.BACK).getMatching(this.machine.fluidStorage()));
                groupFluid(out, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.BOTTOM).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.BOTTOM).getMatching(this.machine.fluidStorage()));
                groupFluid(out, list);
            }
            return out;
        }
        return new Int2IntArrayMap();
    }

    /**
     * Returns a map of the item slot colors highlighted for the hovered configuration.
     * @param mouseX The mouse's x-position.
     * @param mouseY The mouse's y-position.
     * @return A map of the item slot colors highlighted for the hovered configuration.
     */
    @ApiStatus.Internal
    protected Int2IntArrayMap getItemColor(int mouseX, int mouseY) {
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX -= this.leftPos - MLConstant.TextureCoordinate.PANEL_WIDTH;
            mouseY -= this.topPos + MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING;
            Int2IntArrayMap out = new Int2IntArrayMap();
            if (DrawableUtil.isWithin(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.TOP).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.TOP).getMatching(this.machine.itemStorage()));
                groupItem(out, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.LEFT).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.LEFT).getMatching(this.machine.itemStorage()));
                groupItem(out, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.FRONT).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.FRONT).getMatching(this.machine.itemStorage()));
                groupItem(out, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.RIGHT).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.RIGHT).getMatching(this.machine.itemStorage()));
                groupItem(out, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.BACK).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.BACK).getMatching(this.machine.itemStorage()));
                groupItem(out, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, 16, 16) && this.machine.getIOConfig().get(BlockFace.BOTTOM).getMatching() != null) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.BOTTOM).getMatching(this.machine.itemStorage()));
                groupItem(out, list);
            }
            return out;
        }
        return new Int2IntArrayMap();
    }

    @ApiStatus.Internal
    private void groupFluid(Int2IntMap out, IntList list) {
        for (Tank tank : this.menu.tanks) {
            if (list.contains(tank.getIndex())) {
                out.put(tank.getIndex(), this.machine.fluidStorage().getTypes()[tank.getIndex()].getColor().getValue());
            }
        }
    }

    @ApiStatus.Internal
    private void groupItem(Int2IntMap out, IntList list) {
        for (Slot slot : this.menu.slots) {
            int index = slot.getContainerSlot();
            if (list.contains(index)) {
                out.put(index, this.machine.itemStorage().getTypes()[index].getColor().getValue());
            }
        }
    }

    @ApiStatus.Internal
    private void handleSlotHighlight(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (Tab.CONFIGURATION.isOpen()) {
            mouseX -= MLConstant.TextureCoordinate.PANEL_WIDTH + this.leftPos;
            mouseY -= this.topPos + MLConstant.TextureCoordinate.TAB_HEIGHT + SPACING;
            if (DrawableUtil.isWithin(mouseX, mouseY, TOP_FACE_X, TOP_FACE_Y, 16, 16)) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.TOP).getMatching(this.machine.itemStorage()));
                groupStack(matrices, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, LEFT_FACE_X, LEFT_FACE_Y, 16, 16)) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.LEFT).getMatching(this.machine.itemStorage()));
                groupStack(matrices, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, FRONT_FACE_X, FRONT_FACE_Y, 16, 16)) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.FRONT).getMatching(this.machine.itemStorage()));
                groupStack(matrices, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, RIGHT_FACE_X, RIGHT_FACE_Y, 16, 16)) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.RIGHT).getMatching(this.machine.itemStorage()));
                groupStack(matrices, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, BACK_FACE_X, BACK_FACE_Y, 16, 16)) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.BACK).getMatching(this.machine.itemStorage()));
                groupStack(matrices, list);
            }
            if (DrawableUtil.isWithin(mouseX, mouseY, BOTTOM_FACE_X, BOTTOM_FACE_Y, 16, 16)) {
                IntList list = new IntArrayList(this.machine.getIOConfig().get(BlockFace.BOTTOM).getMatching(this.machine.itemStorage()));
                groupStack(matrices, list);
            }
        }
    }

    @ApiStatus.Internal
    private void groupStack(PoseStack matrices, IntList list) {
        for (Slot slot : this.menu.slots) {
            int index = slot.getContainerSlot();
            if (list.contains(index)) {
                drawSlotOverlay(matrices, slot);
            }
        }
    }

    /**
     * Draws a coloured box around the slot, based on the slot's type.
     * @param matrices the matrix stack
     * @param slot the slot to box
     */
    protected void drawSlotOverlay(@NotNull PoseStack matrices, @NotNull Slot slot) {
        int index = slot.getContainerSlot();
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.disableTexture();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        fillGradient(matrices.last().pose(), bufferBuilder,
                slot.x - 1, slot.y - 1,
                slot.x - 1, slot.y + 17,
                this.getBlitOffset(),
                this.machine.itemStorage().getTypes()[index].getColor().getValue(),
                this.machine.itemStorage().getTypes()[index].getColor().getValue());
        fillGradient(matrices.last().pose(), bufferBuilder,
                slot.x - 1, slot.y + 17,
                slot.x + 17, slot.y - 1,
                this.getBlitOffset(),
                this.machine.itemStorage().getTypes()[index].getColor().getValue(),
                this.machine.itemStorage().getTypes()[index].getColor().getValue());
        fillGradient(matrices.last().pose(), bufferBuilder,
                slot.x + 17, slot.y + 17,
                slot.x + 17, slot.y - 1,
                this.getBlitOffset(),
                this.machine.itemStorage().getTypes()[index].getColor().getValue(),
                this.machine.itemStorage().getTypes()[index].getColor().getValue());
        fillGradient(matrices.last().pose(), bufferBuilder,
                slot.x + 17, slot.y - 1,
                slot.x - 1, slot.y - 1,
                this.getBlitOffset(),
                this.machine.itemStorage().getTypes()[index].getColor().getValue(),
                this.machine.itemStorage().getTypes()[index].getColor().getValue());
        tessellator.end();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hasAccess()) {
            boolean tankMod = false;
            if (this.focusedTank != null && button == 0) {
                tankMod = this.focusedTank.acceptStack(ContainerItemContext.ofPlayerCursor(this.menu.player, this.menu));
                if (tankMod) {
                    FriendlyByteBuf packetByteBuf = PacketByteBufs.create().writeVarInt(this.menu.containerId);
                    packetByteBuf.writeInt(this.focusedTank.getId());
                    ClientPlayNetworking.send(new ResourceLocation(MLConstant.MOD_ID, "tank_modify"), packetByteBuf);
                }
            }
            return this.checkConfigurationPanelClick(mouseX, mouseY, button) | super.mouseClicked(mouseX, mouseY, button) | tankMod;
        } else {
            return false;
        }
    }

    @Override
    protected void renderTooltip(PoseStack matrices, int mouseX, int mouseY) {
        if (hasAccess()) {
            super.renderTooltip(matrices, mouseX, mouseY);
            this.drawConfigurationPanelTooltips(matrices, mouseX, mouseY);
        }
    }

    /**
     * Returns whether the player has access to the machine.
     * @return whether the player has access to the machine
     */
    public boolean hasAccess() {
        if (this.machine != null) {
            return this.machine.getSecurity().hasAccess(this.menu.player);
        }
        return false;
    }

    /**
     * Plays a button click sound.
     */
    private void playButtonSound() {
        assert this.minecraft != null;
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    protected final void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        this.drawTitle(matrices);
    }

    /**
     * Returns the x offset of the screen.
     * @return the x offset of the screen
     */
    public int getX() {
        return this.leftPos;
    }

    /**
     * Returns the y offset of the screen.
     * @return the y offset of the screen
     */
    public int getY() {
        return this.topPos;
    }

    /**
     * Returns the requested item based on the id, or defaults to a barrier if nto found.
     * @param id the id of the item
     * @return the item stack
     */
    private static Item getOptionalItem(ResourceLocation id) {
        return Registry.ITEM.getOptional(id).orElse(Items.BARRIER);
    }

    /**
     * The four different types of configuration panel.
     */
    public enum Tab {
        REDSTONE(MLConstant.TextureCoordinate.TAB_REDSTONE_U, MLConstant.TextureCoordinate.TAB_REDSTONE_V, MLConstant.TextureCoordinate.PANEL_REDSTONE_U, MLConstant.TextureCoordinate.PANEL_REDSTONE_V, true),
        CONFIGURATION(MLConstant.TextureCoordinate.TAB_CONFIG_U, MLConstant.TextureCoordinate.TAB_CONFIG_V, MLConstant.TextureCoordinate.PANEL_CONFIG_U, MLConstant.TextureCoordinate.PANEL_CONFIG_V, true),
        STATS(MLConstant.TextureCoordinate.TAB_STATS_U, MLConstant.TextureCoordinate.TAB_STATS_V, MLConstant.TextureCoordinate.PANEL_STATS_U, MLConstant.TextureCoordinate.PANEL_STATS_V, false),
        SECURITY(MLConstant.TextureCoordinate.TAB_SECURITY_U, MLConstant.TextureCoordinate.TAB_SECURITY_V, MLConstant.TextureCoordinate.PANEL_SECURITY_U, MLConstant.TextureCoordinate.PANEL_SECURITY_V, false);

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

    private enum SideConfigurationAction {
        CHANGE_TYPE((player, machine, face, back, reset) -> {
            ConfiguredMachineFace sideOption = machine.getIOConfig().get(face);
            if (reset) {
                sideOption.setOption(ResourceType.NONE, ResourceFlow.BOTH);
                return;
            }
            Map<ResourceType<?, ?>, List<ResourceFlow>> map = new IdentityHashMap<>();

            for (SlotType<Item, ItemVariant> type : machine.itemStorage().getTypes()) {
                List<ResourceFlow> flows = map.computeIfAbsent(ResourceType.ITEM, l -> new ArrayList<>());
                if (type.getFlow() == ResourceFlow.BOTH) {
                    map.put(ResourceType.ITEM, ResourceFlow.ALL_FLOWS);
                    break;
                } else {
                    if (!flows.contains(type.getFlow())) flows.add(type.getFlow());
                    if (flows.size() == 3) break;
                }
            }

            for (SlotType<Fluid, FluidVariant> type : machine.fluidStorage().getTypes()) {
                List<ResourceFlow> flows = map.computeIfAbsent(ResourceType.FLUID, l -> new ArrayList<>());
                if (type.getFlow() == ResourceFlow.BOTH) {
                    map.put(ResourceType.FLUID, ResourceFlow.ALL_FLOWS);
                    break;
                } else {
                    if (!flows.contains(type.getFlow())) flows.add(type.getFlow());
                    if (flows.size() == 3) break;
                }
            }

            if (machine.getEnergyItemExtractionRate() > 0) {
                List<ResourceFlow> flows = map.computeIfAbsent(ResourceType.ENERGY, l -> new ArrayList<>());
                if (machine.getEnergyItemInsertionRate() > 0) {
                    map.put(ResourceType.ENERGY, ResourceFlow.ALL_FLOWS);
                } else {
                    flows.add(0, ResourceFlow.OUTPUT);
                }
            } else if (machine.getEnergyItemInsertionRate() > 0) {
                map.computeIfAbsent(ResourceType.ENERGY, l -> new ArrayList<>()).add(0, ResourceFlow.INPUT);
            }

            ResourceType<?, ?> outType = null;
            ResourceFlow outFlow = null;

            ResourceType<?, ?>[] normalTypes = ResourceType.normalTypes();
            for (int i = 0; i < normalTypes.length; i++) {
                ResourceType<?, ?> type = normalTypes[i];
                if (type == sideOption.getType()) {
                    List<ResourceFlow> resourceFlows = map.get(type);
                    if (resourceFlows != null) {
                        int idx = resourceFlows.indexOf(sideOption.getFlow());
                        if (idx + (back ? -1 : 1) == (back ? -1 : resourceFlows.size())) {
                            if (i + (back ? -1 : 1) == (back ? -1 : normalTypes.length)) {
                                for (int i1 = back ? normalTypes.length - 1 : 0; back ? i1 >= 0 : i1 < normalTypes.length;) {
                                    if (map.get(normalTypes[i1]) != null) {
                                        outType = normalTypes[i1];
                                        break;
                                    }
                                    if (back) {
                                        i1--;
                                    } else {
                                        i1++;
                                    }
                                }
                            } else {
                                outType = normalTypes[i + (back ? -1 : 1)];
                            }
                            outFlow = map.get(outType).get(back ? map.get(outType).size() : 0);
                        } else {
                            outType = type;
                            outFlow = resourceFlows.get(idx + (back ? -1 : 1));
                        }
                    } else {
                        for (int i1 = back ? normalTypes.length - 1 : 0; back ? i1 >= 0 : i1 < normalTypes.length;) {
                            if (map.get(normalTypes[i1]) != null) {
                                outType = normalTypes[i1];
                                break;
                            }
                            if (back) {
                                i1--;
                            } else {
                                i1++;
                            }
                        }
                        outFlow = map.get(outType).get(0);
                    }
                    break;
                }
            }
            if (outType == null) {
                outType = ResourceType.NONE;
                outFlow = ResourceFlow.BOTH;
            }
            assert outFlow != null;
            sideOption.setOption(outType, outFlow);
            sideOption.setMatching(null);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeByte(face.ordinal()).writeBoolean(false).writeByte(sideOption.getType().getOrdinal()).writeByte(sideOption.getFlow().ordinal());
            ClientPlayNetworking.send(new ResourceLocation(MLConstant.MOD_ID, "side_config"), buf);

        }), //LEFT
        CHANGE_MATCH((player, machine, face, back, reset) -> {
            ConfiguredMachineFace sideOption = machine.getIOConfig().get(face);
            if (sideOption.getType().willAcceptResource(ResourceType.ENERGY) || sideOption.getType() == ResourceType.NONE) return;
            if (reset) {
                sideOption.setMatching(null);
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeByte(face.ordinal()).writeBoolean(true).writeBoolean(false).writeInt(-1);
                ClientPlayNetworking.send(new ResourceLocation(MLConstant.MOD_ID, "side_config"), buf);
                return;
            }

            ConfiguredStorage<?, ?> storage = machine.getStorage(sideOption.getType());
            SlotType<?, ?>[] slotTypes = storage != null ? storage.getTypes() : new SlotType[0];

            slotTypes = Arrays.copyOf(slotTypes, slotTypes.length);
            int s = 0;
            for (int i = 0; i < slotTypes.length; i++) {
                if (!slotTypes[i].getType().willAcceptResource(sideOption.getType())) {
                    slotTypes[i] = null;
                    s++;
                }
            }
            if (s > 0) {
                SlotType<?, ?>[] tmp = new SlotType[slotTypes.length - s];
                s = 0;
                for (int i = 0; i < slotTypes.length; i++) {
                    if (slotTypes[i] == null) {
                        s++;
                    } else {
                        tmp[i - s] = slotTypes[i];
                    }
                }
                slotTypes = tmp;
            }
            int i = 0;
            if (sideOption.getMatching() != null && sideOption.getMatching().right().isPresent()) {
                SlotType<?, ?> slotType = sideOption.getMatching().right().get();
                for (; i < slotTypes.length; i++) {
                    if (slotTypes[i] == slotType) break;
                }
                if (back) i--;
                else i++;
            }

            if (i == slotTypes.length) i = 0;
            if (i == -1) i = slotTypes.length - 1;
            sideOption.setMatching(Either.right(slotTypes[i]));
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeByte(face.ordinal()).writeBoolean(true).writeBoolean(false).writeInt(SlotType.REGISTRY.getId(slotTypes[i]));
            ClientPlayNetworking.send(new ResourceLocation(MLConstant.MOD_ID, "side_config"), buf);
        }), //RIGHT
        CHANGE_MATCH_SLOT((player, machine, face, back, reset) -> {
            ConfiguredMachineFace sideOption = machine.getIOConfig().get(face);
            if (sideOption.getType().isSpecial() || sideOption.getType() == ResourceType.ENERGY) return;
            if (reset) {
                sideOption.setMatching(null);
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeByte(face.ordinal()).writeBoolean(true).writeBoolean(true).writeInt(-1);
                ClientPlayNetworking.send(new ResourceLocation(MLConstant.MOD_ID, "side_config"), buf);
                return;
            }
            int i = 0;
            IntList list = null;
            if (sideOption.getMatching() != null && sideOption.getMatching().left().isPresent()) {
                i = sideOption.getMatching().left().get();
                sideOption.setMatching(null);
                list = new IntArrayList(sideOption.getMatching(machine.getStorage(sideOption.getType())));
                i = list.indexOf(i);
            }
            if (list == null)
                list = new IntArrayList(sideOption.getMatching(machine.getStorage(sideOption.getType())));

            if (!back) {
                if (++i == list.size()) i = 0;
            } else {
                if (i == 0) i = list.size();
                i--;
            }
            sideOption.setMatching(Either.left(list.getInt(i)));

            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeByte(face.ordinal());
            buf.writeBoolean(true).writeBoolean(true);
            buf.writeInt(i);
            ClientPlayNetworking.send(new ResourceLocation(MLConstant.MOD_ID, "side_config"), buf);
        }); //MID


        static final SideConfigurationAction[] VALUES = SideConfigurationAction.values();
        private final IOConfigUpdater updater;

        SideConfigurationAction(IOConfigUpdater updater) {
            this.updater = updater;
        }

        void update(LocalPlayer player, MachineBlockEntity machine, BlockFace face, boolean back, boolean reset) {
            updater.update(player, machine, face, back, reset);
        }

        @FunctionalInterface
        interface IOConfigUpdater {
            void update(LocalPlayer player, MachineBlockEntity machine, BlockFace face, boolean back, boolean reset);
        }
    }
}
