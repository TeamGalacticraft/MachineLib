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

package dev.galacticraft.machinelib.testmod.block.entity;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.testmod.Constant;
import dev.galacticraft.machinelib.testmod.block.TestModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TestModBlockEntityTypes {
    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constant.id(Constant.GENERATOR), TestModBlockEntityTypes.GENERATOR);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constant.id(Constant.MIXER), TestModBlockEntityTypes.MIXER);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constant.id(Constant.MELTER), TestModBlockEntityTypes.MELTER);

        MachineBlockEntity.registerProviders(GENERATOR);
        MachineBlockEntity.registerProviders(MIXER);
        MachineBlockEntity.registerProviders(MELTER);
    }    public static final BlockEntityType<GeneratorBlockEntity> GENERATOR = BlockEntityType.Builder.of(GeneratorBlockEntity::new, TestModBlocks.GENERATOR).build(null);
    public static final BlockEntityType<MixerBlockEntity> MIXER = BlockEntityType.Builder.of(MixerBlockEntity::new, TestModBlocks.MIXER).build(null);
    public static final BlockEntityType<MelterBlockEntity> MELTER = BlockEntityType.Builder.of(MelterBlockEntity::new, TestModBlocks.MELTER).build(null);


}
