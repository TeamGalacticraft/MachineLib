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

package dev.galacticraft.api.client.model;

import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.block.util.BlockFace;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.impl.client.model.MachineBakedModel;
import dev.galacticraft.impl.machine.Constant;
import net.minecraft.block.Block;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public interface MachineModelRegistry {
    Identifier MACHINE = new Identifier(Constant.MOD_ID, "block/machine");
    Identifier MACHINE_SIDE = new Identifier(Constant.MOD_ID, "block/machine_side");

    static void register(Block block, SpriteProvider provider) {
        MachineBakedModel.register(block, provider);
    }

    static Collection<Identifier> getTextureDependencies() {
        return MachineBakedModel.TEXTURE_DEPENDENCIES;
    }

    static @Nullable SpriteProvider getSpriteProvider(Block block) {
        return MachineBakedModel.SPRITE_PROVIDERS.get(block);
    }

    static SpriteProvider getSpriteProviderOrElseGet(Block block, SpriteProvider defaultProvider) {
        return MachineBakedModel.SPRITE_PROVIDERS.getOrDefault(block, defaultProvider);
    }

    static Sprite getSprite(BlockFace face, MachineBlockEntity machine, ItemStack stack, MachineModelRegistry.SpriteProvider provider, ResourceType<?, ?> type, ResourceFlow flow) {
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
        @NotNull Sprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<Identifier, Sprite> atlas);

        @Contract("_ -> new")
        static @NotNull SpriteProvider frontFace(Identifier front) {
            return new MachineBakedModel.FrontFaceSpriteProvider(front);
        }

        @Contract("_ -> new")
        static @NotNull SpriteProvider single(Identifier id) {
            return new MachineBakedModel.SingleSpriteProvider(id);
        }

        @Contract("_ -> new")
        static @NotNull SpriteProvider zAxisSided(Identifier id) {
            return zAxisSided(id, id);
        }

        @Contract("_ -> new")
        static @NotNull SpriteProvider zAxis(Identifier id) {
            return zAxis(id, id);
        }

        @Contract("_, _ -> new")
        static @NotNull SpriteProvider zAxisSided(Identifier front, Identifier back) {
            return new MachineBakedModel.ZAxisSpriteProvider(front, back, true);
        }

        @Contract("_, _ -> new")
        static @NotNull SpriteProvider zAxis(Identifier front, Identifier back) {
            return new MachineBakedModel.ZAxisSpriteProvider(front, back, false);
        }
    }
}
