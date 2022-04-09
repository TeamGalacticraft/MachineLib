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

package dev.galacticraft.impl.gas;

import dev.galacticraft.api.gas.Gas;
import dev.galacticraft.api.gas.GasVariant;
import dev.galacticraft.api.gas.Gases;
import net.minecraft.fluid.Fluid;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GasImpl implements Gas {
    private final TranslatableText name;
    private final @Nullable Identifier fluid;
    private final String symbol;
    private final String displaySymbol;
    private final GasVariant variant = new GasVariantImpl(this, null);
    private final RegistryEntry.Reference<Gas> reference = REGISTRY.createEntry(this);

    static {
        Gases.init();
    }

    public GasImpl(TranslatableText name, @Nullable Identifier fluid, String symbol, String displaySymbol) {
        this.name = name;
        this.fluid = fluid;
        this.symbol = symbol;
        this.displaySymbol = displaySymbol;
    }

    @Override
    public Text getName() {
        return this.name;
    }

    @Override
    public String getTranslationKey() {
        return this.name.getKey();
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public String symbolForDisplay() {
        return this.displaySymbol;
    }

    @Override
    public @NotNull Optional<Fluid> getFluid() {
        return Registry.FLUID.getOrEmpty(this.fluid);
    }

    @Override
    @ApiStatus.Internal
    public @NotNull RegistryEntry.Reference<Gas> getReference() {
        return this.reference;
    }

    @Override
    @ApiStatus.Internal
    public GasVariant _getVariant() {
        return variant;
    }
}
