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

import dev.galacticraft.impl.gas.GasStack;
import dev.galacticraft.impl.gas.GasVariantImpl;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface GasVariant extends TransferVariant<Gas> {
    /**
     * Returns the default blank gas variant.
     * @return The default blank gas variant.
     */
    static GasVariant blank() {
        return of(Gas.EMPTY);
    }

    /**
     * Returns a new gas variant of the given gas with no nbt.
     * @param gas The gas to create a variant of.
     * @return A new gas variant of the given gas with no nbt.
     */
    static GasVariant of(@NotNull Gas gas) {
        return of(gas, null);
    }

    /**
     * Returns a new gas variant of the given gas with the stack's nbt.
     * @param stack The gas stack to create a variant of.
     * @return A new gas variant of the given gas with the stack's nbt.
     */
    static GasVariant of(@NotNull GasStack stack) {
        return of(stack.getGas(), stack.getNbt());
    }

    /**
     * Returns a new gas variant of the given gas with the given nbt.
     * @param gas The gas to create a variant of.
     * @param nbt The nbt to create a variant of.
     * @return A new gas variant of the given gas with the given nbt.
     */
    static GasVariant of(@NotNull Gas gas, @Nullable NbtCompound nbt) {
        return GasVariantImpl.of(gas, nbt);
    }

    /**
     * Returns the backing gas of this variant.
     * @return The backing gas of this variant.
     */
    default Gas getGas() {
        return getObject();
    }

    /**
     * Returns a new gas variant from the given nbt.
     * @param nbt The nbt deserialize a variant from.
     * @return A new gas variant from the given nbt.
     */
    static GasVariant readNbt(NbtCompound nbt) {
        return GasVariantImpl.readNbt(nbt);
    }

    /**
     * Returns a new gas variant from the given packet.
     * @param buf The packet to deserialize a variant from.
     * @return A new gas variant from the given packet.
     */
    static GasVariant fromPacket(PacketByteBuf buf) {
        return GasVariantImpl.fromPacket(buf);
    }

    /**
     * Converts this variant to a gas stack.
     * @param amount The amount of gas to create the stack with.
     * @return A gas stack with the same gas and nbt as this variant.
     */
    default GasStack toStack(long amount) {
        if (this.isBlank() || amount == 0) return GasStack.EMPTY;
        return new GasStack(this, amount);
    }
}
