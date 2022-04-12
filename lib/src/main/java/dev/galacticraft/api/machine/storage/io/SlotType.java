/*
 * Copyright (c) 2021-${year} ${company}
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

package dev.galacticraft.api.machine.storage.io;

import com.mojang.serialization.Lifecycle;
import dev.galacticraft.impl.machine.Constant;
import dev.galacticraft.impl.machine.storage.io.SlotTypeImpl;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Used for filtering, flow and I/O configuration of resources.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public interface SlotType<T, V extends TransferVariant<T>> {
    Registry<SlotType<?, ?>> REGISTRY = FabricRegistryBuilder.from(new DefaultedRegistry<SlotType<?, ?>>(new Identifier(Constant.MOD_ID, "none").toString(), RegistryKey.ofRegistry(new Identifier(Constant.MOD_ID, "slot_type")), Lifecycle.stable(), SlotType::getReference)).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    static <T, V extends TransferVariant<T>> SlotType<T, V> create(Identifier id, @NotNull TextColor color, @NotNull TranslatableText name, @NotNull Predicate<V> filter, @NotNull ResourceFlow flow, @NotNull ResourceType<T, V> type) {
        if (color.getRgb() == 0xFFFFFFFF) throw new IllegalArgumentException("Color cannot be totally white (-1)! (It is used as a default/invalid number)");
        if (type.isSpecial()) throw new IllegalArgumentException("Resource type cannot be special!");
        return Registry.register(REGISTRY, id, new SlotTypeImpl<>(color, name, filter, flow, type));
    }

    /**
     * Returns the color of the slot type.
     * @return The color of the slot type.
     */
    @NotNull TextColor getColor();

    /**
     * Returns the name of the slot type.
     * @return The name of the slot type.
     */
    @NotNull Text getName();

    /**
     * Returns the resource type of the slot type.
     * @return The resource type of the slot type.
     */
    @NotNull ResourceType<T, V> getType();

    /**
     * Returns the exposed resource flow of the slot type.
     * @return The exposed resource flow of the slot type.
     */
    @NotNull ResourceFlow getFlow();

    /**
     * Returns whether the slot type is valid for the given resource.
     * @param variant The resource variant.
     * @return Whether the slot type is valid for the given resource.
     */
    boolean willAccept(@NotNull V variant);

    @ApiStatus.Internal RegistryEntry.Reference<SlotType<?, ?>> getReference();
}
