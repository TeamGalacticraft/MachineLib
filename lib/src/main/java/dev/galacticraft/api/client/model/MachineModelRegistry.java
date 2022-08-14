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

package dev.galacticraft.api.client.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.block.util.BlockFace;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.impl.MLConstant;
import dev.galacticraft.impl.client.model.MachineBakedModel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * A registry for {@link MachineBakedModel} sprite providers.
 * Use {@link #register(ResourceLocation, Supplier)} to register a sprite provider for a block.
 */
public interface MachineModelRegistry {
    ResourceLocation MACHINE = new ResourceLocation(MLConstant.MOD_ID, "block/machine");
    ResourceLocation MACHINE_SIDE = new ResourceLocation(MLConstant.MOD_ID, "block/machine_side");

    Map<ResourceLocation, Supplier<SpriteProvider>> REGISTERED_SPRITE_PROVIDERS = new HashMap<>();

    /**
     * Registers a sprite provider for a block.
     * @param id The id to register the provider for.
     * @param provider The provider to register.
     */
    static void register(@NotNull ResourceLocation id, @NotNull Supplier<SpriteProvider> provider) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(provider);

        REGISTERED_SPRITE_PROVIDERS.put(id, provider);
    }

    /**
     * Returns the registered sprite provider for a block.
     * @param providerId The provider id to get the provider for.
     * @return The registered provider, or null if none is registered.
     */
    static @NotNull Supplier<SpriteProvider> getSpriteProvider(@NotNull ResourceLocation providerId) {
        return REGISTERED_SPRITE_PROVIDERS.get(providerId);
    }

    /**
     * Returns the registered sprite provider for a block.
     * @param block The block to get the provider for.
     * @param defaultProvider The default provider to return if none is registered.
     * @return The registered provider, or the default provider if none is registered.
     */
    static @Nullable SpriteProvider getSpriteProviderOrElseGet(@NotNull Block block, @Nullable SpriteProvider defaultProvider) {
        return MachineBakedModel.SPRITE_PROVIDERS.getOrDefault(block, defaultProvider);
    }

    /**
     * Returns the sprite of a machine block for a given face.
     * Either the stack or the block entity must be provided.
     * @param face The face to get the sprite for.
     * @param machine The machine to get the sprite for.
     *                If null, the stack must be provided.
     * @param stack The stack to get the sprite for.
     *              If null, the block entity must be provided.
     * @param provider The provider to get the sprite for.
     * @param type The type of resource to get the sprite for.
     * @param flow The flow of the resource to get the sprite for.
     * @return The sprite of the machine block for the given face.
     */
    static TextureAtlasSprite getSprite(@NotNull BlockFace face, @Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull MachineModelRegistry.SpriteProvider provider, @NotNull ResourceType<?, ?> type, @NotNull ResourceFlow flow) {
        return MachineBakedModel.getSprite(face, machine, stack, provider, type, flow);
    }

    @FunctionalInterface
    interface SpriteProvider {
        SpriteProvider DEFAULT = (machine, stack, face, atlas) -> {
            if (face.horizontal()) return atlas.apply(MACHINE_SIDE);
            return atlas.apply(MACHINE);
        };

        /**
         * @param machine The machine block entity instance. Will be null in item contexts.
         * @param stack The machine stack being rendered. Will be null in block contexts. DO NOT MODIFY THE STACK
         * @param face The face that is being textured.
         * @param atlas The texture atlas.
         * @return The appropriate sprite to render for the given face.
         */
        @Contract(pure = true, value = "null,null,_,_->fail;!null,!null,_,_->fail")
        @NotNull TextureAtlasSprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<ResourceLocation, TextureAtlasSprite> atlas);

        default void fromJson(JsonObject jsonObject, Set<ResourceLocation> textureDependencies) {}
    }
}
