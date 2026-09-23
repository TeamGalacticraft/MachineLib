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

package dev.galacticraft.machinelib.impl.compat.waila;

import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.block.entity.RecipeMachineBlockEntity;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.data.ProgressData;

@SuppressWarnings("unused")
public class MachineLibWailaCommonPlugin implements IWailaCommonPlugin {
    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.blockData((IDataProvider<MachineBlockEntity>) (data, accessor, config) -> {
            MachineBlockEntity machine = accessor.getTarget();
            data.raw().put("security", machine.getSecurity().createTag());
            data.raw().put("redstone", machine.getRedstoneMode().createTag());
            if (machine instanceof RecipeMachineBlockEntity<?, ?> recipeMachine) {
                if (recipeMachine.getActiveRecipe() != null) {
                    data.add(ProgressData.TYPE, res ->
                        res.add(ProgressData.ratio(recipeMachine.getProgressRatio())
                                .input(recipeMachine.inputItemStacks())
                                .output(recipeMachine.outputItemStacks()))
                    );
                }
            }
        }, MachineBlock.class);
    }
}
