/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.testmod.block;

import dev.galacticraft.machinelib.api.block.SimpleMachineBlock;
import dev.galacticraft.machinelib.testmod.Constant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class TestModBlocks {
    public static final Block GENERATOR = new GeneratorBlock(BlockBehaviour.Properties.of().sound(SoundType.METAL).mapColor(MapColor.METAL));
    public static final Block MIXER = new SimpleMachineBlock(BlockBehaviour.Properties.of().sound(SoundType.METAL).mapColor(MapColor.METAL), Constant.id(Constant.MIXER));
    public static final Block MELTER = new SimpleMachineBlock(BlockBehaviour.Properties.of().sound(SoundType.METAL).mapColor(MapColor.METAL), Constant.id(Constant.MELTER));
    public static final Block WIRE = new SimpleWireBlock(BlockBehaviour.Properties.of().sound(SoundType.METAL).mapColor(MapColor.METAL));

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, Constant.id(Constant.GENERATOR), TestModBlocks.GENERATOR);
        Registry.register(BuiltInRegistries.BLOCK, Constant.id(Constant.MIXER), TestModBlocks.MIXER);
        Registry.register(BuiltInRegistries.BLOCK, Constant.id(Constant.MELTER), TestModBlocks.MELTER);
        Registry.register(BuiltInRegistries.BLOCK, Constant.id(Constant.WIRE), TestModBlocks.WIRE);
    }
}
