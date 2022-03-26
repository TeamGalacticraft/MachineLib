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

package dev.galacticraft.api.client.screen;

import dev.galacticraft.api.machine.storage.io.ExposedStorage;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.impl.client.screen.TankImpl;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Somewhat like a {@link net.minecraft.screen.slot.Slot} but for fluids and gases.
 * Resources can be inserted into the tank and extracted from it via the gui.
 *
 * @param <T> The type of resource that this slot holds.
 * @param <V> The resource that this slot holds.
 *
 * @see ResourceType#FLUID
 * @see ResourceType#GAS
 */
public interface Tank<T, V extends TransferVariant<T>> {
    @Contract(value = "_, _, _, _, _, _ -> new", pure = true)
    static <T, V extends TransferVariant<T>> @NotNull Tank<T, V> create(ExposedStorage<T, V> storage, int index, int x, int y, int height, @NotNull ResourceType<T, V> type) {
        if (type != ResourceType.GAS && type != ResourceType.FLUID) {
            throw new UnsupportedOperationException("Invalid tank of resource: " + type);
        }
        return new TankImpl<>(storage, index, x, y, height, type);
    }

    V getResource();

    int getIndex();

    int getX();

    int getY();

    int getHeight();

    int getWidth();

    int getId();

    void setId(int id);

    void drawTooltip(@NotNull MatrixStack matrices, MinecraftClient client, int x, int y, int mouseX, int mouseY);

    boolean acceptStack(@NotNull ContainerItemContext context);

    @NotNull ResourceType<T, V> getResourceType();

    @ApiStatus.Internal ExposedStorage<T, V> getStorage();

    long getAmount();

    long getCapacity();
}
