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

package dev.galacticraft.machinelib.impl.multiblock.detection;

import dev.galacticraft.machinelib.api.multiblock.*;
import dev.galacticraft.machinelib.impl.multiblock.CompileFormationContext;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class MultiblockDetectionIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger("MachineLib/Multiblocks");

    private final Map<Block, CompiledVariantBucket> buckets = new HashMap<>();

    private final MultiblockValidationQueue validationQueue = new MultiblockValidationQueue();

    public void compile(final Collection<MultiblockDefinition> definitions) {
        this.buckets.clear();

        int compiledVariantCount = 0;

        for (final MultiblockDefinition definition : definitions) {
            compiledVariantCount += this.compile(definition);
        }

        for (final CompiledVariantBucket bucket : this.buckets.values()) {
            bucket.sort();
        }

        LOGGER.info(
                "Compiled multiblock detection index: definitions={}, blockBuckets={}, localVariants={}",
                definitions.size(),
                this.buckets.size(),
                compiledVariantCount
        );
    }

    public void onBlockChanged(
            final ServerLevel level,
            final BlockPos pos,
            final BlockState state
    ) {
        final CompiledVariantBucket bucket = this.buckets.get(state.getBlock());

        if (bucket == null) {
            return;
        }

        bucket.test(level, pos, this.validationQueue);

        final MultiblockValidationMode mode = MultiblockConfig.validationMode();

        if (mode == MultiblockValidationMode.IMMEDIATE) {
            this.validationQueue.processAll();
        }
    }

    public void processQueuedValidations(final int limit) {
        this.validationQueue.process(limit);
    }

    private int compile(final MultiblockDefinition definition) {
        final MultiblockPattern pattern = definition.pattern();

        if (pattern == null) {
            return 0;
        }

        int compiled = 0;

        for (final MultiblockOrientation orientation : MultiblockOrientation.values()) {
            if (!this.rulesAllowCompileVariant(definition, orientation)) {
                continue;
            }

            compiled += this.compile(definition, pattern, orientation);
        }

        LOGGER.debug(
                "Compiled multiblock {} with {} local variants",
                definition.id(),
                compiled
        );

        return compiled;
    }

    private boolean rulesAllowCompileVariant(
            final MultiblockDefinition definition,
            final MultiblockOrientation orientation
    ) {
        final CompileFormationContext context = new CompileFormationContext(
                orientation,
                definition
        );

        for (final FormationRule rule : definition.rules()) {
            final FormationResult result = rule.check(context);

            if (!result.isSuccess()) {
                return false;
            }
        }

        return true;
    }

    private int compile(
            final MultiblockDefinition definition,
            final MultiblockPattern pattern,
            final MultiblockOrientation orientation
    ) {
        int compiled = 0;

        for (int x = 0; x < pattern.sizeX(); x++) {
            for (int y = 0; y < pattern.sizeY(); y++) {
                for (int z = 0; z < pattern.sizeZ(); z++) {
                    final MultiblockSlotPredicate centerPredicate = pattern.predicateAt(x, y, z);

                    if (centerPredicate == null || !centerPredicate.canBeDetectionCenter()) {
                        continue;
                    }

                    final Block[] detectionBlocks = centerPredicate.detectionBlocks();

                    if (detectionBlocks.length == 0) {
                        continue;
                    }

                    final BlockPos transformedRelativePos = orientation.transformRelative(
                            x,
                            y,
                            z,
                            pattern.sizeX(),
                            pattern.sizeY(),
                            pattern.sizeZ()
                    );

                    final CompiledLocalVariant variant = new CompiledLocalVariant(
                            definition,
                            orientation,
                            transformedRelativePos,
                            this.neighbourPredicate(pattern, orientation, x, y, z, Direction.UP),
                            this.neighbourPredicate(pattern, orientation, x, y, z, Direction.DOWN),
                            this.neighbourPredicate(pattern, orientation, x, y, z, Direction.NORTH),
                            this.neighbourPredicate(pattern, orientation, x, y, z, Direction.SOUTH),
                            this.neighbourPredicate(pattern, orientation, x, y, z, Direction.EAST),
                            this.neighbourPredicate(pattern, orientation, x, y, z, Direction.WEST)
                    );

                    for (final Block detectionBlock : detectionBlocks) {
                        this.bucket(detectionBlock).add(variant);
                        compiled++;
                    }
                }
            }
        }

        return compiled;
    }

    private CompiledVariantBucket bucket(final Block block) {
        return this.buckets.computeIfAbsent(block, CompiledVariantBucket::new);
    }

    private MultiblockSlotPredicate neighbourPredicate(
            final MultiblockPattern pattern,
            final MultiblockOrientation orientation,
            final int sourceX,
            final int sourceY,
            final int sourceZ,
            final Direction worldDirection
    ) {
        final Direction localDirection = this.inverseTransformDirection(
                orientation,
                worldDirection
        );

        final int nx = sourceX + localDirection.getStepX();
        final int ny = sourceY + localDirection.getStepY();
        final int nz = sourceZ + localDirection.getStepZ();

        if (nx < 0 || ny < 0 || nz < 0) {
            return null;
        }

        if (nx >= pattern.sizeX() || ny >= pattern.sizeY() || nz >= pattern.sizeZ()) {
            return null;
        }

        return pattern.predicateAt(nx, ny, nz);
    }

    private Direction inverseTransformDirection(
            final MultiblockOrientation orientation,
            final Direction worldDirection
    ) {
        for (final Direction localDirection : Direction.values()) {
            if (orientation.transformDirection(localDirection) == worldDirection) {
                return localDirection;
            }
        }

        throw new IllegalStateException(
                "Could not inverse-transform direction " + worldDirection + " for " + orientation
        );
    }

}