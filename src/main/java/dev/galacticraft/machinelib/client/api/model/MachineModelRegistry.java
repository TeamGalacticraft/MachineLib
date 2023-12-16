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

package dev.galacticraft.machinelib.client.api.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.api.render.MachineRenderData;
import dev.galacticraft.machinelib.client.impl.model.MachineBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A registry for {@link MachineBakedModel} sprite providers.
 */
public interface MachineModelRegistry {
    Map<ResourceLocation, SpriteProviderFactory> FACTORIES = new HashMap<>();

    /**
     * Registers a sprite provider for a block.
     *
     * @param id       The id to register the provider for.
     * @param factory The provider to register.
     */
    static void register(@NotNull ResourceLocation id, @NotNull SpriteProviderFactory factory) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(factory);

        FACTORIES.put(id, factory);
    }

    /**
     * Returns the registered sprite provider for a block.
     *
     * @param providerId The provider id to get the provider for.
     * @return The registered provider, or null if none is registered.
     */
    static @Nullable SpriteProviderFactory getProviderFactory(@NotNull ResourceLocation providerId) {
        return FACTORIES.get(providerId);
    }

    static @NotNull SpriteProviderFactory getProviderFactoryOrDefault(@NotNull ResourceLocation providerId) {
        return FACTORIES.getOrDefault(providerId, SpriteProviderFactory.DEFAULT);
    }

    @FunctionalInterface
    interface SpriteProviderFactory {
        SpriteProviderFactory DEFAULT = new SpriteProviderFactory() {
            @Contract(value = "_, _ -> new", pure = true)
            @Override
            public @NotNull SpriteProvider create(@NotNull JsonObject json, @NotNull Function<Material, TextureAtlasSprite> atlas) {
                return new SpriteProvider() {
                    private final TextureAtlasSprite machineSide = atlas.apply(MachineBakedModel.MACHINE_SIDE);
                    private final TextureAtlasSprite machine = atlas.apply(MachineBakedModel.MACHINE);

                    @Override
                    public @NotNull TextureAtlasSprite getSpritesForState(@Nullable MachineRenderData renderData, @NotNull BlockFace face) {
                        if (face.side()) return this.machineSide;
                        return this.machine;
                    }
                };
            }
        };

        SpriteProvider create(@NotNull JsonObject json, @NotNull Function<Material, TextureAtlasSprite> atlas);
    }

    @FunctionalInterface
    interface SpriteProvider {
        /**
         * @param face    The face that is being textured.
         * @return The appropriate sprite to render for the given face.
         */
        @Contract(pure = true)
        @NotNull TextureAtlasSprite getSpritesForState(@Nullable MachineRenderData data, @NotNull BlockFace face);
    }
}
