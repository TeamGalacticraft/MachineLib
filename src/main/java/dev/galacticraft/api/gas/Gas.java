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
import dev.galacticraft.impl.gas.GasVariantImpl;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Gas {
    public static final Registry<Gas> REGISTRY = FabricRegistryBuilder.from(new DefaultedRegistry<>("galacticraft-api:empty", RegistryKey.ofRegistry(new Identifier("galacticraft-api", "gas")), Lifecycle.stable(), Gas::getReference)).buildAndRegister();
    public static final Gas EMPTY = new Gas(new TranslatableText("gas.galacticraft-api.empty"), new Identifier("minecraft", "empty"), "");

    private final TranslatableText name;
    private final @Nullable Identifier fluid;
    private final String symbol;
    private final String displaySymbol;
    private final GasVariant variant = new GasVariantImpl(this, null);
    private final RegistryEntry.Reference<Gas> reference = REGISTRY.createEntry(this);

    public Gas(TranslatableText name, @Nullable Identifier fluid, String symbol) {
        this.name = name;
        this.fluid = fluid;
        this.symbol = symbol;
        this.displaySymbol = this.symbol
                .replaceAll("0", "₀")
                .replaceAll("1", "₁")
                .replaceAll("2", "₂")
                .replaceAll("3", "₃")
                .replaceAll("4", "₄")
                .replaceAll("5", "₅")
                .replaceAll("6", "₆")
                .replaceAll("7", "₇")
                .replaceAll("8", "₈")
                .replaceAll("9", "₉");
    }

    public static int getRawId(Gas gas) {
        return REGISTRY.getRawId(gas);
    }

    public static Gas byRawId(int id) {
        return REGISTRY.get(id);
    }

    public Text getName() {
        return this.name;
    }

    public String getTranslationKey() {
        return this.name.getKey();
    }

    public String getSymbol() {
        return symbol;
    }

    public String symbolForDisplay() {
        return this.displaySymbol;
    }

    public @NotNull Fluid getFluid() {
        return Registry.FLUID.get(this.fluid);
    }

    public RegistryEntry.Reference<Gas> getReference() {
        return reference;
    }

    @ApiStatus.Internal
    public GasVariant _getVariant() {
        return variant;
    }
}
