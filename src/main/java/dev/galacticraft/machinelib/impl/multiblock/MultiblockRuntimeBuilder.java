package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class MultiblockRuntimeBuilder {

    private MultiblockRuntimeBuilder() {
    }

    /**
     * Rebuilds the runtime part list for a formed multiblock from its saved identity data.
     *
     * <p>This method is intentionally side-effect free. It does not register the machine,
     * write saved data, validate formation rules, or modify the world. It only reconstructs
     * the same {@link MultiblockPart} list that would have existed when the multiblock was
     * originally formed.</p>
     *
     * <p>The returned parts are based on:</p>
     *
     * <ul>
     *     <li>the definition's pattern-space slot positions,</li>
     *     <li>the supplied origin,</li>
     *     <li>the supplied orientation transform,</li>
     *     <li>and each slot's original predicate.</li>
     * </ul>
     *
     * @param level the level the multiblock exists in
     * @param origin the world-space origin of the multiblock
     * @param orientation the saved orientation of the multiblock
     * @param definition the registered multiblock definition
     * @return rebuilt runtime parts for the multiblock
     */
    public static List<MultiblockPart> buildParts(
            final ServerLevel level,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition
    ) {
        final MultiblockPattern pattern = definition.pattern();
        final List<MultiblockPart> parts = new ArrayList<>();

        for (int x = 0; x < pattern.sizeX(); x++) {
            for (int y = 0; y < pattern.sizeY(); y++) {
                for (int z = 0; z < pattern.sizeZ(); z++) {
                    final MultiblockSlotPredicate predicate = pattern.predicateAt(x, y, z);

                    if (predicate == null) {
                        continue;
                    }

                    final BlockPos originalRelativePos = new BlockPos(x, y, z);
                    final BlockPos transformedRelativePos = orientation.transformRelative(
                            originalRelativePos,
                            pattern.sizeX(),
                            pattern.sizeY(),
                            pattern.sizeZ()
                    );
                    final BlockPos worldPos = origin.offset(transformedRelativePos);

                    parts.add(new MultiblockPart(
                            worldPos,
                            originalRelativePos,
                            transformedRelativePos,
                            predicate
                    ));
                }
            }
        }

        return List.copyOf(parts);
    }

    /**
     * Rebuilds and validates the runtime part list for a saved multiblock.
     *
     * <p>This is useful during persistent reload, where MachineLib needs to check whether a
     * saved multiblock still exists in the world before restoring it into the runtime manager.
     * This method only validates slot predicates. Formation rules should still be checked by
     * the caller if rule-level validation is required.</p>
     *
     * @param level the level the multiblock exists in
     * @param origin the world-space origin of the multiblock
     * @param orientation the saved orientation of the multiblock
     * @param definition the registered multiblock definition
     * @return rebuilt runtime parts if every slot still matches, otherwise {@code null}
     */
    public static List<MultiblockPart> buildValidPartsOrNull(
            final ServerLevel level,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition
    ) {
        final List<MultiblockPart> parts = buildParts(
                level,
                origin,
                orientation,
                definition
        );

        for (final MultiblockPart part : parts) {
            final BlockState state = level.getBlockState(part.worldPos());

            if (!part.predicate().matches(level, part.worldPos(), state)) {
                return null;
            }
        }

        return parts;
    }

    /**
     * Checks whether every rebuilt part still matches the world.
     *
     * @param level the level to check
     * @param parts the rebuilt part list
     * @return {@code true} if all part predicates still match their world positions
     */
    public static boolean arePartsValid(
            final ServerLevel level,
            final List<MultiblockPart> parts
    ) {
        for (final MultiblockPart part : parts) {
            final BlockState state = level.getBlockState(part.worldPos());

            if (!part.predicate().matches(level, part.worldPos(), state)) {
                return false;
            }
        }

        return true;
    }

}