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

import com.mojang.serialization.Lifecycle;
import dev.galacticraft.api.gas.Gas;
import dev.galacticraft.api.gas.GasVariant;
import dev.galacticraft.impl.machine.Constant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class SlotType<T, V extends TransferVariant<T>> implements StringIdentifiable {
    public static final Registry<SlotType<?, ?>> SLOT_TYPES = new DefaultedRegistry<>(Constant.MOD_ID + ":none", RegistryKey.ofRegistry(new Identifier(Constant.MOD_ID, "slot_type")), Lifecycle.experimental(), SlotType::getReference);

    public static final SlotType<?, ?> NONE = new SlotType(new Identifier(Constant.MOD_ID, "none"), TextColor.fromRgb(0x000000), new TranslatableText("ui.galacticraft.io_config.none"), ResourceFlow.BOTH, ResourceType.NONE);
    public static final SlotType<Item, ItemVariant> WILDCARD_ITEM = new SlotType<>(new Identifier(Constant.MOD_ID, "wildcard_item"), TextColor.fromRgb(0x8d32c7), new TranslatableText("ui.galacticraft.io_config.wildcard_item"), ResourceFlow.BOTH, ResourceType.ITEM);
    public static final SlotType<Fluid, FluidVariant> WILDCARD_FLUID = new SlotType<>(new Identifier(Constant.MOD_ID, "wildcard_fluid"), TextColor.fromRgb(0x8d32c7), new TranslatableText("ui.galacticraft.io_config.wildcard_fluid"), ResourceFlow.BOTH, ResourceType.FLUID);
    public static final SlotType<Gas, GasVariant> WILDCARD_GAS = new SlotType<>(new Identifier(Constant.MOD_ID, "wildcard_gas"), TextColor.fromRgb(0x9faac7), new TranslatableText("ui.galacticraft.io_config.wildcard_gas"), ResourceFlow.BOTH, ResourceType.GAS);

    private final @NotNull RegistryEntry.Reference<SlotType<?, ?>> reference = SLOT_TYPES.createEntry(this);
    private final @NotNull Identifier id;
    private final @NotNull TextColor color;
    private final @NotNull Text name;
    private final @NotNull ResourceFlow flow;
    private final @NotNull ResourceType<T, V> type;
    public Predicate<V> filter;

    public SlotType(@NotNull Identifier id, @NotNull TextColor color, @NotNull TranslatableText name, @NotNull ResourceFlow flow, @NotNull ResourceType<T, V> type) {
        this.id = id;
        this.color = color;
        this.name = name.setStyle(Style.EMPTY.withColor(color));
        this.flow = flow;
        this.type = type;
    }

    public @NotNull Identifier getId() {
        return id;
    }

    public @NotNull TextColor getColor() {
        return color;
    }

    public @NotNull Text getName() {
        return this.name;
    }

    public @NotNull ResourceType<T, V> getType() {
        return this.type;
    }

    public @NotNull ResourceFlow getFlow() {
        return this.flow;
    }

    public @NotNull RegistryEntry.Reference<SlotType<?, ?>> getReference() {
        return this.reference;
    }

    @Override
    public String asString() {
        return this.id.toString();
    }

    @Override
    public String toString() {
        return "SlotType{" +
                "reference=" + reference +
                ", id=" + id +
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
        SlotType<?, ?> slotType = (SlotType<?, ?>) o;
        return reference.equals(slotType.reference) && id.equals(slotType.id) && color.equals(slotType.color) && name.equals(slotType.name) && flow == slotType.flow && type.equals(slotType.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, id, color, name, flow, type);
    }

    static {
        Registry.register(SLOT_TYPES, NONE.getId(), NONE);
        Registry.register(SLOT_TYPES, WILDCARD_ITEM.getId(), WILDCARD_ITEM);
        Registry.register(SLOT_TYPES, WILDCARD_FLUID.getId(), WILDCARD_FLUID);
        Registry.register(SLOT_TYPES, WILDCARD_GAS.getId(), WILDCARD_FLUID);
    }

    public boolean willAccept(V variant) {
        return this.filter.test(variant);
    }
}
