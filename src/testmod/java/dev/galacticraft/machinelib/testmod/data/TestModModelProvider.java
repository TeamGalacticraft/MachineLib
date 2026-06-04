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

package dev.galacticraft.machinelib.testmod.data;

import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.data.model.MachineModelGenerator;
import dev.galacticraft.machinelib.client.api.model.MachineTextureBase;
import dev.galacticraft.machinelib.client.api.model.TextureProvider;
import dev.galacticraft.machinelib.testmod.Constant;
import dev.galacticraft.machinelib.testmod.block.TestModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class TestModModelProvider extends FabricModelProvider {
    public TestModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators gen) {
        MachineModelGenerator.setupMachineBaseTextures(gen, Constant.MOD_ID, MachineTextureBase.prefixed(Constant.MOD_ID, "block/machine"));

        ResourceLocation melter = MachineModelGenerator.generateMachineModel(gen, TestModBlocks.MELTER, TextureProvider.builder()
                .front(TextureMapping.getBlockTexture(Blocks.BLAST_FURNACE, "_front"))
                .back(TextureMapping.getBlockTexture(Blocks.BLAST_FURNACE, "_front"))
                .build()
        );

        ResourceLocation melterActive = MachineModelGenerator.generateMachineModel(gen, MachineModelGenerator.getMachineModelLocation(TestModBlocks.MELTER, "_active"), TextureProvider.builder()
                .front(TextureMapping.getBlockTexture(Blocks.BLAST_FURNACE, "_front_on"))
                .back(TextureMapping.getBlockTexture(Blocks.BLAST_FURNACE, "_front_on"))
                .build()
        );

        gen.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(TestModBlocks.MELTER)
                                .with(BlockModelGenerators.createBooleanModelDispatch(MachineBlock.ACTIVE, melterActive, melter))
                );
        gen.skipAutoItemBlock(TestModBlocks.MELTER);

        MachineModelGenerator.createTrivialMachine(gen, TestModBlocks.GENERATOR, TextureProvider.builder()
                .front(TextureMapping.getBlockTexture(Blocks.FURNACE, "_front"))
                .topOverride(TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
                .build());
        MachineModelGenerator.createTrivialMachine(gen, TestModBlocks.MIXER, TextureProvider.none());
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
    }
}
