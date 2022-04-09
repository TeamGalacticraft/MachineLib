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

package dev.galacticraft.api.gas;

import com.mojang.serialization.Lifecycle;
import dev.galacticraft.impl.gas.GasImpl;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.fluid.Fluid;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface Gas {
    /**
     * The {@link RegistryKey} of the {@link Gas} registry.
     * @see #REGISTRY
     */
    RegistryKey<Registry<Gas>> REGISTRY_KEY = RegistryKey.ofRegistry(new Identifier("machinelib", "gas"));
    /**
     * The gas registry. All gases must be registered here, otherwise the game will crash.
     * @see #REGISTRY_KEY
     */
    Registry<Gas> REGISTRY = FabricRegistryBuilder.from(new DefaultedRegistry<>("machinelib:empty", REGISTRY_KEY, Lifecycle.stable(), Gas::getReference)).buildAndRegister();
    /**
     * The default gas.
     * Technically, this isn't a gas, but it's used to represent the lack of a gas.
     */
    Gas EMPTY = Registry.register(REGISTRY, new Identifier("machinelib:empty"), new GasImpl(new TranslatableText("gas.machinelib.empty"), new Identifier("minecraft", "empty"), "", ""));

    /**
     * Creates a new gas.
     * @param name The name of the gas
     * @param fluid The fluid associated with the gas
     * @param symbol The symbol of the gas
     * @return The newly created gas
     */
    @Contract("_, _, _ -> new")
    static @NotNull Gas create(TranslatableText name, @Nullable Identifier fluid, String symbol) {
        return new GasImpl(name, fluid, symbol, symbol
                .replaceAll("0", "₀")
                .replaceAll("1", "₁")
                .replaceAll("2", "₂")
                .replaceAll("3", "₃")
                .replaceAll("4", "₄")
                .replaceAll("5", "₅")
                .replaceAll("6", "₆")
                .replaceAll("7", "₇")
                .replaceAll("8", "₈")
                .replaceAll("9", "₉"));
    }

    /**
     * Returns the raw integer id of the provided gas.
     * @param gas The gas to get the id of
     * @return The id of the gas
     */
    static int getRawId(Gas gas) {
        return REGISTRY.getRawId(gas);
    }

    /**
     * Returns the gas with the provided id.
     * @param id The raw id of the gas
     * @return The gas with the provided id
     */
    static Gas byRawId(int id) {
        return REGISTRY.get(id);
    }

    /**
     * Returns the name of this gas.
     * @return The name of this gas
     */
    Text getName();

    /**
     * Returns the translation key of this gas' name.
     * @return The translation key of this gas' name
     */
    String getTranslationKey();

    /**
     * Returns the symbol of this gas.
     * @return The symbol of this gas
     */
    String getSymbol();

    /**
     * Returns the symbol of this gas, with the appropriate superscripts.
     * @return The symbol of this gas, with the appropriate superscripts
     */
    String symbolForDisplay();

    /**
     * Returns the fluid associated with this gas.
     * @return The fluid associated with this gas
     */
    @NotNull Optional<Fluid> getFluid();

    /**
     * Returns the registry reference of this gas.
     * Ensures that there are no unregistered gases in the game.
     * @return The registry reference of this gas
     */
    @ApiStatus.Internal
    @NotNull RegistryEntry.Reference<Gas> getReference();

    /**
     * Returns the gas variant associated with this gas.
     * This is an internal method and should not be used by other mods.
     * @return The gas variant associated with this gas
     * @see GasVariant#of(Gas)
     */
    @ApiStatus.Internal
    GasVariant _getVariant();
}
