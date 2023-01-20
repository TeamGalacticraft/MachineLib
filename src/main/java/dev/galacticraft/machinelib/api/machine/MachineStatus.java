/*
 * Copyright (c) 2021-2023 Team Galacticraft
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

package dev.galacticraft.machinelib.api.machine;

import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.MachineLib;
import dev.galacticraft.machinelib.impl.machine.MachineStatusImpl;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the status of a machine.
 */
public interface MachineStatus {
    /**
     * Default machine status.
     */
    MachineStatus INVALID = createAndRegister(Constant.id("invalid"), Component.translatable(Constant.TranslationKey.STATUS_INVALID), Type.OTHER);

    /**
     * Creates a new machine status and registers it in the registry.
     *
     * @param id   The ID of the machine status.
     * @param name The name of the machine status.
     * @param type The type of the machine status.
     * @return The newly created machine status.
     */
    static @NotNull MachineStatus createAndRegister(@NotNull ResourceLocation id, @NotNull Component name, @NotNull MachineStatus.Type type) {
        return Registry.register(MachineLib.MACHINE_STATUS_REGISTRY, id, create(name, type));
    }

    /**
     * Creates a new machine status.
     *
     * @param name The name of the machine status.
     * @param type The type of the machine status.
     * @return The newly created machine status.
     */
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull MachineStatus create(@NotNull Component name, @NotNull MachineStatus.Type type) {
        return new MachineStatusImpl(name, type);
    }

    /**
     * Returns the name of the machine status.
     *
     * @return The name of the machine status.
     */
    @NotNull Component name();

    /**
     * Returns the type of the machine status.
     *
     * @return The type of the machine status.
     */
    @NotNull MachineStatus.Type type();

    /**
     * Represents the types of machine statuses.
     */
    enum Type {
        /**
         * The machine is active and generating resources.
         */
        WORKING(true),
        /**
         * The machine is active at reduced efficiency.
         */
        PARTIALLY_WORKING(true),
        /**
         * The machine is missing a resource it needs to function.
         * Prefer more specific static types over this one.
         *
         * @see #MISSING_ENERGY
         * @see #MISSING_FLUIDS
         * @see #MISSING_ITEMS
         */
        MISSING_RESOURCE(false),
        /**
         * The machine is missing a fluid it needs to function.
         */
        MISSING_FLUIDS(false),
        /**
         * The machine does not have the amount of energy needed to function.
         */
        MISSING_ENERGY(false),
        /**
         * The machine does not have the items needed to function.
         */
        MISSING_ITEMS(false),
        /**
         * The machine's output is blocked/full.
         */
        OUTPUT_FULL(false),
        /**
         * All other problems.
         */
        OTHER(false);

        private final boolean active;

        @Contract(pure = true)
        Type(boolean active) {
            this.active = active;
        }

        /**
         * Returns whether the machine should be considered to be generating resources.
         *
         * @return whether the machine should be considered to be generating resources.
         */
        @Contract(pure = true)
        public boolean isActive() {
            return this.active;
        }
    }
}
