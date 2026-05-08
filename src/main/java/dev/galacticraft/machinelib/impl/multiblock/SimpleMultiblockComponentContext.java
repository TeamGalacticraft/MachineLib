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

package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.components.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;
import java.util.UUID;

/**
 * Default component context backed by a formed multiblock machine.
 */
public record SimpleMultiblockComponentContext(FormedMultiblockMachine machine) implements MultiblockComponentContext {

    /**
     * Creates a component context.
     *
     * @param machine formed machine
     */
    public SimpleMultiblockComponentContext {
    }

    @Override
    public ServerLevel level() {
        return this.machine.level();
    }

    @Override
    public BlockPos origin() {
        return this.machine.origin();
    }

    @Override
    public UUID instanceId() {
        return this.machine.instanceId();
    }

    @Override
    public MultiblockOrientation orientation() {
        return this.machine.orientation();
    }

    @Override
    public MultiblockDefinition definition() {
        return this.machine.definition();
    }

    @Override
    public Set<BlockPos> partPositions() {
        return this.machine.partPositions();
    }

    @Override
    public <T extends MultiblockComponent> T component(final Class<T> type) {
        return this.machine.component(type);
    }

    @Override
    public void setChanged() {
        this.machine.setComponentsChanged();
    }

}