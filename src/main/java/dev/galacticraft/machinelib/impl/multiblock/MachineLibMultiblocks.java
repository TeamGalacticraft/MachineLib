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
import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import dev.galacticraft.machinelib.api.multiblock.MultiblockRegistry;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortRule;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortType;
import dev.galacticraft.machinelib.impl.multiblock.detection.MultiblockDetectionIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Consumer;

/**
 * Central multiblock registry and runtime entry point for MachineLib.
 *
 * <p>This class is responsible for:</p>
 *
 * <ul>
 *     <li>Registering multiblock definitions.</li>
 *     <li>Compiling optimized detection predicates.</li>
 *     <li>Receiving block change notifications.</li>
 *     <li>Dispatching validation checks.</li>
 *     <li>Managing the multiblock detection index.</li>
 * </ul>
 *
 * <p>The registry becomes immutable after {@link #freeze()} is called.
 * Once frozen, the detection index is compiled and no additional
 * multiblocks may be registered.</p>
 */
public final class MachineLibMultiblocks implements MultiblockRegistry {

    /**
     * Global singleton registry instance.
     */
    public static final MachineLibMultiblocks INSTANCE = new MachineLibMultiblocks();

    /**
     * Registered multiblock definitions by id.
     *
     * <p>A {@link LinkedHashMap} is used to preserve deterministic
     * registration order during compilation.</p>
     */
    private final Map<ResourceLocation, MultiblockDefinition> definitions =
            new LinkedHashMap<>();

    /**
     * Compiled detection index used for extremely fast runtime lookups.
     *
     * <p>The detection index converts multiblock structures into
     * highly optimized local predicate checks.</p>
     */
    private final MultiblockDetectionIndex detectionIndex =
            new MultiblockDetectionIndex();

    /**
     * Whether the registry has been frozen.
     *
     * <p>Once frozen, registration is permanently disabled.</p>
     */
    private boolean frozen = false;

    /**
     * Creates the singleton registry instance.
     */
    private MachineLibMultiblocks() {

    }

    /**
     * Convenience registration helper.
     *
     * <p>This is the primary registration method intended for developers.</p>
     *
     * <pre>{@code
     * MachineLibMultiblocks.register(id, builder -> {
     *     builder.pattern(pattern);
     *     builder.rule(rule);
     * });
     * }</pre>
     *
     * @param id unique multiblock registry id
     * @param consumer builder configuration callback
     */
    public static void register(
            final ResourceLocation id,
            final Consumer<SimpleMultiblockBuilder> consumer
    ) {
        if (INSTANCE.frozen) {
            throw new IllegalStateException("Multiblock registry already frozen");
        }

        final SimpleMultiblockBuilder builder = new SimpleMultiblockBuilder(id);

        consumer.accept(builder);

        INSTANCE.register(builder.build());
    }

    /**
     * Registers a completed multiblock definition.
     *
     * @param definition multiblock definition
     */
    @Override
    public void register(final MultiblockDefinition definition) {
        if (this.frozen) {
            throw new IllegalStateException("Multiblock registry already frozen");
        }

        if (this.definitions.containsKey(definition.id())) {
            throw new IllegalArgumentException("Duplicate multiblock id: " + definition.id());
        }

        this.definitions.put(definition.id(), definition);
    }

    /**
     * Gets a registered multiblock definition by id.
     *
     * @param id multiblock registry id
     * @return registered definition, or null if missing
     */
    @Override
    public MultiblockDefinition get(final ResourceLocation id) {
        return this.definitions.get(id);
    }

    /**
     * Gets a registered multiblock definition from the global MachineLib registry.
     *
     * @param id multiblock registry id
     * @return registered definition, or null if missing
     */
    public static MultiblockDefinition getDefinition(final ResourceLocation id) {
        return INSTANCE.get(id);
    }

    /**
     * {@return immutable collection of all registered multiblock definitions}
     */
    @Override
    public Collection<MultiblockDefinition> definitions() {
        return Collections.unmodifiableCollection(this.definitions.values());
    }

    /**
     * Freezes the registry, registers multiblock port providers, and compiles the
     * detection index.
     *
     * <p>This should only be called once during mod initialization after all
     * multiblocks have been registered. Provider registration happens here because
     * this is the first point where MachineLib has the complete immutable set of
     * registered multiblock definitions.</p>
     */
    public void freeze() {
        if (this.frozen) {
            return;
        }

        MultiblockPortDebug.LOGGER.info(
                "Freezing multiblock registry with {} definitions.",
                this.definitions.size()
        );

        this.frozen = true;

        MultiblockPortProviders.registerItemProviders(
                this.collectPortProviderBlocks(MultiblockPortType.ITEM)
        );

        MultiblockPortProviders.registerFluidProviders(
                this.collectPortProviderBlocks(MultiblockPortType.FLUID)
        );

        MultiblockPortProviders.registerEnergyProviders(
                this.collectPortProviderBlocks(MultiblockPortType.ENERGY)
        );

        MultiblockPortDebug.LOGGER.info("Multiblock port provider registration complete.");

        this.detectionIndex.compile(this.definitions.values());
    }

    /**
     * Collects every exact block predicate used by definitions that expose ports of
     * the requested type.
     *
     * @param type port type to collect provider blocks for
     * @return blocks that may need provider lookup support for the requested port type
     */
    private Set<Block> collectPortProviderBlocks(final MultiblockPortType type) {
        final Set<Block> blocks = new LinkedHashSet<>();

        for (final MultiblockDefinition definition : this.definitions.values()) {
            if (!this.hasPorts(definition, type)) {
                continue;
            }

            this.collectPatternDetectionBlocks(
                    definition,
                    blocks
            );
        }

        return blocks;
    }

    /**
     * Checks whether a definition has any port rule or default port for a specific
     * resource type.
     *
     * @param definition multiblock definition
     * @param type port type to check
     * @return {@code true} if the definition can expose that port type
     */
    private boolean hasPorts(
            final MultiblockDefinition definition,
            final MultiblockPortType type
    ) {
        for (final MultiblockPortRule rule : definition.portRules()) {
            if (rule.types().contains(type)) {
                return true;
            }
        }

        for (final ConfiguredMultiblockPort port : definition.defaultPorts()) {
            if (port.type() == type) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds all exact detection blocks from a definition's pattern into the supplied
     * output set.
     *
     * <p>Exact block predicates return their block through
     * {@link MultiblockSlotPredicate#detectionBlocks()}, while wildcard predicates
     * return an empty array. This means provider registration stays conservative
     * and only registers against known exact blocks.</p>
     *
     * @param definition multiblock definition
     * @param output output block set
     */
    private void collectPatternDetectionBlocks(
            final MultiblockDefinition definition,
            final Set<Block> output
    ) {
        final MultiblockPattern pattern = definition.pattern();

        for (int x = 0; x < pattern.sizeX(); x++) {
            for (int y = 0; y < pattern.sizeY(); y++) {
                for (int z = 0; z < pattern.sizeZ(); z++) {
                    final MultiblockSlotPredicate predicate = pattern.predicateAt(
                            x,
                            y,
                            z
                    );

                    if (predicate == null) {
                        continue;
                    }

                    output.addAll(List.of(predicate.detectionBlocks()));
                }
            }
        }
    }

    /**
     * Called whenever a block changes in the world.
     *
     * <p>This is the main runtime entry point for multiblock detection.</p>
     *
     * <p>The system first checks whether the changed block belongs to an
     * already formed multiblock. If so, the existing machine is validated.</p>
     *
     * <p>If the block is not part of a valid formed multiblock,
     * the detection index is queried to determine whether the block
     * could form a new multiblock.</p>
     *
     * @param level world containing the changed block
     * @param pos changed block position
     * @param state new block state
     */
    public static void onBlockChanged(
            final ServerLevel level,
            final BlockPos pos,
            final BlockState state
    ) {
        final MultiblockManager manager = MultiblockManager.get(level);

        final boolean changedInsideStillValidMachine = manager.handleBlockChanged(pos);

        if (changedInsideStillValidMachine) {
            return;
        }

        INSTANCE.detectionIndex.onBlockChanged(
                level,
                pos,
                state
        );
    }

    /**
     * Processes queued multiblock validations.
     *
     * <p>Queued validation allows MachineLib to spread expensive
     * multiblock checks across multiple ticks.</p>
     *
     * @param limit maximum number of validations to process
     */
    public static void processQueuedValidations(final int limit) {
        INSTANCE.detectionIndex.processQueuedValidations(limit);
    }
}