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

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Default immutable implementation of {@link MultiblockMenuContext}.
 */
public record SimpleMultiblockMenuContext(
        MultiblockPartInteractionContext interaction) implements MultiblockMenuContext {

    /**
     * Creates a menu context from an interaction context.
     *
     * @param interaction interaction that opened the menu
     */
    public SimpleMultiblockMenuContext {
    }

    @Override
    public ServerLevel level() {
        return this.interaction.level();
    }

    @Override
    public ServerPlayer player() {
        return this.interaction.player();
    }

    @Override
    public BlockPos clickedPos() {
        return this.interaction.clickedPos();
    }

    @Override
    public BlockPos origin() {
        return this.interaction.origin();
    }

    @Override
    public UUID instanceId() {
        return this.interaction.instanceId();
    }

    @Override
    public MultiblockOrientation orientation() {
        return this.interaction.orientation();
    }

    @Override
    public MultiblockDefinition definition() {
        return this.interaction.definition();
    }

}