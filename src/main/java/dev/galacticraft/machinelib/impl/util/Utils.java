/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.util;

import dev.galacticraft.machinelib.impl.MachineLib;
import io.netty.buffer.ByteBufAllocator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public final class Utils {
    /**
     * A codec that copies the contents of a buffer.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, RegistryFriendlyByteBuf> BUF_IDENTITY_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf src, RegistryFriendlyByteBuf dst) {
            src.writeBytes(dst);
        }

        @Override
        public @NotNull RegistryFriendlyByteBuf decode(RegistryFriendlyByteBuf src) {
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(ByteBufAllocator.DEFAULT.buffer(src.capacity()), src.registryAccess());
            buf.writeBytes(src);
            return buf;
        }
    };

    private Utils() {
    }

    public static void awardUsedRecipes(@NotNull ServerPlayer player, @NotNull Set<ResourceLocation> recipes) {
        for (ResourceLocation id : recipes) {
            Optional<RecipeHolder<?>> optional = player.serverLevel().getRecipeManager().byKey(id);
            if (optional.isPresent()) {
                player.awardRecipes(Collections.singleton(optional.get()));
                player.triggerRecipeCrafted(optional.get(), Collections.emptyList());
            }
        }
    }

    @Contract(pure = true)
    public static boolean itemsEqual(@Nullable Item a, @NotNull Item b) {
        return a == b || (a == null && b == Items.AIR);
    }

    /**
     * {@return the short string representation of the given id}
     * Should only be used to serialize packets and other short-lived data.
     * If the namespace matches "minecraft", only the path is returned.
     *
     * @param id the id to shorten
     */
    public static String getShortId(ResourceLocation id) {
        return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
    }

    public static void breakpointMe(String s) {
        MachineLib.LOGGER.error(s);
    }
}
