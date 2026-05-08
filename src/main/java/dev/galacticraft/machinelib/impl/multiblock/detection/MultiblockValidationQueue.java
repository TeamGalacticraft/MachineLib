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

import dev.galacticraft.machinelib.api.multiblock.FormationResult;
import dev.galacticraft.machinelib.api.multiblock.FormationRule;
import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockManager;
import dev.galacticraft.machinelib.impl.multiblock.MultiblockPart;
import dev.galacticraft.machinelib.impl.multiblock.SimpleFormationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class MultiblockValidationQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger("MachineLib/Multiblocks");

    private final Queue<MultiblockCandidate> queue = new ArrayDeque<>();

    private final Set<MultiblockCandidate> queued = new HashSet<>();

    public void enqueue(final MultiblockCandidate candidate) {
        if (!this.queued.add(candidate)) {
            return;
        }

        this.queue.add(candidate);
    }

    public void processAll() {
        while (!this.queue.isEmpty()) {
            final MultiblockCandidate candidate = this.queue.poll();

            this.queued.remove(candidate);

            this.validate(candidate);
        }
    }

    public void process(final int limit) {
        int processed = 0;

        while (processed < limit && !this.queue.isEmpty()) {
            final MultiblockCandidate candidate = this.queue.poll();

            this.queued.remove(candidate);

            this.validate(candidate);

            processed++;
        }
    }

    private void validate(final MultiblockCandidate candidate) {
        final MultiblockDefinition definition = candidate.definition();
        final MultiblockPattern pattern = definition.pattern();

        if (pattern == null) {
            LOGGER.warn(
                    "Cannot validate multiblock {} because it has no pattern",
                    definition.id()
            );

            return;
        }

        final List<MultiblockPart> parts =
                new ArrayList<>();

        for (int x = 0; x < pattern.sizeX(); x++) {
            for (int y = 0; y < pattern.sizeY(); y++) {
                for (int z = 0; z < pattern.sizeZ(); z++) {
                    final MultiblockSlotPredicate predicate = pattern.predicateAt(x, y, z);

                    if (predicate == null) {
                        continue;
                    }

                    final BlockPos originalRelativePos = new BlockPos(x, y, z);

                    final BlockPos transformedRelativePos = candidate.orientation().transformRelative(
                            originalRelativePos,
                            pattern.sizeX(),
                            pattern.sizeY(),
                            pattern.sizeZ()
                    );

                    final BlockPos worldPos = candidate.origin().offset(
                            transformedRelativePos.getX(),
                            transformedRelativePos.getY(),
                            transformedRelativePos.getZ()
                    );

                    final BlockState state = candidate.level().getBlockState(worldPos);

                    if (!predicate.matches(candidate.level(), worldPos, state)) {
                        return;
                    }

                    parts.add(
                            new MultiblockPart(
                                    worldPos,
                                    originalRelativePos,
                                    transformedRelativePos,
                                    predicate
                            )
                    );
                }
            }
        }

        final SimpleFormationContext context =
                new SimpleFormationContext(
                        candidate.level(),
                        candidate.origin(),
                        candidate.orientation(),
                        definition
                );

        for (final FormationRule rule : definition.rules()) {
            final FormationResult result = rule.check(context);

            if (!result.isSuccess()) {
                return;
            }
        }

        final MultiblockManager manager =
                MultiblockManager.get(candidate.level());

        if (!manager.canRegister(candidate.origin(), parts)) {
            return;
        }

        manager.register(
                candidate.origin(),
                candidate.orientation(),
                definition,
                parts
        );
    }

}