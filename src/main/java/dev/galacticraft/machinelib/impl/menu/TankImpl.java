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

import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.util.GenericApiUtil;
import dev.galacticraft.machinelib.client.api.screen.Tank;
import dev.galacticraft.machinelib.client.impl.util.DrawableUtil;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.storage.slot.InputType;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
    public final ResourceSlot<Fluid, FluidStack> slot;
    private final int index;
    private final int x;
    private final int y;
    private final int height;
    public int id = -1;

    public TankImpl(ResourceSlot<Fluid, FluidStack> slot, int index, int x, int y, int height) {
        this.slot = slot;
        this.index = index;
        this.x = x;
        this.y = y;
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
    public FluidVariant getVariant() {
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
        return 16;
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
    public void drawTooltip(@NotNull PoseStack matrices, Minecraft client, int x, int y, int mouseX, int mouseY) { //todo: client/server split
        matrices.translate(0, 0, 1);
        if (DrawableUtil.isWithin(mouseX, mouseY, x + this.x, y + this.y, this.getWidth(), this.getHeight())) {
            List<Component> lines = new ArrayList<>(2);
            assert client.screen != null;
            if (this.isEmpty()) {
                client.screen.renderTooltip(matrices, Component.translatable(Constant.TranslationKey.TANK_EMPTY).setStyle(Constant.Text.GRAY_STYLE), mouseX, mouseY);
                return;
            }
            long amount = this.getAmount();
            MutableComponent text = Screen.hasShiftDown() || amount / 81.0 < 10000 ?
                    Component.literal(DrawableUtil.roundForDisplay(amount / 81.0, 0) + "mB")
                    : Component.literal(DrawableUtil.roundForDisplay(amount / 81000.0, 2) + "B");

            MutableComponent translatableText;
            translatableText = Component.translatable(Constant.TranslationKey.TANK_CONTENTS);

            lines.add(translatableText.setStyle(Constant.Text.GRAY_STYLE).append(FluidVariantAttributes.getName(this.getVariant())).setStyle(Constant.Text.BLUE_STYLE));
            lines.add(Component.translatable(Constant.TranslationKey.TANK_AMOUNT).setStyle(Constant.Text.GRAY_STYLE).append(text.setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))));
            client.screen.renderComponentTooltip(matrices, lines, mouseX, mouseY);
        }
        matrices.translate(0, 0, -1);
    }

    @Override
    public boolean acceptStack(@NotNull ContainerItemContext context) {
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        if (storage != null) {
            InputType type = this.slot.getGroup().getType().inputType();
            if (storage.supportsExtraction() && type.playerInsertion()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    FluidVariant storedResource;
                    if (this.isEmpty()) {
                        storedResource = StorageUtil.findStoredResource(storage, variant -> this.slot.getFilter().test(variant.getFluid(), variant.getNbt()));
                    } else {
                        storedResource = this.getVariant();
                    }
                    if (storedResource != null && !storedResource.isBlank()) {
                        if (GenericApiUtil.move(storedResource, storage, this.slot, Long.MAX_VALUE, transaction) != 0) {
                            transaction.commit();
                            return true;
                        }
                        return false;
                    }
                }
            } else if (storage.supportsInsertion() && type.playerExtraction()) {
                FluidVariant storedResource = this.getVariant();
                if (!storedResource.isBlank()) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        if (GenericApiUtil.move(storedResource, this.slot, storage, Long.MAX_VALUE, transaction) != 0) {
                            transaction.commit();
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ResourceSlot<Fluid, FluidStack> getSlot() {
        return this.slot;
    }
}
