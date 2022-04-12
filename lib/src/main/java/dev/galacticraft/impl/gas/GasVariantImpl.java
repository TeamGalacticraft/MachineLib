/*
 * Copyright (c) 2021-${year} ${company}
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Internal
public class GasVariantImpl implements GasVariant {
	public static GasVariant of(@NotNull Gas gas, @Nullable NbtCompound nbt) {
		Objects.requireNonNull(gas, "Gas may not be null");

		if (nbt == null) {
			return gas._getVariant();
		} else {
			return new GasVariantImpl(gas, nbt);
		}
	}

	private final @NotNull Gas gas;
	private final @Nullable NbtCompound nbt;
	private final int hashCode;

	public GasVariantImpl(@NotNull Gas gas, @Nullable NbtCompound nbt) {
		this.gas = gas;
		this.nbt = nbt == null ? null : nbt.copy(); // defensive copy
		hashCode = Objects.hash(gas, nbt);
	}

	@Override
	public @NotNull Gas getObject() {
		return gas;
	}

	@Nullable
	@Override
	public NbtCompound getNbt() {
		return nbt;
	}

	@Override
	public boolean isBlank() {
		return gas == Gas.EMPTY;
	}

	@Override
	public NbtCompound toNbt() {
		NbtCompound result = new NbtCompound();
		result.putString("gas", Gas.REGISTRY.getId(gas).toString());

		if (nbt != null) {
			result.put("tag", nbt.copy());
		}

		return result;
	}

	public static GasVariant readNbt(NbtCompound nbt) {
		try {
			Gas gas = Gas.REGISTRY.get(new Identifier(nbt.getString("gas")));
			NbtCompound aTag = nbt.contains("tag") ? nbt.getCompound("tag") : null;
			return of(gas, aTag);
		} catch (RuntimeException runtimeException) {
			return GasVariant.blank();
		}
	}

	@Override
	public void toPacket(PacketByteBuf buf) {
		if (isBlank()) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			buf.writeVarInt(Gas.getRawId(gas));
			buf.writeNbt(nbt);
		}
	}

	public static GasVariant fromPacket(PacketByteBuf buf) {
		if (!buf.readBoolean()) {
			return GasVariant.blank();
		} else {
			Gas gas = Gas.byRawId(buf.readVarInt());
			NbtCompound nbt = buf.readNbt();
			return of(gas, nbt);
		}
	}

	@Override
	public String toString() {
		return "GasVariantImpl{gas=" + gas + ", nbt=" + nbt + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GasVariantImpl that = (GasVariantImpl) o;
		return hashCode == that.hashCode && gas == that.gas && Objects.equals(nbt, that.nbt);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}
