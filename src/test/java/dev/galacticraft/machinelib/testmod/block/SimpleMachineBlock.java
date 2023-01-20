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

package dev.galacticraft.machinelib.testmod.block;

import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.testmod.block.entity.SimpleMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SimpleMachineBlock extends MachineBlock<SimpleMachineBlockEntity> {
    public SimpleMachineBlock(Block.Properties settings) {
        super(settings);
    }

    @Override
    public SimpleMachineBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SimpleMachineBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (placer != null) {
            placer.push(0, 1, 0);
        }
    }

    @Override
    public Component machineDescription(ItemStack stack, BlockGetter view, boolean advanced) {
        return Component.empty();
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        double d = blockPos.getX() + 0.4 + randomSource.nextFloat() * 0.2;
        double e = blockPos.getY() + 0.7 + randomSource.nextFloat() * 0.3;
        double f = blockPos.getZ() + 0.4 + randomSource.nextFloat() * 0.2;
        level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
    }
}
