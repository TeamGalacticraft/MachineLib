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

package dev.galacticraft.impl.machine.storage.io;

import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.api.machine.storage.io.SlotType;
import dev.galacticraft.impl.MLConstant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class SlotTypeImpl<T, V extends TransferVariant<T>> implements SlotType<T, V> {
    static {
        Registry.register(REGISTRY, new Identifier(MLConstant.MOD_ID, "none"), new SlotTypeImpl(TextColor.fromRgb(0x000000), new TranslatableText(MLConstant.TranslationKey.INVALID_SLOT_TYPE), v -> false, ResourceFlow.BOTH, ResourceType.NONE));
    }

    private final @NotNull RegistryEntry.Reference<SlotType<?, ?>> reference = REGISTRY.createEntry(this);
    private final @NotNull TextColor color;
    private final @NotNull TranslatableText name;
    private final @NotNull Predicate<V> filter;
    private final @NotNull ResourceFlow flow;
    private final @NotNull ResourceType<T, V> type;

    public SlotTypeImpl(@NotNull TextColor color, @NotNull TranslatableText name, @NotNull Predicate<V> filter, @NotNull ResourceFlow flow, @NotNull ResourceType<T, V> type) {
        this.color = color;
        this.filter = filter;
        this.name = (TranslatableText) name.setStyle(Style.EMPTY.withColor(color));
        this.flow = flow;
        this.type = type;
    }

    @Override
    public @NotNull TextColor getColor() {
        return color;
    }

    @Override
    public @NotNull Text getName() {
        return this.name;
    }

    @Override
    public @NotNull ResourceType<T, V> getType() {
        return this.type;
    }

    @Override
    public @NotNull ResourceFlow getFlow() {
        return this.flow;
    }

    @Override
    public boolean willAccept(@NotNull V variant) {
        return variant.isBlank() || this.filter.test(variant);
    }

    @Override
    public RegistryEntry.@NotNull Reference<SlotType<?, ?>> getReference() {
        return this.reference;
    }

    @Override
    public String toString() {
        return "SlotTypeImpl{" +
                ", color=" + color +
                ", name=" + name +
                ", flow=" + flow +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotTypeImpl<?, ?> slotTypeImpl = (SlotTypeImpl<?, ?>) o;
        return color.equals(slotTypeImpl.color) && name.equals(slotTypeImpl.name) && flow == slotTypeImpl.flow && type.equals(slotTypeImpl.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, name, flow, type);
    }
}
