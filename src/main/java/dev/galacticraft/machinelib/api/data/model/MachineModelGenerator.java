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

package dev.galacticraft.machinelib.api.data.model;

import dev.galacticraft.machinelib.client.api.model.MachineTextureBase;
import dev.galacticraft.machinelib.client.api.model.TextureProvider;
import dev.galacticraft.machinelib.client.impl.data.model.MachineModelData;
import dev.galacticraft.machinelib.client.impl.data.model.MachineTextureBaseData;
import dev.galacticraft.machinelib.client.impl.model.MachineModelLoadingPlugin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class MachineModelGenerator {
    public static ResourceLocation getMachineModelLocation(Block block) {
        ResourceLocation resourceLocation = BuiltInRegistries.BLOCK.getKey(block);
        return resourceLocation.withPrefix("machine/");
    }

    public static ResourceLocation getMachineModelLocation(Block block, String suffix) {
        return getMachineModelLocation(block).withSuffix(suffix);
    }

    public static void setupMachineBaseTextures(BlockModelGenerators gen, String namespace, MachineTextureBase base) {
        setupMachineBaseTextures(gen, ResourceLocation.fromNamespaceAndPath(namespace, MachineModelLoadingPlugin.DEFAULT_MACHINE_BASE), base);
    }

    public static void setupMachineBaseTextures(BlockModelGenerators gen, ResourceLocation id, MachineTextureBase base) {
        gen.modelOutput.accept(id, new MachineTextureBaseData(base));
    }

    public static ResourceLocation generateMachineModel(BlockModelGenerators gen, Block block, TextureProvider textureProvider) {
        return generateMachineModel(gen, block, null, textureProvider);
    }

    public static ResourceLocation generateMachineModel(BlockModelGenerators gen, Block block, @Nullable ResourceLocation base, TextureProvider textureProvider) {
        return generateMachineModel(gen, getMachineModelLocation(block), base, textureProvider);
    }

    public static ResourceLocation generateMachineModel(BlockModelGenerators gen, ResourceLocation id, TextureProvider textureProvider) {
        return generateMachineModel(gen, id, null, textureProvider);
    }

    public static ResourceLocation generateMachineModel(BlockModelGenerators gen, ResourceLocation id, @Nullable ResourceLocation base, TextureProvider textureProvider) {
        gen.modelOutput.accept(id, new MachineModelData(base, textureProvider));
        return id;
    }

    public static void createTrivialMachine(BlockModelGenerators gen, Block block, TextureProvider textureProvider) {
        createTrivialMachine(gen, block, null, textureProvider);
    }

    public static void createTrivialMachine(BlockModelGenerators gen, Block block, @Nullable ResourceLocation base, TextureProvider textureProvider) {
        gen.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, generateMachineModel(gen, block, base, textureProvider))));
        gen.skipAutoItemBlock(block);
    }
}
