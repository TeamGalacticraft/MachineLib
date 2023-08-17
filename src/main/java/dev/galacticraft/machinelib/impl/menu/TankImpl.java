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

package dev.galacticraft.machinelib.impl.menu;

import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.InputType;
import dev.galacticraft.machinelib.api.util.GenericApiUtil;
import dev.galacticraft.machinelib.client.api.screen.Tank;
import dev.galacticraft.machinelib.client.api.util.DisplayUtil;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Somewhat like a {@link net.minecraft.world.inventory.Slot} but for fluids and gases.
 * Resources can be inserted into the tank and extracted from it via the gui.
 */
public final class TankImpl implements Tank {
    public final ResourceSlot<Fluid> slot;
    private final InputType inputType;
    private final int index;
    private final int x;
    private final int y;
    private final int height;
    private final int width;
    public int id = -1;

    public TankImpl(ResourceSlot<Fluid> slot, InputType inputType, int index, int x, int y, int width, int height) {
        this.slot = slot;
        this.inputType = inputType;
        this.index = index;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public @Nullable Fluid getFluid() {
        return this.slot.getResource();
    }

    @Override
    public @Nullable CompoundTag getTag() {
        return this.slot.getTag();
    }

    @Override
    public @Nullable CompoundTag copyTag() {
        return this.slot.copyTag();
    }

    @Override
    public long getAmount() {
        return this.slot.getAmount();
    }

    @Override
    public long getCapacity() {
        return this.slot.getCapacity();
    }

    @Override
    public boolean isEmpty() {
        return this.slot.isEmpty();
    }

    @Override
    public FluidVariant createVariant() {
        return this.getFluid() == null ? FluidVariant.blank() : FluidVariant.of(this.getFluid(), this.getTag());
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void drawTooltip(@NotNull GuiGraphics graphics, Minecraft client, int x, int y, int mouseX, int mouseY) { //todo: client/server split
        graphics.pose().translate(0, 0, 1);
        if (mouseIn(mouseX, mouseY, x + this.x, y + this.y, this.getWidth(), this.getHeight())) {
            List<Component> lines = new ArrayList<>(2);
            assert client.screen != null;
            if (this.isEmpty()) {
                graphics.renderTooltip(client.font, Component.translatable(Constant.TranslationKey.TANK_EMPTY).setStyle(Constant.Text.GRAY_STYLE), mouseX, mouseY);
                return;
            }
            long amount = this.getAmount();
            MutableComponent text = Screen.hasShiftDown() || amount / 81.0 < 10000 ?
                    Component.literal(DisplayUtil.truncateDecimal(amount / (FluidConstants.BUCKET / 1000.0), 0) + "mB")
                    : Component.literal(DisplayUtil.truncateDecimal(amount / (double) FluidConstants.BUCKET, 2) + "B");

            MutableComponent translatableText;
            translatableText = Component.translatable(Constant.TranslationKey.TANK_CONTENTS);

            lines.add(translatableText.setStyle(Constant.Text.GRAY_STYLE).append(FluidVariantAttributes.getName(this.createVariant())).setStyle(Constant.Text.BLUE_STYLE));
            lines.add(Component.translatable(Constant.TranslationKey.TANK_AMOUNT).setStyle(Constant.Text.GRAY_STYLE).append(text.setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));
            graphics.renderComponentTooltip(client.font, lines, mouseX, mouseY);
        }
        graphics.pose().translate(0, 0, -1);
    }

    @Override
    public boolean acceptStack(@NotNull ContainerItemContext context) {
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        if (storage != null) {
            if (storage.supportsExtraction() && this.inputType.playerInsertion()) {
                FluidVariant storedResource;
                if (this.isEmpty()) {
                    storedResource = StorageUtil.findStoredResource(storage, variant -> this.slot.getFilter().test(variant.getFluid(), variant.getNbt()));
                } else {
                    storedResource = this.createVariant();
                }
                if (storedResource != null && !storedResource.isBlank()) {
                    return GenericApiUtil.move(storedResource, storage, this.slot, Long.MAX_VALUE, null) != 0;
                }
            } else if (storage.supportsInsertion() && this.inputType.playerExtraction()) {
                FluidVariant storedResource = this.createVariant();
                if (!storedResource.isBlank()) {
                    return GenericApiUtil.move(storedResource, this.slot, storage, Long.MAX_VALUE, null) != 0;
                }
            }
        }
        return false;
    }

    @Override
    public ResourceSlot<Fluid> getSlot() {
        return this.slot;
    }

    @Override
    public InputType getInputType() {
        return this.inputType;
    }

    private static boolean mouseIn(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }
}
