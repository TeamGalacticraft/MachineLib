/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

import dev.galacticraft.api.gas.Gas;
import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.transfer.v1.gas.GasStorage;
import dev.galacticraft.impl.client.util.DrawableUtil;
import dev.galacticraft.impl.machine.Constant;
import dev.galacticraft.impl.util.GenericStorageUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Tank<T, V extends TransferVariant<T>> {
    public final ExposedStorage<T, V> storage;
    private final int index;
    public int id = -1;
    private final int x;
    private final int y;
    private final int height;
    private final @NotNull ResourceType<T, V> type;

    public Tank(ExposedStorage<T, V> storage, int index, int x, int y, int height, @NotNull ResourceType<T, V> type) {
        this.storage = storage;
        this.index = index;
        this.x = x;
        this.y = y;
        this.height = height;
        this.type = type;

        if (this.type != ResourceType.GAS && this.type != ResourceType.FLUID) {
            throw new AssertionError("Invalid tank of resource: " + this.type);
        }
    }

    public V getResource() {
        return this.storage.getResource(this.index);
    }

    public int getIndex() {
        return index;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return 16;
    }

    public void drawTooltip(@NotNull MatrixStack matrices, MinecraftClient client, int x, int y, int mouseX, int mouseY) {
        matrices.translate(0, 0, 1);
        if (DrawableUtil.isWithin(mouseX, mouseY, x + this.x, y + this.y, this.getWidth(), this.getHeight())) {
            List<Text> lines = new ArrayList<>(2);
            if (this.getResource().isBlank()) {
                if (type == ResourceType.GAS) {
                    client.currentScreen.renderTooltip(matrices, new TranslatableText("ui.galacticraft.machine.tank.gas.empty").setStyle(Constant.Text.GRAY_STYLE), mouseX, mouseY);
                } else {
                    client.currentScreen.renderTooltip(matrices, new TranslatableText("ui.galacticraft.machine.tank.fluid.empty").setStyle(Constant.Text.GRAY_STYLE), mouseX, mouseY);
                }
                return;
            }
            MutableText amount;
            long amnt = this.getAmount();
            if (Screen.hasShiftDown() || amnt / 81.0 < 10000) {
                amount = new LiteralText(DrawableUtil.roundForDisplay(amnt / 81.0, 0) + "mB");
            } else {
                amount = new LiteralText(DrawableUtil.roundForDisplay(amnt / 81000.0, 2) + "B");
            }

            TranslatableText translatableText;
            if (this.type == ResourceType.GAS) {
                translatableText = new TranslatableText("ui.galacticraft.machine.tank.gas");
            } else {
                translatableText = new TranslatableText("ui.galacticraft.machine.tank.fluid");
            }
            lines.add(translatableText.setStyle(Constant.Text.GRAY_STYLE).append(new LiteralText(getName(this.getResource())).setStyle(Constant.Text.BLUE_STYLE)));
            lines.add(new TranslatableText("ui.galacticraft.machine.tank.amount").setStyle(Constant.Text.GRAY_STYLE).append(amount.setStyle(Style.EMPTY.withColor(Formatting.WHITE))));
            client.currentScreen.renderTooltip(matrices, lines, mouseX, mouseY);
        }
        matrices.translate(0, 0, -1);
    }

    @Environment(EnvType.CLIENT)
    private String getName(@NotNull TransferVariant<?> object) {
        Object obj = object.getObject();
        if (obj instanceof Gas) {
            return I18n.translate(((Gas) obj).getTranslationKey());
        } else if (obj instanceof Fluid fluid) {
            Identifier id = Registry.FLUID.getId(fluid);
            if (I18n.hasTranslation("fluid." + id.getNamespace() + "." + id.getPath())) {
                return I18n.translate("fluid." + id.getNamespace() + "." + id.getPath());
            } else if (I18n.hasTranslation("block." + id.getNamespace() + "." + id.getPath())) {
                return I18n.translate("block." + id.getNamespace() + "." + id.getPath());
            } else {
                Item bucketItem = ((Fluid) obj).getBucketItem();
                if (bucketItem == Items.AIR) {
                    return guessNameFromId(id);
                } else {
                    return I18n.translate(bucketItem.getTranslationKey()).replace(I18n.translate("item.minecraft.bucket"), "").trim();
                }
            }

        } else {
            return "Invalid tank entry?!";
        }
    }

    @NotNull
    private String guessNameFromId(@NotNull Identifier id) {
        char[] chars = id.getPath().replace("flowing", "").replace("still", "").replace("_", " ").trim().toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0 || chars[i - 1] == ' ') {
                builder.append(Character.toUpperCase(chars[i]));
            } else {
                builder.append(chars[i]);
            }
        }
        return builder.toString();
    }

    public boolean acceptStack(@NotNull ContainerItemContext context) {
        Storage<V> storage = context.find(getLookup(this.type));
        if (storage != null) {
            if (storage.supportsExtraction() && this.storage.supportsInsertion()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    V storedResource;
                    if (this.getResource().isBlank()) {
                        storedResource = StorageUtil.findStoredResource(storage, this.storage.getFilter(this.index), transaction);
                    } else {
                        storedResource = this.getResource();
                    }
                    if (storedResource != null) {
                        if (GenericStorageUtil.move(storedResource, storage, this.storage.getSlot(this.index), Long.MAX_VALUE, transaction) != 0) {
                            transaction.commit();
                            return true;
                        }
                        return false;
                    }
                }
            } else if (storage.supportsInsertion() && this.storage.supportsExtraction()) {
                V storedResource = this.getResource();
                if (!storedResource.isBlank()) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        if (GenericStorageUtil.move(storedResource, this.storage.getSlot(this.index), storage, Long.MAX_VALUE, transaction) != 0) {
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

    private static <T, V extends TransferVariant<T>> ItemApiLookup<Storage<V>, ContainerItemContext> getLookup(ResourceType<T, V> type) {
        if (type == ResourceType.GAS) {
            return (ItemApiLookup<Storage<V>, ContainerItemContext>) (Object)GasStorage.ITEM;
        } else if (type == ResourceType.GAS){
            return (ItemApiLookup<Storage<V>, ContainerItemContext>) (Object)FluidStorage.ITEM;
        }
        throw new AssertionError();
    }

    public @NotNull ResourceType<T, V> getResourceType() {
        return this.type;
    }

    public long getAmount() {
        return this.storage.getAmount(this.index);
    }

    public long getCapacity() {
        return this.storage.getCapacity(this.index);
    }
}
