package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent per-dimension saved data for MachineLib multiblocks.
 *
 * <p>This data is the source of truth for formed multiblocks that should survive
 * world saves, server restarts, and chunk unloading. The runtime
 * {@link MultiblockManager} only tracks loaded machines, while this class tracks
 * all saved formed machines in the dimension.</p>
 *
 * <p>Saved machines intentionally store only stable identity and placement data:
 * instance id, definition id, origin, and orientation. Runtime part lists are
 * rebuilt from the registered definition when needed.</p>
 */
public final class MultiblockSavedData extends SavedData {

    private static final String DATA_NAME = "machinelib_multiblocks";
    private static final String MACHINES = "Machines";

    private final Map<UUID, SavedMachine> machines = new HashMap<>();

    /**
     * Gets or creates the saved multiblock data for a server level.
     *
     * @param level server level
     * @return saved multiblock data
     */
    public static MultiblockSavedData get(final ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        MultiblockSavedData::new,
                        MultiblockSavedData::load,
                        null
                ),
                DATA_NAME
        );
    }

    /**
     * Loads saved multiblock data from NBT.
     *
     * @param tag saved root tag
     * @param registries registry lookup provider
     * @return loaded saved data
     */
    public static MultiblockSavedData load(
            final CompoundTag tag,
            final HolderLookup.Provider registries
    ) {
        final MultiblockSavedData data = new MultiblockSavedData();
        final ListTag list = tag.getList(MACHINES, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            final SavedMachine machine = SavedMachine.load(list.getCompound(i));
            data.machines.put(machine.instanceId(), machine);
        }

        return data;
    }

    /**
     * Saves all persistent multiblock records to NBT.
     *
     * @param tag root tag to write into
     * @param registries registry lookup provider
     * @return written root tag
     */
    @Override
    public CompoundTag save(
            final CompoundTag tag,
            final HolderLookup.Provider registries
    ) {
        final ListTag list = new ListTag();

        for (final SavedMachine machine : this.machines.values()) {
            list.add(machine.save());
        }

        tag.put(MACHINES, list);
        return tag;
    }

    /**
     * Gets all saved machines.
     *
     * @return immutable saved machine collection
     */
    public Collection<SavedMachine> machines() {
        return Collections.unmodifiableCollection(this.machines.values());
    }

    /**
     * Gets a saved machine by instance id.
     *
     * @param instanceId instance id
     * @return saved machine, or {@code null}
     */
    public SavedMachine get(final UUID instanceId) {
        return this.machines.get(instanceId);
    }

    /**
     * Checks whether the saved data already contains an instance id.
     *
     * @param instanceId instance id
     * @return {@code true} if a saved machine exists with that id
     */
    public boolean containsInstance(final UUID instanceId) {
        return this.machines.containsKey(instanceId);
    }

    /**
     * Checks whether any saved machine uses the supplied origin.
     *
     * @param origin world-space origin
     * @return {@code true} if a saved machine already exists at the origin
     */
    public boolean containsOrigin(final BlockPos origin) {
        for (final SavedMachine machine : this.machines.values()) {
            if (machine.origin().equals(origin)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether any saved machine uses the supplied origin, excluding a known
     * existing instance id.
     *
     * <p>This is useful when restoring a saved machine into runtime indexes,
     * because the machine being restored should not conflict with itself.</p>
     *
     * @param origin world-space origin
     * @param ignoredInstanceId instance id to ignore
     * @return {@code true} if another saved machine uses the origin
     */
    public boolean containsOriginExcept(
            final BlockPos origin,
            final UUID ignoredInstanceId
    ) {
        for (final SavedMachine machine : this.machines.values()) {
            if (machine.instanceId().equals(ignoredInstanceId)) {
                continue;
            }

            if (machine.origin().equals(origin)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a proposed runtime part list overlaps any saved machine.
     *
     * <p>This protects saved-but-unloaded multiblocks from being overwritten by
     * newly formed multiblocks. The supplied parts are compared against rebuilt
     * part positions for every saved machine whose definition is still registered.</p>
     *
     * @param level level containing the proposed parts
     * @param parts proposed runtime parts
     * @return {@code true} if any saved machine overlaps the proposed parts
     */
    public boolean overlapsAnySavedMachine(
            final ServerLevel level,
            final List<MultiblockPart> parts
    ) {
        return this.overlapsAnySavedMachineExcept(level, parts, null);
    }

    /**
     * Checks whether a proposed runtime part list overlaps any saved machine,
     * excluding a known instance id.
     *
     * <p>The exclusion is required during persistent restoration. A saved machine
     * being restored will naturally overlap itself, but that should not block
     * runtime registration.</p>
     *
     * @param level level containing the proposed parts
     * @param parts proposed runtime parts
     * @param ignoredInstanceId instance id to ignore, or {@code null}
     * @return {@code true} if any non-ignored saved machine overlaps the parts
     */
    public boolean overlapsAnySavedMachineExcept(
            final ServerLevel level,
            final List<MultiblockPart> parts,
            final UUID ignoredInstanceId
    ) {
        for (final SavedMachine savedMachine : this.machines.values()) {
            if (ignoredInstanceId != null && savedMachine.instanceId().equals(ignoredInstanceId)) {
                continue;
            }

            final MultiblockDefinition definition = MachineLibMultiblocks.getDefinition(savedMachine.definitionId());

            if (definition == null) {
                continue;
            }

            final List<MultiblockPart> savedParts = MultiblockRuntimeBuilder.buildParts(
                    level,
                    savedMachine.origin(),
                    savedMachine.orientation(),
                    definition
            );

            if (overlaps(parts, savedParts)) {
                return true;
            }
        }

        return false;
    }

    private static boolean overlaps(
            final List<MultiblockPart> first,
            final List<MultiblockPart> second
    ) {
        for (final MultiblockPart a : first) {
            for (final MultiblockPart b : second) {
                if (a.worldPos().equals(b.worldPos())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Adds or replaces a saved machine record from a runtime formed machine.
     *
     * @param machine formed runtime machine
     */
    public void put(final FormedMultiblockMachine machine) {
        this.machines.put(
                machine.instanceId(),
                new SavedMachine(
                        machine.instanceId(),
                        machine.definition().id(),
                        machine.origin(),
                        machine.orientation(),
                        machine.saveComponents()
                )
        );

        machine.clearComponentsChanged();
        this.setDirty();
    }

    /**
     * Removes a saved machine record.
     *
     * @param instanceId instance id to remove
     */
    public void remove(final UUID instanceId) {
        if (this.machines.remove(instanceId) != null) {
            this.setDirty();
        }
    }

    /**
     * Removes all saved machine records.
     */
    public void clearMachines() {
        this.machines.clear();
        this.setDirty();
    }

    /**
     * Updates only the persisted component data for a saved machine.
     *
     * @param machine runtime machine whose components should be saved
     */
    public void updateComponents(final FormedMultiblockMachine machine) {
        final SavedMachine existing = this.machines.get(machine.instanceId());

        if (existing == null) {
            this.put(machine);
            return;
        }

        this.machines.put(
                machine.instanceId(),
                new SavedMachine(
                        existing.instanceId(),
                        existing.definitionId(),
                        existing.origin(),
                        existing.orientation(),
                        machine.saveComponents()
                )
        );

        machine.clearComponentsChanged();
        this.setDirty();
    }

    /**
     * Persistent identity and placement record for one formed multiblock.
     *
     * @param instanceId persistent instance id
     * @param definitionId registered multiblock definition id
     * @param origin world-space origin
     * @param orientation applied orientation
     */
    public record SavedMachine(
            UUID instanceId,
            ResourceLocation definitionId,
            BlockPos origin,
            MultiblockOrientation orientation,
            CompoundTag components
    ) {

        private static final String INSTANCE_ID = "InstanceId";
        private static final String DEFINITION_ID = "DefinitionId";
        private static final String ORIGIN_X = "OriginX";
        private static final String ORIGIN_Y = "OriginY";
        private static final String ORIGIN_Z = "OriginZ";
        private static final String FORWARD = "Forward";
        private static final String UP = "Up";
        private static final String COMPONENTS = "Components";

        /**
         * Saves this machine record to NBT.
         *
         * @return serialized machine tag
         */
        public CompoundTag save() {
            final CompoundTag tag = new CompoundTag();

            tag.putUUID(INSTANCE_ID, this.instanceId);
            tag.putString(DEFINITION_ID, this.definitionId.toString());

            tag.putInt(ORIGIN_X, this.origin.getX());
            tag.putInt(ORIGIN_Y, this.origin.getY());
            tag.putInt(ORIGIN_Z, this.origin.getZ());

            tag.putString(FORWARD, this.orientation.forward().getName());
            tag.putString(UP, this.orientation.up().getName());

            tag.put(
                    COMPONENTS,
                    this.components.copy()
            );

            return tag;
        }

        /**
         * Loads a saved machine record from NBT.
         *
         * @param tag machine tag
         * @return loaded saved machine
         */
        public static SavedMachine load(final CompoundTag tag) {
            final UUID instanceId = tag.getUUID(INSTANCE_ID);
            final ResourceLocation definitionId = ResourceLocation.parse(tag.getString(DEFINITION_ID));

            final BlockPos origin = new BlockPos(
                    tag.getInt(ORIGIN_X),
                    tag.getInt(ORIGIN_Y),
                    tag.getInt(ORIGIN_Z)
            );

            final Direction forward = Direction.byName(tag.getString(FORWARD));
            final Direction up = Direction.byName(tag.getString(UP));

            if (forward == null || up == null) {
                throw new IllegalArgumentException("Invalid saved multiblock orientation.");
            }

            final CompoundTag components;

            if (tag.contains(COMPONENTS, Tag.TAG_COMPOUND)) {
                components = tag.getCompound(COMPONENTS).copy();
            } else {
                components = new CompoundTag();
            }

            return new SavedMachine(
                    instanceId,
                    definitionId,
                    origin,
                    findOrientation(forward, up),
                    components
            );
        }

        private static MultiblockOrientation findOrientation(
                final Direction forward,
                final Direction up
        ) {
            for (final MultiblockOrientation orientation : MultiblockOrientation.all()) {
                if (orientation.forward() == forward && orientation.up() == up) {
                    return orientation;
                }
            }

            throw new IllegalArgumentException("No orientation exists for forward=" + forward + ", up=" + up);
        }

    }

}