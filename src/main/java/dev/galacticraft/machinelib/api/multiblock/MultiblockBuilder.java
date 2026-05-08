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

package dev.galacticraft.machinelib.api.multiblock;

import dev.galacticraft.machinelib.api.multiblock.components.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortRule;
import net.minecraft.resources.ResourceLocation;

/**
 * Mutable builder API for a multiblock definition.
 */
public interface MultiblockBuilder {

    /**
     * Sets the multiblock pattern.
     *
     * @param pattern structure pattern
     * @return this builder
     */
    MultiblockBuilder pattern(MultiblockPattern pattern);

    /**
     * Adds a formation rule.
     *
     * @param rule formation rule
     * @return this builder
     */
    MultiblockBuilder rule(FormationRule rule);

    /**
     * Adds an allowed port rule.
     *
     * <p>Port rules define which faces on which multiblock parts may be
     * configured as item, fluid, energy, or redstone ports.</p>
     *
     * @param rule port rule
     * @return this builder
     */
    MultiblockBuilder portRule(MultiblockPortRule rule);

    /**
     * Adds a default configured port.
     *
     * <p>Default ports are applied when a formed multiblock is created for the
     * first time. They must match one of the registered port rules.</p>
     *
     * @param port default configured port
     * @return this builder
     */
    MultiblockBuilder defaultPort(ConfiguredMultiblockPort port);

    /**
     * Sets the handler called when a player interacts with a formed part.
     *
     * @param handler interaction handler
     * @return this builder
     */
    MultiblockBuilder onUsePart(MultiblockPartInteractionHandler handler);

    /**
     * Sets the menu factory used when a player interacts with a formed part and
     * no custom interaction handler claims the interaction.
     *
     * @param factory menu factory
     * @return this builder
     */
    MultiblockBuilder menu(MultiblockMenuFactory factory);

    /**
     * Uses a default MachineLib-style multiblock menu.
     *
     * <p>This is a convenience method for ordinary MachineLib-style multiblocks.
     * It adds the storage component from the supplied menu spec and installs a
     * standard menu factory that opens the supplied menu type.</p>
     *
     * <p>Use {@link #menu(MultiblockMenuFactory)} directly instead for custom
     * menus, multi-menu multiblocks, headless multiblocks, or dynamic layouts.</p>
     *
     * @param spec default multiblock menu spec
     * @param <Menu> menu type
     * @return this builder
     */
    <Menu extends MultiblockMachineMenu> MultiblockBuilder useDefaultMenu(
            MultiblockMachineMenuSpec<Menu> spec
    );

    /**
     * Adds a runtime component factory to this multiblock definition.
     *
     * @param id stable persistent component id
     * @param type component lookup type
     * @param factory component factory
     * @param <T> component type
     * @return this builder
     */
    <T extends MultiblockComponent> MultiblockBuilder component(
            ResourceLocation id,
            Class<T> type,
            MultiblockComponentFactory<? extends T> factory
    );

    /**
     * Builds the immutable multiblock definition.
     *
     * @return built definition
     */
    MultiblockDefinition build();

}