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

package dev.galacticraft.api.machine.storage.slot;

import dev.galacticraft.api.gas.Gas;
import dev.galacticraft.api.gas.GasVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public final class ResourceType<T, V> {
    public static final ResourceType<?, ?> ANY = new ResourceType<>(new TranslatableText("ui.galacticraft.side_option.any").setStyle(Style.EMPTY.withColor(Formatting.AQUA)));
    public static final ResourceType<?, ?> NONE = new ResourceType<>(new TranslatableText("ui.galacticraft.side_option.none").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
    public static final ResourceType<Long, Long> ENERGY = new ResourceType<>(new TranslatableText("ui.galacticraft.side_option.energy").setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)));
    public static final ResourceType<Fluid, FluidVariant> FLUID = new ResourceType<>(new TranslatableText("ui.galacticraft.side_option.fluid").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
    public static final ResourceType<Gas, GasVariant> GAS = new ResourceType<>(new TranslatableText("ui.galacticraft.side_option.gas").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
    public static final ResourceType<Item, ItemVariant> ITEM = new ResourceType<>(new TranslatableText("ui.galacticraft.side_option.item").setStyle(Style.EMPTY.withColor(Formatting.GOLD)));

    private final Text name;

    private ResourceType(Text name) {
        this.name = name;
    }

    public Text getName() {
        return this.name;
    }

    public <OT, OV extends TransferVariant<OT>> boolean willAcceptResource(ResourceType<OT, OV> other) {
        return this != NONE && (this == other || this == ANY);
    }
}
