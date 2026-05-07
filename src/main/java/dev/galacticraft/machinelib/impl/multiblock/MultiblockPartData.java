package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public final class MultiblockPartData {

    private static final String INSTANCE_ID = "InstanceId";
    private static final String DEFINITION_ID = "DefinitionId";
    private static final String ORIGIN_X = "OriginX";
    private static final String ORIGIN_Y = "OriginY";
    private static final String ORIGIN_Z = "OriginZ";
    private static final String WORLD_X = "WorldX";
    private static final String WORLD_Y = "WorldY";
    private static final String WORLD_Z = "WorldZ";
    private static final String ORIGINAL_RELATIVE_X = "OriginalRelativeX";
    private static final String ORIGINAL_RELATIVE_Y = "OriginalRelativeY";
    private static final String ORIGINAL_RELATIVE_Z = "OriginalRelativeZ";
    private static final String TRANSFORMED_RELATIVE_X = "TransformedRelativeX";
    private static final String TRANSFORMED_RELATIVE_Y = "TransformedRelativeY";
    private static final String TRANSFORMED_RELATIVE_Z = "TransformedRelativeZ";
    private static final String FORWARD = "Forward";
    private static final String UP = "Up";

    private final UUID instanceId;
    private final ResourceLocation definitionId;
    private final BlockPos origin;
    private final BlockPos worldPos;
    private final BlockPos originalRelativePos;
    private final BlockPos transformedRelativePos;
    private final MultiblockOrientation orientation;

    public MultiblockPartData(
            final UUID instanceId,
            final ResourceLocation definitionId,
            final BlockPos origin,
            final BlockPos worldPos,
            final BlockPos originalRelativePos,
            final BlockPos transformedRelativePos,
            final MultiblockOrientation orientation
    ) {
        this.instanceId = instanceId;
        this.definitionId = definitionId;
        this.origin = origin.immutable();
        this.worldPos = worldPos.immutable();
        this.originalRelativePos = originalRelativePos.immutable();
        this.transformedRelativePos = transformedRelativePos.immutable();
        this.orientation = orientation;
    }

    /**
     * Returns the unique runtime identity of the formed multiblock instance.
     */
    public UUID instanceId() {
        return this.instanceId;
    }

    /**
     * Returns the registered multiblock definition id this part belongs to.
     */
    public ResourceLocation definitionId() {
        return this.definitionId;
    }

    /**
     * Returns the world-space origin of the formed multiblock.
     */
    public BlockPos origin() {
        return this.origin;
    }

    /**
     * Returns the world-space position of this specific part.
     */
    public BlockPos worldPos() {
        return this.worldPos;
    }

    /**
     * Returns this part's untransformed pattern-space position.
     */
    public BlockPos originalRelativePos() {
        return this.originalRelativePos;
    }

    /**
     * Returns this part's transformed relative position after orientation is applied.
     */
    public BlockPos transformedRelativePos() {
        return this.transformedRelativePos;
    }

    /**
     * Returns the orientation used by the formed multiblock.
     */
    public MultiblockOrientation orientation() {
        return this.orientation;
    }

    /**
     * Serializes this part data for storage on a block entity or level save structure.
     */
    public CompoundTag save() {
        final CompoundTag tag = new CompoundTag();

        tag.putUUID(INSTANCE_ID, this.instanceId);
        tag.putString(DEFINITION_ID, this.definitionId.toString());

        tag.putInt(ORIGIN_X, this.origin.getX());
        tag.putInt(ORIGIN_Y, this.origin.getY());
        tag.putInt(ORIGIN_Z, this.origin.getZ());

        tag.putInt(WORLD_X, this.worldPos.getX());
        tag.putInt(WORLD_Y, this.worldPos.getY());
        tag.putInt(WORLD_Z, this.worldPos.getZ());

        tag.putInt(ORIGINAL_RELATIVE_X, this.originalRelativePos.getX());
        tag.putInt(ORIGINAL_RELATIVE_Y, this.originalRelativePos.getY());
        tag.putInt(ORIGINAL_RELATIVE_Z, this.originalRelativePos.getZ());

        tag.putInt(TRANSFORMED_RELATIVE_X, this.transformedRelativePos.getX());
        tag.putInt(TRANSFORMED_RELATIVE_Y, this.transformedRelativePos.getY());
        tag.putInt(TRANSFORMED_RELATIVE_Z, this.transformedRelativePos.getZ());

        tag.putString(FORWARD, this.orientation.forward().getName());
        tag.putString(UP, this.orientation.up().getName());

        return tag;
    }

    /**
     * Deserializes multiblock part data from NBT.
     *
     * <p>This only reconstructs identity and placement data. It does not register
     * or validate the formed multiblock by itself.</p>
     */
    public static MultiblockPartData load(final CompoundTag tag) {
        final UUID instanceId = tag.getUUID(INSTANCE_ID);
        final ResourceLocation definitionId = ResourceLocation.parse(tag.getString(DEFINITION_ID));

        final BlockPos origin = new BlockPos(
                tag.getInt(ORIGIN_X),
                tag.getInt(ORIGIN_Y),
                tag.getInt(ORIGIN_Z)
        );

        final BlockPos worldPos = new BlockPos(
                tag.getInt(WORLD_X),
                tag.getInt(WORLD_Y),
                tag.getInt(WORLD_Z)
        );

        final BlockPos originalRelativePos = new BlockPos(
                tag.getInt(ORIGINAL_RELATIVE_X),
                tag.getInt(ORIGINAL_RELATIVE_Y),
                tag.getInt(ORIGINAL_RELATIVE_Z)
        );

        final BlockPos transformedRelativePos = new BlockPos(
                tag.getInt(TRANSFORMED_RELATIVE_X),
                tag.getInt(TRANSFORMED_RELATIVE_Y),
                tag.getInt(TRANSFORMED_RELATIVE_Z)
        );

        final Direction forward = Direction.byName(tag.getString(FORWARD));
        final Direction up = Direction.byName(tag.getString(UP));

        if (forward == null || up == null) {
            throw new IllegalArgumentException("Invalid multiblock orientation in saved part data.");
        }

        final MultiblockOrientation orientation = findOrientation(forward, up);

        return new MultiblockPartData(
                instanceId,
                definitionId,
                origin,
                worldPos,
                originalRelativePos,
                transformedRelativePos,
                orientation
        );
    }

    private static MultiblockOrientation findOrientation(final Direction forward, final Direction up) {
        for (final MultiblockOrientation orientation : MultiblockOrientation.all()) {
            if (orientation.forward() == forward && orientation.up() == up) {
                return orientation;
            }
        }

        throw new IllegalArgumentException("No multiblock orientation exists for forward=" + forward + ", up=" + up);
    }

}