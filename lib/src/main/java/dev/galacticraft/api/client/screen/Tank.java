/*
 * Copyright (c) 2021-2022 Team Galacticraft
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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Somewhat like a {@link net.minecraft.screen.slot.Slot} but for fluids and gases.
 * Resources can be inserted into the tank and extracted from it via the gui.
 *
 * @see ResourceType#FLUID
 */
public interface Tank {
    @Contract(value = "_, _, _, _, _, _ -> new", pure = true)
    static @NotNull Tank create(ExposedStorage<Fluid, FluidVariant> storage, int index, int x, int y, int height) {
        return new TankImpl(storage, index, x, y, height);
    }

    /**
     * Returns the resource that is currently in this tank.
     * @return The resource that is currently in this tank.
     */
    FluidVariant getResource();

    /**
     * Returns the index of this tank in the storage.
     * @return The index of this tank in the storage.
     */
    int getIndex();

    /**
     * Retunrs the x-position of this tank.
     * @return The x-position of this tank.
     */
    int getX();

    /**
     * Returns the y-position of this tank.
     * @return The y-position of this tank.
     */
    int getY();

    /**
     * Returns the height of this tank.
     * @return The height of this tank.
     */
    int getHeight();

    /**
     * Returns the width of this tank.
     * Currently, always returns {@code 16}.
     * @return The width of this tank.
     */
    int getWidth();

    /**
     * Returns the id of this tank (the index of the tank in the screen).
     * @return The id of this tank.
     */
    int getId();

    /**
     * Sets the id of this tank.
     * @param id The id of this tank.
     */
    @ApiStatus.Internal
    void setId(int id);

    void drawTooltip(@NotNull MatrixStack matrices, MinecraftClient client, int x, int y, int mouseX, int mouseY);

    boolean acceptStack(@NotNull ContainerItemContext context);

    @ApiStatus.Internal ExposedStorage<Fluid, FluidVariant> getStorage();

    long getAmount();

    long getCapacity();
}
