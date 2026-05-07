package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Runtime representation of a formed multiblock machine.
 *
 * <p>This object is runtime-only. Persistent identity is stored separately in
 * {@link MultiblockSavedData}. A formed machine may be removed from the runtime
 * manager without deleting saved data when its chunks unload.</p>
 */
public final class FormedMultiblockMachine {

    private final UUID instanceId;
    private final ServerLevel level;
    private final BlockPos origin;
    private final MultiblockOrientation orientation;
    private final MultiblockDefinition definition;
    private final List<MultiblockPart> parts;
    private final Set<BlockPos> partPositions;
    private final List<MultiblockPartData> partData;

    private MultiblockState state = MultiblockState.FORMED;

    /**
     * Creates a new formed multiblock machine with a random instance id.
     *
     * @param level level containing the multiblock
     * @param origin world-space origin
     * @param orientation applied orientation
     * @param definition multiblock definition
     * @param parts runtime part list
     */
    public FormedMultiblockMachine(
            final ServerLevel level,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition,
            final List<MultiblockPart> parts
    ) {
        this(
                UUID.randomUUID(),
                level,
                origin,
                orientation,
                definition,
                parts
        );
    }

    /**
     * Creates a formed multiblock machine with a specific persistent instance id.
     *
     * <p>This constructor is used when restoring a machine from saved data.</p>
     *
     * @param instanceId persistent instance id
     * @param level level containing the multiblock
     * @param origin world-space origin
     * @param orientation applied orientation
     * @param definition multiblock definition
     * @param parts runtime part list
     */
    public FormedMultiblockMachine(
            final UUID instanceId,
            final ServerLevel level,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition,
            final List<MultiblockPart> parts
    ) {
        this.instanceId = instanceId;
        this.level = level;
        this.origin = origin.immutable();
        this.orientation = orientation;
        this.definition = definition;
        this.parts = List.copyOf(parts);

        final Set<BlockPos> positions = new HashSet<>();
        final List<MultiblockPartData> data = new ArrayList<>();

        for (final MultiblockPart part : parts) {
            positions.add(part.worldPos());
            data.add(part.createData(
                    this.instanceId,
                    definition.id(),
                    this.origin,
                    this.orientation
            ));
        }

        this.partPositions = Collections.unmodifiableSet(positions);
        this.partData = List.copyOf(data);
    }

    /**
     * @return unique persistent/runtime instance id
     */
    public UUID instanceId() {
        return this.instanceId;
    }

    /**
     * @return level containing the multiblock
     */
    public ServerLevel level() {
        return this.level;
    }

    /**
     * @return world-space origin
     */
    public BlockPos origin() {
        return this.origin;
    }

    /**
     * @return orientation used by the formed machine
     */
    public MultiblockOrientation orientation() {
        return this.orientation;
    }

    /**
     * @return registered multiblock definition
     */
    public MultiblockDefinition definition() {
        return this.definition;
    }

    /**
     * @return immutable runtime part list
     */
    public List<MultiblockPart> parts() {
        return this.parts;
    }

    /**
     * @return immutable serializable part data list
     */
    public List<MultiblockPartData> partData() {
        return this.partData;
    }

    /**
     * @return immutable set of all world positions occupied by this multiblock
     */
    public Set<BlockPos> partPositions() {
        return this.partPositions;
    }

    /**
     * @return current runtime state
     */
    public MultiblockState state() {
        return this.state;
    }

    /**
     * @return {@code true} if this machine is currently formed
     */
    public boolean isFormed() {
        return this.state == MultiblockState.FORMED;
    }

    /**
     * Checks whether a position is part of this formed multiblock.
     *
     * @param pos world position
     * @return {@code true} if the position belongs to this multiblock
     */
    public boolean contains(final BlockPos pos) {
        return this.partPositions.contains(pos);
    }

    /**
     * Gets serializable part data for a specific world position.
     *
     * @param pos part position
     * @return part data, or {@code null} if the position is not part of this machine
     */
    public MultiblockPartData dataForPart(final BlockPos pos) {
        for (final MultiblockPartData data : this.partData) {
            if (data.worldPos().equals(pos)) {
                return data;
            }
        }

        return null;
    }

    /**
     * Validates the current runtime machine while respecting unloaded chunks.
     *
     * <p>If any part chunk is unloaded, this method returns
     * {@link MultiblockValidationStatus#UNLOADED} instead of treating the machine
     * as invalid.</p>
     *
     * @return validation status
     */
    public MultiblockValidationStatus validate() {
        if (this.state != MultiblockState.FORMED) {
            return MultiblockValidationStatus.INVALID;
        }

        for (final MultiblockPart part : this.parts) {
            if (!MultiblockChunkUtil.isBlockLoaded(this.level, part.worldPos())) {
                return MultiblockValidationStatus.UNLOADED;
            }
        }

        for (final MultiblockPart part : this.parts) {
            final BlockState state = this.level.getBlockState(part.worldPos());

            if (!part.predicate().matches(this.level, part.worldPos(), state)) {
                return MultiblockValidationStatus.INVALID;
            }
        }

        return MultiblockValidationStatus.VALID;
    }

    /**
     * Legacy convenience check.
     *
     * @return {@code true} only when the machine is fully loaded and valid
     */
    public boolean stillValid() {
        return this.validate() == MultiblockValidationStatus.VALID;
    }

    /**
     * Marks this runtime machine as invalidated.
     */
    public void invalidate() {
        this.state = MultiblockState.INVALIDATED;
    }

}