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

import dev.galacticraft.machinelib.api.multiblock.FormationRule;
import dev.galacticraft.machinelib.api.multiblock.MultiblockBuilder;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentFactory;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentFactoryEntry;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenu;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenuSpec;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuFactory;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionHandler;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockStandardComponents;
import dev.galacticraft.machinelib.api.multiblock.port.*;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.MultiblockPortConflictRule;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.MultiblockPortConflictRuleAssignment;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.MultiblockPortConflictScope;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mutable builder used to construct a simple immutable multiblock definition.
 */
public final class SimpleMultiblockBuilder implements MultiblockBuilder {

    private final ResourceLocation id;
    private final List<FormationRule> rules = new ArrayList<>();
    private final List<MultiblockComponentFactoryEntry<?>> componentFactories =
            new ArrayList<>();
    private final List<MultiblockPortRule> portRules = new ArrayList<>();
    private final List<ConfiguredMultiblockPort> defaultPorts = new ArrayList<>();
    private final List<AllFacePortRuleTemplate> allFacePortRules = new ArrayList<>();
    private final List<AllFaceDefaultPortTemplate> allFaceDefaultPorts = new ArrayList<>();

    private MultiblockPattern pattern;
    private MultiblockPartInteractionHandler interactionHandler;
    private MultiblockMenuFactory menuFactory;

    private final List<MultiblockPortConflictRuleAssignment> portConflictRules = new ArrayList<>();

    /**
     * Creates a builder for a multiblock id.
     *
     * @param id unique multiblock id
     */
    public SimpleMultiblockBuilder(final ResourceLocation id) {
        this.id = id;
    }

    /**
     * Sets the structure pattern for the multiblock.
     *
     * @param pattern structure pattern
     * @return this builder
     */
    @Override
    public SimpleMultiblockBuilder pattern(final MultiblockPattern pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Adds a formation rule.
     *
     * @param rule formation rule
     * @return this builder
     */
    @Override
    public SimpleMultiblockBuilder rule(final FormationRule rule) {
        this.rules.add(rule);
        return this;
    }

    /**
     * Adds an allowed port rule.
     *
     * @param rule port rule
     * @return this builder
     */
    @Override
    public SimpleMultiblockBuilder portRule(final MultiblockPortRule rule) {
        this.portRules.add(rule);
        return this;
    }

    /**
     * Adds a default configured port.
     *
     * @param port default configured port
     * @return this builder
     */
    @Override
    public SimpleMultiblockBuilder defaultPort(final ConfiguredMultiblockPort port) {
        this.defaultPorts.add(port);
        return this;
    }

    @Override
    public SimpleMultiblockBuilder portRulesForAllExposedFaces(
            final BlockPos relativePos,
            final Set<MultiblockPortType> types,
            final Set<MultiblockPortMode> modes,
            final Set<MultiblockPortTarget> targets
    ) {
        this.allFacePortRules.add(new AllFacePortRuleTemplate(
                relativePos.immutable(),
                Set.copyOf(types),
                Set.copyOf(modes),
                Set.copyOf(targets)
        ));

        return this;
    }

    @Override
    public SimpleMultiblockBuilder defaultPortsForAllExposedFaces(
            final BlockPos relativePos,
            final MultiblockPortType type,
            final MultiblockPortMode mode,
            final MultiblockPortTarget target
    ) {
        this.allFaceDefaultPorts.add(new AllFaceDefaultPortTemplate(
                relativePos.immutable(),
                type,
                mode,
                target
        ));

        return this;
    }

    /**
     * Sets the handler called when a player interacts with a formed part.
     *
     * <p>If this handler returns {@link InteractionResult#PASS}, the definition
     * can still fall back to opening its configured menu.</p>
     *
     * @param interactionHandler interaction handler
     * @return this builder
     */
    @Override
    public SimpleMultiblockBuilder onUsePart(final MultiblockPartInteractionHandler interactionHandler) {
        this.interactionHandler = interactionHandler;
        return this;
    }

    /**
     * Sets the menu factory used by this multiblock.
     *
     * @param menuFactory menu factory
     * @return this builder
     */
    @Override
    public SimpleMultiblockBuilder menu(final MultiblockMenuFactory menuFactory) {
        this.menuFactory = menuFactory;
        return this;
    }

    /**
     * Uses a default MachineLib-style multiblock menu.
     *
     * <p>This installs the storage component defined by the supplied menu spec
     * and registers a default menu factory that opens the menu for the formed
     * multiblock machine.</p>
     *
     * @param spec default menu spec
     * @param <Menu> menu type
     * @return this builder
     */
    @Override
    public <Menu extends MultiblockMachineMenu> SimpleMultiblockBuilder useDefaultMenu(
            final MultiblockMachineMenuSpec<Menu> spec
    ) {
        MultiblockStandardComponents.storage(
                this,
                spec.storage()
        );

        this.menu(new MultiblockMenuFactory() {
            @Override
            public AbstractContainerMenu createMenu(
                    final MultiblockMenuContext context,
                    final int syncId,
                    final Inventory inventory,
                    final Player player
            ) {
                final FormedMultiblockMachine machine =
                        MultiblockManager.get(context.level()).getById(context.instanceId());

                if (machine == null || !(player instanceof ServerPlayer serverPlayer)) {
                    return null;
                }

                return spec.createServerMenu(
                        syncId,
                        serverPlayer,
                        machine,
                        context.clickedPos()
                );
            }

            @Override
            public Component getDisplayName(final MultiblockMenuContext context) {
                return spec.displayName();
            }
        });

        return this;
    }

    @Override
    public <T extends MultiblockComponent> SimpleMultiblockBuilder component(
            final ResourceLocation id,
            final Class<T> type,
            final MultiblockComponentFactory<? extends T> factory
    ) {
        if (this.hasComponent(id)) {
            throw new IllegalStateException(
                    "Multiblock " + this.id + " already has component " + id
            );
        }

        this.componentFactories.add(new MultiblockComponentFactoryEntry<>(
                id,
                type,
                factory
        ));

        return this;
    }

    /**
     * Builds the immutable multiblock definition.
     *
     * <p>This validates the structure, collects externally exposed faces, validates
     * port rules/default ports, automatically installs the standard port component
     * when needed, and finally creates the immutable definition.</p>
     *
     * @return built definition
     */
    @Override
    public SimpleMultiblockDefinition build() {
        if (this.pattern == null) {
            throw new IllegalStateException("Multiblock " + this.id + " has no pattern");
        }

        final Set<MultiblockPortFace> exposedFaces = this.collectExposedFaces();

        this.expandAllFacePortRules(exposedFaces);
        this.expandAllFaceDefaultPorts(exposedFaces);

        this.validatePortRules(exposedFaces);
        this.validateDefaultPorts(exposedFaces);

        if (this.needsPortComponent() && !this.hasComponent(MultiblockStandardComponents.PORTS)) {
            MultiblockStandardComponents.ports(this);
        }

        return new SimpleMultiblockDefinition(
                this.id,
                this.pattern,
                this.rules,
                this.interactionHandler,
                this.menuFactory,
                this.componentFactories,
                this.portRules,
                this.defaultPorts,
                exposedFaces,
                this.portConflictRules
        );
    }

    /**
     * Expands all pending all-face port rule templates into concrete port rules.
     *
     * @param exposedFaces precomputed exposed face cache
     */
    private void expandAllFacePortRules(final Set<MultiblockPortFace> exposedFaces) {
        for (final AllFacePortRuleTemplate template : this.allFacePortRules) {
            for (final MultiblockPortFace face : exposedFaces) {
                if (!face.relativePos().equals(template.relativePos())) {
                    continue;
                }

                this.portRules.add(new MultiblockPortRule(
                        face,
                        template.types(),
                        template.modes(),
                        template.targets()
                ));
            }
        }
    }

    /**
     * Expands all pending all-face default port templates into concrete default
     * ports.
     *
     * @param exposedFaces precomputed exposed face cache
     */
    private void expandAllFaceDefaultPorts(final Set<MultiblockPortFace> exposedFaces) {
        for (final AllFaceDefaultPortTemplate template : this.allFaceDefaultPorts) {
            for (final MultiblockPortFace face : exposedFaces) {
                if (!face.relativePos().equals(template.relativePos())) {
                    continue;
                }

                this.defaultPorts.add(new ConfiguredMultiblockPort(
                        face,
                        template.type(),
                        template.mode(),
                        template.target()
                ));
            }
        }
    }

    /**
     * Adds a scoped port conflict rule to this multiblock.
     *
     * <p>Conflict rules are evaluated only in the port configuration UI. They allow
     * a multiblock definition to mark otherwise rule-allowed port options as
     * conflicting or disabled. Rules may be scoped to the entire multiblock, one
     * port type, one pattern-relative block, or one exact face.</p>
     *
     * @param scope scope where the rule applies
     * @param rule conflict rule
     * @return this builder
     */
    public SimpleMultiblockBuilder portConflictRule(
            final MultiblockPortConflictScope scope,
            final MultiblockPortConflictRule rule
    ) {
        this.portConflictRules.add(new MultiblockPortConflictRuleAssignment(
                scope,
                rule
        ));

        return this;
    }

    /**
     * Collects every externally exposed face in the current pattern.
     *
     * <p>A face is exposed when the source position is occupied by a multiblock
     * part and the adjacent position is either outside the pattern bounds or not
     * occupied by another multiblock part.</p>
     *
     * @return mutable set of exposed faces
     */
    private Set<MultiblockPortFace> collectExposedFaces() {
        final Set<MultiblockPortFace> exposedFaces = new HashSet<>();

        for (int x = 0; x < this.pattern.sizeX(); x++) {
            for (int y = 0; y < this.pattern.sizeY(); y++) {
                for (int z = 0; z < this.pattern.sizeZ(); z++) {
                    if (!this.hasPartAt(x, y, z)) {
                        continue;
                    }

                    final BlockPos relativePos = new BlockPos(x, y, z);

                    for (final Direction face : Direction.values()) {
                        final BlockPos adjacent = relativePos.relative(face);

                        if (!this.isInsidePattern(adjacent) || !this.hasPartAt(adjacent)) {
                            exposedFaces.add(new MultiblockPortFace(relativePos, face));
                        }
                    }
                }
            }
        }

        return exposedFaces;
    }

    /**
     * Validates that every registered port rule is placed on a real exposed
     * multiblock face.
     *
     * @param exposedFaces precomputed exposed face cache
     */
    private void validatePortRules(final Set<MultiblockPortFace> exposedFaces) {
        for (final MultiblockPortRule rule : this.portRules) {
            this.validateFaceExists(rule.face());

            if (!exposedFaces.contains(rule.face())) {
                throw new IllegalStateException(
                        "Multiblock " + this.id
                                + " has port rule on non-exposed face "
                                + rule.face()
                );
            }
        }
    }

    /**
     * Validates every default configured port.
     *
     * <p>Each default port must be placed on an existing exposed face and must be
     * allowed by at least one registered port rule.</p>
     *
     * @param exposedFaces precomputed exposed face cache
     */
    private void validateDefaultPorts(final Set<MultiblockPortFace> exposedFaces) {
        for (final ConfiguredMultiblockPort port : this.defaultPorts) {
            this.validateFaceExists(port.face());

            if (!exposedFaces.contains(port.face())) {
                throw new IllegalStateException(
                        "Multiblock " + this.id
                                + " has default port on non-exposed face "
                                + port.face()
                );
            }

            if (!this.isDefaultPortAllowed(port)) {
                throw new IllegalStateException(
                        "Multiblock " + this.id
                                + " has default port that does not match any port rule: "
                                + port
                );
            }
        }
    }

    /**
     * Checks whether a default port is allowed by any registered port rule.
     *
     * @param port configured default port
     * @return {@code true} if at least one rule allows the port
     */
    private boolean isDefaultPortAllowed(final ConfiguredMultiblockPort port) {
        for (final MultiblockPortRule rule : this.portRules) {
            if (rule.allows(port)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validates that a port face points at an occupied pattern position.
     *
     * @param face port face to validate
     */
    private void validateFaceExists(final MultiblockPortFace face) {
        final BlockPos relativePos = face.relativePos();

        if (!this.isInsidePattern(relativePos)) {
            throw new IllegalStateException(
                    "Multiblock " + this.id
                            + " has port face outside pattern bounds: "
                            + face
            );
        }

        if (!this.hasPartAt(relativePos)) {
            throw new IllegalStateException(
                    "Multiblock " + this.id
                            + " has port face on empty/null pattern slot: "
                            + face
            );
        }
    }

    /**
     * Checks whether the multiblock definition needs the standard port component.
     *
     * @return {@code true} if rules or default ports have been registered
     */
    private boolean needsPortComponent() {
        return !this.portRules.isEmpty() || !this.defaultPorts.isEmpty();
    }

    /**
     * Checks whether a component factory with the supplied id has already been
     * registered on this builder.
     *
     * @param id component id
     * @return {@code true} if the component exists
     */
    private boolean hasComponent(final ResourceLocation id) {
        for (final MultiblockComponentFactoryEntry<?> entry : this.componentFactories) {
            if (entry.id().equals(id)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a relative position is inside the current pattern bounds.
     *
     * @param pos relative position
     * @return {@code true} if the position is inside the pattern
     */
    private boolean isInsidePattern(final BlockPos pos) {
        return pos.getX() >= 0
                && pos.getY() >= 0
                && pos.getZ() >= 0
                && pos.getX() < this.pattern.sizeX()
                && pos.getY() < this.pattern.sizeY()
                && pos.getZ() < this.pattern.sizeZ();
    }

    /**
     * Checks whether the supplied relative position contains a multiblock part.
     *
     * @param pos relative position
     * @return {@code true} if the position has a non-null slot predicate
     */
    private boolean hasPartAt(final BlockPos pos) {
        return this.hasPartAt(
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
    }

    /**
     * Checks whether the supplied pattern coordinates contain a multiblock part.
     *
     * @param x pattern x coordinate
     * @param y pattern y coordinate
     * @param z pattern z coordinate
     * @return {@code true} if the slot has a non-null predicate
     */
    private boolean hasPartAt(
            final int x,
            final int y,
            final int z
    ) {
        return this.pattern.predicateAt(x, y, z) != null;
    }

    private record AllFacePortRuleTemplate(
            BlockPos relativePos,
            Set<MultiblockPortType> types,
            Set<MultiblockPortMode> modes,
            Set<MultiblockPortTarget> targets
    ) {

    }

    private record AllFaceDefaultPortTemplate(
            BlockPos relativePos,
            MultiblockPortType type,
            MultiblockPortMode mode,
            MultiblockPortTarget target
    ) {

    }
}