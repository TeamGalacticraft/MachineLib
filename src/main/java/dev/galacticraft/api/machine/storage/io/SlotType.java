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

package dev.galacticraft.api.machine.storage.io;

import dev.galacticraft.impl.machine.storage.io.SlotTypeImpl;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public interface SlotType<T, V extends TransferVariant<T>> {
    static <T, V extends TransferVariant<T>> SlotType<T, V> create(@NotNull TextColor color, @NotNull MutableText name, @NotNull Predicate<V> filter, @NotNull ResourceFlow flow, @NotNull ResourceType<T, V> type) {
        return new SlotTypeImpl<>(color, name, filter, flow, type);
    }

    @NotNull TextColor getColor();

    @NotNull Text getName();

    @NotNull ResourceType<T, V> getType();

    @NotNull ResourceFlow getFlow();

    boolean willAccept(V variant);
}
