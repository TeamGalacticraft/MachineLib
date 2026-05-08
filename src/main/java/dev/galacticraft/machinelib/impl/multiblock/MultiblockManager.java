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
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.impl.network.s2c.MultiblockSyncAddPayload;
import dev.galacticraft.machinelib.impl.network.s2c.MultiblockSyncRemovePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Runtime manager for formed multiblock machines in a single server level.
 */
public final class MultiblockManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("MachineLib/Multiblocks");

    private static final Map<ResourceKey<Level>, MultiblockManager> MANAGERS =
            new HashMap<>();

    private final ServerLevel level;

    private final Map<UUID, FormedMultiblockMachine> machinesById =
            new HashMap<>();

    private final Map<BlockPos, FormedMultiblockMachine> machinesByOrigin =
            new HashMap<>();

    private final Map<BlockPos, FormedMultiblockMachine> machinesByPart =
            new HashMap<>();

    private final Map<ChunkPos, Set<FormedMultiblockMachine>> machinesByChunk =
            new HashMap<>();

    private final MultiblockSavedData savedData;

    private boolean loadedPersistentMachines;

    private MultiblockManager(final ServerLevel level) {
        this.level = level;
        this.savedData = MultiblockSavedData.get(level);
    }

    /**
     * Gets the multiblock manager for a server level.
     *
     * @param level server level
     * @return level manager
     */
    public static MultiblockManager get(final ServerLevel level) {
        return MANAGERS.computeIfAbsent(
                level.dimension(),
                ignored -> new MultiblockManager(level)
        );
    }

    /**
     * Gets the level owned by this manager.
     *
     * @return server level
     */
    public ServerLevel level() {
        return this.level;
    }

    /**
     * Gets the persistent saved-data object used by this manager.
     *
     * @return saved multiblock data
     */
    public MultiblockSavedData savedData() {
        return this.savedData;
    }

    /**
     * Gets a currently loaded runtime machine by instance id.
     *
     * @param id instance id
     * @return formed machine, or {@code null}
     */
    public FormedMultiblockMachine getById(final UUID id) {
        return this.machinesById.get(id);
    }

    /**
     * Gets a currently loaded runtime machine by origin.
     *
     * @param origin multiblock origin
     * @return formed machine, or {@code null}
     */
    public FormedMultiblockMachine getByOrigin(final BlockPos origin) {
        return this.machinesByOrigin.get(origin);
    }

    /**
     * Gets a currently loaded runtime machine by part position.
     *
     * @param pos part position
     * @return formed machine, or {@code null}
     */
    public FormedMultiblockMachine getByPart(final BlockPos pos) {
        return this.machinesByPart.get(pos);
    }

    /**
     * Gets all loaded runtime machines that touch a chunk.
     *
     * @param chunkPos chunk position
     * @return immutable collection of machines touching the chunk
     */
    public Collection<FormedMultiblockMachine> getMachinesIntersectingChunk(final ChunkPos chunkPos) {
        final Set<FormedMultiblockMachine> machines = this.machinesByChunk.get(chunkPos);

        if (machines == null) {
            return List.of();
        }

        return Collections.unmodifiableSet(machines);
    }

    /**
     * Handles player interaction with a formed multiblock part.
     *
     * @param player interacting player
     * @param pos clicked block position
     * @param hand interaction hand
     * @param hit block hit result
     * @return interaction result
     */
    public InteractionResult handleUsePart(
            final ServerPlayer player,
            final BlockPos pos,
            final InteractionHand hand,
            final BlockHitResult hit
    ) {
        final FormedMultiblockMachine machine = this.machinesByPart.get(pos);

        if (machine == null) {
            return InteractionResult.PASS;
        }

        final MultiblockValidationStatus status = machine.validate();

        if (status == MultiblockValidationStatus.UNLOADED) {
            this.unloadRuntimeOnly(machine);
            return InteractionResult.PASS;
        }

        if (status == MultiblockValidationStatus.INVALID) {
            this.invalidate(machine);
            return InteractionResult.PASS;
        }

        return machine.definition().usePart(new SimpleMultiblockPartInteractionContext(
                machine,
                player,
                pos,
                hand,
                hit
        ));
    }

    /**
     * Gets serializable part data for a currently loaded runtime part.
     *
     * @param pos part position
     * @return part data, or {@code null}
     */
    public MultiblockPartData getPartData(final BlockPos pos) {
        final FormedMultiblockMachine machine = this.machinesByPart.get(pos);

        if (machine == null) {
            return null;
        }

        return machine.dataForPart(pos);
    }

    /**
     * Checks whether a position belongs to a currently loaded formed multiblock.
     *
     * @param pos world position
     * @return {@code true} if the position belongs to a loaded runtime machine
     */
    public boolean isPartOfFormedMultiblock(final BlockPos pos) {
        return this.machinesByPart.containsKey(pos);
    }

    /**
     * Handles a block change affecting a possible runtime multiblock part.
     *
     * @param pos changed block position
     * @return {@code true} if the changed block was inside a still-valid machine
     */
    public boolean handleBlockChanged(final BlockPos pos) {
        final FormedMultiblockMachine machine = this.machinesByPart.get(pos);

        if (machine == null) {
            return false;
        }

        final MultiblockValidationStatus status = machine.validate();

        if (status == MultiblockValidationStatus.VALID) {
            return true;
        }

        if (status == MultiblockValidationStatus.UNLOADED) {
            this.unloadRuntimeOnly(machine);
            return false;
        }

        this.invalidate(machine);
        return false;
    }

    /**
     * Checks whether a newly formed machine can be registered.
     *
     * @param origin proposed origin
     * @param parts proposed part list
     * @return {@code true} if the machine can be registered safely
     */
    public boolean canRegister(
            final BlockPos origin,
            final List<MultiblockPart> parts
    ) {
        return this.canRegister(
                origin,
                parts,
                null
        );
    }

    private boolean canRegister(
            final BlockPos origin,
            final List<MultiblockPart> parts,
            final UUID ignoredSavedInstanceId
    ) {
        if (this.machinesByOrigin.containsKey(origin)) {
            return false;
        }

        for (final MultiblockPart part : parts) {
            if (this.machinesByPart.containsKey(part.worldPos())) {
                return false;
            }
        }

        if (ignoredSavedInstanceId == null) {
            if (this.savedData.containsOrigin(origin)) {
                return false;
            }

            return !this.savedData.overlapsAnySavedMachine(this.level, parts);
        }

        if (this.savedData.containsOriginExcept(origin, ignoredSavedInstanceId)) {
            return false;
        }

        return !this.savedData.overlapsAnySavedMachineExcept(
                this.level,
                parts,
                ignoredSavedInstanceId
        );
    }

    /**
     * Registers a newly formed multiblock and writes it to persistent saved data.
     *
     * @param origin world-space origin
     * @param orientation applied orientation
     * @param definition multiblock definition
     * @param parts runtime parts
     * @return formed machine, or {@code null} if registration failed
     */
    public FormedMultiblockMachine register(
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition,
            final List<MultiblockPart> parts
    ) {
        return this.register(
                UUID.randomUUID(),
                origin,
                orientation,
                definition,
                parts,
                true
        );
    }

    /**
     * Registers a formed multiblock with a specific instance id and writes it to
     * persistent saved data.
     *
     * @param instanceId instance id
     * @param origin world-space origin
     * @param orientation applied orientation
     * @param definition multiblock definition
     * @param parts runtime parts
     * @return formed machine, or {@code null} if registration failed
     */
    public FormedMultiblockMachine register(
            final UUID instanceId,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition,
            final List<MultiblockPart> parts
    ) {
        return this.register(
                instanceId,
                origin,
                orientation,
                definition,
                parts,
                true
        );
    }

    private FormedMultiblockMachine register(
            final UUID instanceId,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition,
            final List<MultiblockPart> parts,
            final boolean save
    ) {
        final UUID ignoredSavedInstanceId = save ? null : instanceId;

        if (!this.canRegister(origin, parts, ignoredSavedInstanceId)) {
            return null;
        }

        final CompoundTag savedComponents;

        if (save) {
            savedComponents = new CompoundTag();
        } else {
            final MultiblockSavedData.SavedMachine savedMachine =
                    this.savedData.get(instanceId);

            savedComponents = savedMachine == null
                    ? new CompoundTag()
                    : savedMachine.components();
        }

        final FormedMultiblockMachine machine = new FormedMultiblockMachine(
                instanceId,
                this.level,
                origin,
                orientation,
                definition,
                parts,
                savedComponents
        );

        this.addRuntimeIndexes(machine);

        if (save) {
            this.savedData.put(machine);
        }

        LOGGER.info(
                "Registered formed multiblock {} at {} orientation={} with {} parts; instance={}",
                definition.id(),
                origin,
                orientation,
                parts.size(),
                machine.instanceId()
        );

        MultiblockSyncAddPayload.syncAdded(machine);

        return machine;
    }

    /**
     * Invalidates a machine permanently.
     *
     * @param machine machine to invalidate
     */
    public void invalidate(final FormedMultiblockMachine machine) {
        if (machine == null) {
            return;
        }

        machine.invalidate();

        this.removeRuntimeIndexes(machine);
        MultiblockSyncRemovePayload.syncRemoved(machine);
        MultiblockPlayerSyncTracker.forgetMachine(machine.instanceId());
        this.savedData.remove(machine.instanceId());

        LOGGER.info(
                "Invalidated multiblock {} at {}; instance={}",
                machine.definition().id(),
                machine.origin(),
                machine.instanceId()
        );
    }

    /**
     * Removes a machine from runtime indexes without deleting persistent saved data.
     *
     * @param machine machine to unload from runtime only
     */
    public void unloadRuntimeOnly(final FormedMultiblockMachine machine) {
        if (machine == null) {
            return;
        }

        if (machine.componentsChanged()) {
            this.savedData.updateComponents(machine);
        }

        machine.unloadRuntime();

        this.removeRuntimeIndexes(machine);
        MultiblockSyncRemovePayload.syncRemoved(machine);
        MultiblockPlayerSyncTracker.forgetMachine(machine.instanceId());

        LOGGER.info(
                "Unloaded runtime multiblock {} at {}; instance={}",
                machine.definition().id(),
                machine.origin(),
                machine.instanceId()
        );
    }

    private void addRuntimeIndexes(final FormedMultiblockMachine machine) {
        this.machinesById.put(machine.instanceId(), machine);
        this.machinesByOrigin.put(machine.origin(), machine);

        for (final MultiblockPart part : machine.parts()) {
            this.machinesByPart.put(part.worldPos(), machine);
        }

        for (final ChunkPos chunkPos : machine.touchedChunks()) {
            this.machinesByChunk
                    .computeIfAbsent(chunkPos, ignored -> new HashSet<>())
                    .add(machine);
        }
    }

    private void removeRuntimeIndexes(final FormedMultiblockMachine machine) {
        this.machinesById.remove(machine.instanceId());
        this.machinesByOrigin.remove(machine.origin());

        for (final BlockPos partPos : machine.partPositions()) {
            this.machinesByPart.remove(partPos);
        }

        for (final ChunkPos chunkPos : machine.touchedChunks()) {
            final Set<FormedMultiblockMachine> machines = this.machinesByChunk.get(chunkPos);

            if (machines == null) {
                continue;
            }

            machines.remove(machine);

            if (machines.isEmpty()) {
                this.machinesByChunk.remove(chunkPos);
            }
        }
    }

    /**
     * Marks initial persistent loading as started.
     */
    public void loadPersistentMachines() {
        if (this.loadedPersistentMachines) {
            return;
        }

        this.loadedPersistentMachines = true;
        this.tryRestorePersistentMachines();
    }

    /**
     * Attempts to restore all saved multiblocks whose required chunks are loaded.
     */
    public void tryRestorePersistentMachines() {
        for (final MultiblockSavedData.SavedMachine savedMachine : List.copyOf(this.savedData.machines())) {
            if (this.machinesById.containsKey(savedMachine.instanceId())) {
                continue;
            }

            final MultiblockDefinition definition = MachineLibMultiblocks.getDefinition(savedMachine.definitionId());

            if (definition == null) {
                this.savedData.remove(savedMachine.instanceId());
                continue;
            }

            final List<MultiblockPart> parts = MultiblockRuntimeBuilder.buildParts(
                    this.level,
                    savedMachine.origin(),
                    savedMachine.orientation(),
                    definition
            );

            if (!MultiblockChunkUtil.areAllPartsLoaded(this.level, parts)) {
                continue;
            }

            if (!MultiblockRuntimeBuilder.arePartsValid(this.level, parts)) {
                this.savedData.remove(savedMachine.instanceId());
                continue;
            }

            this.register(
                    savedMachine.instanceId(),
                    savedMachine.origin(),
                    savedMachine.orientation(),
                    definition,
                    parts,
                    false
            );
        }
    }

    /**
     * Gets all currently loaded runtime machines.
     *
     * @return immutable runtime machine collection
     */
    public Collection<FormedMultiblockMachine> formedMachines() {
        return Collections.unmodifiableCollection(this.machinesById.values());
    }

    /**
     * Gets the number of currently loaded runtime machines.
     *
     * @return formed runtime machine count
     */
    public int formedCount() {
        return this.machinesById.size();
    }

    /**
     * Gets the number of indexed loaded part positions.
     *
     * @return indexed runtime part count
     */
    public int indexedPartCount() {
        return this.machinesByPart.size();
    }

    /**
     * Clears runtime indexes only.
     */
    public void clear() {
        this.machinesById.clear();
        this.machinesByOrigin.clear();
        this.machinesByPart.clear();
        this.machinesByChunk.clear();
    }

    /**
     * Ticks runtime components for all currently loaded formed machines.
     *
     * <p>If a component marks itself changed during ticking, this method writes the
     * component data back into persistent saved data.</p>
     */
    public void tickComponents() {
        for (final FormedMultiblockMachine machine : List.copyOf(this.machinesById.values())) {
            machine.tickComponents();

            if (machine.componentsChanged()) {
                this.savedData.updateComponents(machine);
            }
        }
    }
}