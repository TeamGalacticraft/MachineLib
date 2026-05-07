package dev.galacticraft.machinelib.client.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side index of formed multiblock parts.
 *
 * <p>This exists to prevent client-side interaction prediction from treating
 * formed multiblock parts as ordinary blocks when the server has already claimed
 * those positions as part of a MachineLib multiblock.</p>
 */
public final class ClientMultiblockManager {

    private static final Map<BlockPos, ClientMultiblockPartData> PARTS_BY_POS =
            new HashMap<>();

    private static final Map<UUID, List<BlockPos>> PARTS_BY_INSTANCE =
            new HashMap<>();

    private ClientMultiblockManager() {

    }

    /**
     * Adds or replaces a synced formed multiblock instance.
     *
     * @param instanceId formed instance id
     * @param definitionId multiblock definition id
     * @param origin multiblock origin
     * @param orientation multiblock orientation
     * @param partPositions synced part positions
     */
    public static void addMachine(
            final UUID instanceId,
            final ResourceLocation definitionId,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final List<BlockPos> partPositions
    ) {
        removeMachine(instanceId);

        final List<BlockPos> immutablePositions = new ArrayList<>();

        for (final BlockPos position : partPositions) {
            final BlockPos immutablePosition = position.immutable();

            immutablePositions.add(immutablePosition);

            PARTS_BY_POS.put(
                    immutablePosition,
                    new ClientMultiblockPartData(
                            instanceId,
                            definitionId,
                            origin.immutable(),
                            orientation,
                            immutablePosition
                    )
            );
        }

        PARTS_BY_INSTANCE.put(instanceId, List.copyOf(immutablePositions));
    }

    /**
     * Removes a synced formed multiblock instance.
     *
     * @param instanceId formed instance id
     */
    public static void removeMachine(final UUID instanceId) {
        final List<BlockPos> positions = PARTS_BY_INSTANCE.remove(instanceId);

        if (positions == null) {
            return;
        }

        for (final BlockPos position : positions) {
            PARTS_BY_POS.remove(position);
        }
    }

    /**
     * Clears all client-side multiblock data.
     */
    public static void clear() {
        PARTS_BY_POS.clear();
        PARTS_BY_INSTANCE.clear();
    }

    /**
     * Checks whether a block position is known client-side as a formed
     * multiblock part.
     *
     * @param pos world position
     * @return {@code true} if the position is currently a synced formed part
     */
    public static boolean isKnownPart(final BlockPos pos) {
        return PARTS_BY_POS.containsKey(pos);
    }

    /**
     * Gets synced client part data.
     *
     * @param pos world position
     * @return part data, or {@code null}
     */
    public static ClientMultiblockPartData getPart(final BlockPos pos) {
        return PARTS_BY_POS.get(pos);
    }

}