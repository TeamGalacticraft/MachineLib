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

package dev.galacticraft.machinelib.impl.block.entity;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of a {@link BlockEntityTicker} for {@link MachineBlockEntity}s for convenience.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public final class MachineBlockEntityTicker<T extends BlockEntity> implements BlockEntityTicker<T> {
    private static final MachineBlockEntityTicker<? extends MachineBlockEntity> INSTANCE = new MachineBlockEntityTicker<>();

    private MachineBlockEntityTicker() {}

    @Contract(pure = true)
    public static <T extends BlockEntity> @NotNull MachineBlockEntityTicker<T> getInstance() {
        return (MachineBlockEntityTicker<T>) INSTANCE;
    }

    @Override
    public void tick(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull T machine) {
        ((MachineBlockEntity) machine).tickBase(world, pos, state, world.getProfiler());
    }
}
