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

package dev.galacticraft.api.machine.storage.io;

import com.mojang.serialization.Lifecycle;
import dev.galacticraft.impl.MLConstant;
import dev.galacticraft.impl.machine.storage.io.SlotTypeImpl;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Used for filtering, flow and I/O configuration of resources.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public interface SlotType<T, V extends TransferVariant<T>> {
    Registry<SlotType<?, ?>> REGISTRY = FabricRegistryBuilder.from(new DefaultedRegistry<SlotType<?, ?>>(new ResourceLocation(MLConstant.MOD_ID, "none").toString(), ResourceKey.createRegistryKey(new ResourceLocation(MLConstant.MOD_ID, "slot_type")), Lifecycle.stable(), null)).attribute(RegistryAttribute.SYNCED).buildAndRegister();

    static <T, V extends TransferVariant<T>> SlotType<T, V> create(ResourceLocation id, @NotNull TextColor color, @NotNull MutableComponent name, @NotNull Predicate<V> filter, @NotNull ResourceFlow flow, @NotNull ResourceType<T, V> type) {
        if (color.getValue() == 0xFFFFFFFF) throw new IllegalArgumentException("Color cannot be totally white (-1)! (It is used as a default/invalid number)");
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
    @NotNull Component getName();

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
}
