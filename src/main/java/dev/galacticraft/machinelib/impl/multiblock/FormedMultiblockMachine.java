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
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentFactoryEntry;
import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockPortComponent;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    private final Set<ChunkPos> touchedChunks;
    private final List<MultiblockPartData> partData;
    private final List<MultiblockComponent> components;
    private final Map<Class<? extends MultiblockComponent>, MultiblockComponent> componentsByType;
    private final SimpleMultiblockComponentContext componentContext;
    private final Map<ResourceLocation, MultiblockComponent> componentsById;
    private boolean componentsChanged;

    private MultiblockState state = MultiblockState.FORMED;

    /**
     * Creates a formed multiblock machine with a specific persistent instance id.
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
        this(
                instanceId,
                level,
                origin,
                orientation,
                definition,
                parts,
                new CompoundTag()
        );
    }

    /**
     * Creates a formed multiblock machine with a specific persistent instance id and
     * saved component data.
     *
     * @param instanceId persistent instance id
     * @param level level containing the multiblock
     * @param origin world-space origin
     * @param orientation applied orientation
     * @param definition multiblock definition
     * @param parts runtime part list
     * @param savedComponents saved component data
     */
    public FormedMultiblockMachine(
            final UUID instanceId,
            final ServerLevel level,
            final BlockPos origin,
            final MultiblockOrientation orientation,
            final MultiblockDefinition definition,
            final List<MultiblockPart> parts,
            final CompoundTag savedComponents
    ) {
        this.instanceId = instanceId;
        this.level = level;
        this.origin = origin.immutable();
        this.orientation = orientation;
        this.definition = definition;
        this.parts = List.copyOf(parts);

        final Set<BlockPos> positions = new HashSet<>();
        final Set<ChunkPos> chunks = new HashSet<>();
        final List<MultiblockPartData> data = new ArrayList<>();

        for (final MultiblockPart part : parts) {
            final BlockPos worldPos = part.worldPos().immutable();

            positions.add(worldPos);
            chunks.add(new ChunkPos(worldPos));

            data.add(part.createData(
                    this.instanceId,
                    definition.id(),
                    this.origin,
                    this.orientation
            ));
        }

        this.partPositions = Collections.unmodifiableSet(positions);
        this.touchedChunks = Collections.unmodifiableSet(chunks);
        this.partData = List.copyOf(data);
        this.componentContext = new SimpleMultiblockComponentContext(this);

        final List<MultiblockComponent> createdComponents = new ArrayList<>();
        final Map<Class<? extends MultiblockComponent>, MultiblockComponent> componentTypeMap =
                new HashMap<>();
        final Map<ResourceLocation, MultiblockComponent> componentIdMap =
                new HashMap<>();

        for (final MultiblockComponentFactoryEntry<?> entry : definition.componentFactories()) {
            if (componentTypeMap.containsKey(entry.type())) {
                throw new IllegalStateException(
                        "Duplicate multiblock component type " + entry.type().getName()
                                + " on multiblock " + definition.id()
                );
            }

            if (componentIdMap.containsKey(entry.id())) {
                throw new IllegalStateException(
                        "Duplicate multiblock component id " + entry.id()
                                + " on multiblock " + definition.id()
                );
            }

            final MultiblockComponent component =
                    entry.factory().create(this.componentContext);

            final String savedKey = entry.id().toString();

            if (savedComponents.contains(savedKey, Tag.TAG_COMPOUND)) {
                component.load(
                        this.componentContext,
                        savedComponents.getCompound(savedKey)
                );
            }

            createdComponents.add(component);
            componentTypeMap.put(entry.type(), component);
            componentIdMap.put(entry.id(), component);
        }

        this.components = List.copyOf(createdComponents);
        this.componentsByType = Map.copyOf(componentTypeMap);
        this.componentsById = Map.copyOf(componentIdMap);

        for (final MultiblockComponent component : this.components) {
            component.onFormed(this.componentContext);
        }
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
     * Gets all chunks touched by this formed multiblock.
     *
     * <p>This is used for client synchronization. A player only needs to track
     * one touched chunk to require a client-side formed-part record.</p>
     *
     * @return immutable set of touched chunk positions
     */
    public Set<ChunkPos> touchedChunks() {
        return this.touchedChunks;
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
     * Checks whether this machine touches a chunk.
     *
     * @param chunkPos chunk position
     * @return {@code true} if any part of this machine is in the chunk
     */
    public boolean touchesChunk(final ChunkPos chunkPos) {
        return this.touchedChunks.contains(chunkPos);
    }

    /**
     * Resolves a world-space part position and world-space side into the configured
     * multiblock port on that face.
     *
     * <p>This is the central world-to-pattern port lookup used by transfer
     * providers, debug tools, rendering overlays, and the future 3D port
     * configuration screen.</p>
     *
     * @param worldPos world position of the formed multiblock part
     * @param worldSide world-space side being queried
     * @return configured port on that face, or empty if none exists
     */
    public Optional<ConfiguredMultiblockPort> getConfiguredPortAtWorldSide(
            final BlockPos worldPos,
            final Direction worldSide
    ) {
        final MultiblockPortFace face = this.getPortFaceAtWorldSide(
                worldPos,
                worldSide
        );

        if (face == null) {
            return Optional.empty();
        }

        final MultiblockPortComponent ports =
                this.component(MultiblockPortComponent.class);

        if (ports == null) {
            return Optional.empty();
        }

        return ports.portAt(face);
    }

    /**
     * Resolves a world-space part position and side into the matching
     * pattern-local port face.
     *
     * @param worldPos world position of the formed multiblock part
     * @param worldSide world-space side being queried
     * @return pattern-local port face, or {@code null} if unresolved
     */
    public @Nullable MultiblockPortFace getPortFaceAtWorldSide(
            final BlockPos worldPos,
            final Direction worldSide
    ) {
        final MultiblockPart part = this.partAt(worldPos);

        if (part == null) {
            return null;
        }

        final Direction localSide = this.inverseTransformDirection(worldSide);

        if (localSide == null) {
            return null;
        }

        return new MultiblockPortFace(
                part.originalRelativePos(),
                localSide
        );
    }

    /**
     * Finds the formed multiblock part at a world position.
     *
     * @param worldPos world position
     * @return matching part, or {@code null}
     */
    public @Nullable MultiblockPart partAt(final BlockPos worldPos) {
        for (final MultiblockPart part : this.parts()) {
            if (part.worldPos().equals(worldPos)) {
                return part;
            }
        }

        return null;
    }

    /**
     * Converts a world-space direction back into the pattern-local direction that
     * produced it for this formed multiblock's orientation.
     *
     * @param worldSide world-space side
     * @return local pattern side, or {@code null}
     */
    private @Nullable Direction inverseTransformDirection(final Direction worldSide) {
        for (final Direction localSide : Direction.values()) {
            if (this.orientation().transformDirection(localSide) == worldSide) {
                return localSide;
            }
        }

        return null;
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
     * Gets all runtime components attached to this formed machine.
     *
     * @return immutable component list
     */
    public List<MultiblockComponent> components() {
        return this.components;
    }

    /**
     * Gets a runtime component by type.
     *
     * @param type component type
     * @return component, or {@code null}
     * @param <T> component type
     */
    public <T extends MultiblockComponent> T component(final Class<T> type) {
        return type.cast(this.componentsByType.get(type));
    }

    /**
     * Ticks all runtime components attached to this formed machine.
     */
    public void tickComponents() {
        if (this.state != MultiblockState.FORMED) {
            return;
        }

        for (final MultiblockComponent component : this.components) {
            component.tick(this.componentContext);
        }
    }

    /**
     * Marks this machine's persistent component data as changed.
     */
    public void setComponentsChanged() {
        this.componentsChanged = true;
    }

    /**
     * Checks whether this machine's persistent component data has changed.
     *
     * @return {@code true} if component data should be written to saved data
     */
    public boolean componentsChanged() {
        return this.componentsChanged;
    }

    /**
     * Clears this machine's component dirty flag after saving.
     */
    public void clearComponentsChanged() {
        this.componentsChanged = false;
    }

    /**
     * Saves all persistent component data into one compound.
     *
     * @return saved component data
     */
    public CompoundTag saveComponents() {
        final CompoundTag componentsTag = new CompoundTag();

        for (final MultiblockComponentFactoryEntry<?> entry : this.definition.componentFactories()) {
            final MultiblockComponent component = this.componentsById.get(entry.id());

            if (component == null) {
                continue;
            }

            final CompoundTag componentTag = new CompoundTag();

            component.save(
                    this.componentContext,
                    componentTag
            );

            componentsTag.put(
                    entry.id().toString(),
                    componentTag
            );
        }

        return componentsTag;
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
     * Marks this runtime machine as permanently invalidated and notifies all
     * attached components.
     */
    public void invalidate() {
        if (this.state == MultiblockState.INVALIDATED) {
            return;
        }

        this.state = MultiblockState.INVALIDATED;

        for (final MultiblockComponent component : this.components) {
            component.onInvalidated(this.componentContext);
        }
    }

    /**
     * Notifies attached components that this runtime machine is being unloaded
     * without deleting persistent saved data.
     */
    public void unloadRuntime() {
        for (final MultiblockComponent component : this.components) {
            component.onRuntimeUnloaded(this.componentContext);
        }
    }
}