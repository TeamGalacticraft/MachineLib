/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.api.machine;

import com.mojang.serialization.Lifecycle;
import dev.galacticraft.impl.machine.Constant;
import dev.galacticraft.impl.machine.MachineStatusImpl;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the status of a machine.
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public interface MachineStatus {
    /**
     * Registry for machine statuses.
     * All machine statuses should be registered in this registry to be used.
     */
    Registry<MachineStatus> REGISTRY = FabricRegistryBuilder.from(new DefaultedRegistry<>("machinelib:invalid", RegistryKey.<MachineStatus>ofRegistry(new Identifier(Constant.MOD_ID, "machine_status")), Lifecycle.stable(), null)).buildAndRegister();
    /**
     * Default machine status.
     */
    MachineStatus INVALID = createAndRegister(new Identifier(Constant.MOD_ID, "invalid"), new TranslatableText("ui.machinelib.machine_status.invalid"), Type.OTHER);

    /**
     * Creates a new machine status and registers it in the registry.
     * @param id The ID of the machine status.
     * @param name The name of the machine status.
     * @param type The type of the machine status.
     * @return The newly created machine status.
     */
    static MachineStatus createAndRegister(@NotNull Identifier id, @NotNull Text name, @NotNull MachineStatus.Type type) {
        return Registry.register(REGISTRY, id, create(name, type));
    }

    /**
     * Creates a new machine status.
     * @param name The name of the machine status.
     * @param type The type of the machine status.
     * @return The newly created machine status.
     */
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull MachineStatus create(@NotNull Text name, @NotNull MachineStatus.Type type) {
        return new MachineStatusImpl(name, type);
    }

    /**
     * Returns the name of the machine status.
     * @return The name of the machine status.
     */
    @NotNull Text getName();

    /**
     * Returns the type of the machine status.
     * @return The type of the machine status.
     */
    @NotNull MachineStatus.Type getType();

    /**
     * Represents the types of machine statuses.
     */
    enum Type {
        /**
         * The machine is active
         */
        WORKING(true),
        /**
         * The machine is active, but at reduced efficiency.
         */
        PARTIALLY_WORKING(true),
        /**
         * The machine is missing a resource it needs to function.
         * Should not be an item, fluid or energy.
         *
         * @see #MISSING_ENERGY
         * @see #MISSING_FLUIDS
         * @see #MISSING_ITEMS
         */
        MISSING_RESOURCE(false),
        /**
         * The machine is missing a fluid it needs to function.
         * Should be preferred over {@link #MISSING_RESOURCE}
         */
        MISSING_FLUIDS(false),
        /**
         * The machine does not have the amount of energy needed to function.
         * Should be preferred over {@link #MISSING_RESOURCE}
         */
        MISSING_ENERGY(false),
        /**
         * The machine does not have the items needed to function.
         * Should be preferred over {@link #MISSING_RESOURCE}
         */
        MISSING_ITEMS(false),
        /**
         * The machine's output is blocked/full.
         */
        OUTPUT_FULL(false),
        /**
         * Everything else
         */
        OTHER(false);

        private final boolean active;

        Type(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return this.active;
        }
    }
}
