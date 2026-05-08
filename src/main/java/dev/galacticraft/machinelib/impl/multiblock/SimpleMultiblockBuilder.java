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
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentFactory;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentFactoryEntry;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenu;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenuSpec;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuFactory;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPartInteractionHandler;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockStandardComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable builder used to construct a simple immutable multiblock definition.
 */
public final class SimpleMultiblockBuilder implements MultiblockBuilder {

    private final ResourceLocation id;
    private final List<FormationRule> rules = new ArrayList<>();
    private final List<MultiblockComponentFactoryEntry<?>> componentFactories =
            new ArrayList<>();

    private MultiblockPattern pattern;
    private MultiblockPartInteractionHandler interactionHandler;
    private MultiblockMenuFactory menuFactory;

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

    /**
     * Adds a runtime component factory to this multiblock definition.
     *
     * @param id stable persistent component id
     * @param type component lookup type
     * @param factory component factory
     * @param <T> component type
     * @return this builder
     */
    @Override
    public <T extends MultiblockComponent> SimpleMultiblockBuilder component(
            final ResourceLocation id,
            final Class<T> type,
            final MultiblockComponentFactory<? extends T> factory
    ) {
        this.componentFactories.add(new MultiblockComponentFactoryEntry<>(
                id,
                type,
                factory
        ));

        return this;
    }

    /**
     * Builds the immutable definition.
     *
     * @return built definition
     */
    @Override
    public SimpleMultiblockDefinition build() {
        if (this.pattern == null) {
            throw new IllegalStateException("Multiblock " + this.id + " has no pattern");
        }

        return new SimpleMultiblockDefinition(
                this.id,
                this.pattern,
                this.rules,
                this.interactionHandler,
                this.menuFactory,
                this.componentFactories
        );
    }

}