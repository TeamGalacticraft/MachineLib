/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.testmod.Constant;
import dev.galacticraft.machinelib.testmod.block.entity.GeneratorBlockEntity;
import dev.galacticraft.machinelib.testmod.block.entity.TestModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorBlock extends MachineBlock<GeneratorBlockEntity> {
    public GeneratorBlock(Properties settings) {
        super(settings, Constant.id(Constant.GENERATOR));
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (MachineBlock.isActive(blockState)) {
            double d = blockPos.getX() + 0.4 + randomSource.nextFloat() * 0.2;
            double e = blockPos.getY() + 0.7 + randomSource.nextFloat() * 0.3;
            double f = blockPos.getZ() + 0.4 + randomSource.nextFloat() * 0.2;
            level.addParticle(ParticleTypes.SMOKE, d, e, f, randomSource.nextFloat() * 0.07, randomSource.nextFloat() * 0.05, randomSource.nextFloat() * 0.07);
        }
    }
}
