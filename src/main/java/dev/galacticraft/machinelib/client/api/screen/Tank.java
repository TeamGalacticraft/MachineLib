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

package dev.galacticraft.machinelib.client.api.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.impl.menu.TankImpl;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Somewhat like a {@link net.minecraft.world.inventory.Slot} but for fluids and gases.
 * Resources can be inserted into the tank and extracted from it via the gui.
 *
 * @see ResourceType#FLUID
 */
public interface Tank {
    @Contract(value = "_, _ -> new", pure = true)
    static @NotNull Tank create(@NotNull FluidResourceSlot slot, int index) {
        Preconditions.checkNotNull(slot);
        return new TankImpl(slot, index, slot.getDisplay().x(), slot.getDisplay().y(), slot.getDisplay().height());
    }

    /**
     * Returns the resource that is currently in this tank.
     *
     * @return The resource that is currently in this tank.
     */
    @Nullable Fluid getFluid();

    @Nullable CompoundTag getTag();

    @Nullable CompoundTag copyTag();

    long getAmount();

    long getCapacity();

    boolean isEmpty();

    FluidVariant getVariant();

    /**
     * Returns the index of this tank in the storage.
     *
     * @return The index of this tank in the storage.
     */
    int getIndex();

    /**
     * Returns the x-position of this tank.
     *
     * @return The x-position of this tank.
     */
    int getX();

    /**
     * Returns the y-position of this tank.
     *
     * @return The y-position of this tank.
     */
    int getY();

    /**
     * Returns the height of this tank.
     *
     * @return The height of this tank.
     */
    int getHeight();

    /**
     * Returns the width of this tank.
     * Currently, always returns {@code 16}.
     *
     * @return The width of this tank.
     */
    int getWidth();

    /**
     * Returns the id of this tank (the index of the tank in the screen).
     *
     * @return The id of this tank.
     */
    int getId();

    /**
     * Sets the id of this tank.
     *
     * @param id The id of this tank.
     */
    @ApiStatus.Internal
    void setId(int id);

    void drawTooltip(@NotNull PoseStack matrices, Minecraft client, int x, int y, int mouseX, int mouseY);

    boolean acceptStack(@NotNull ContainerItemContext context);

    @ApiStatus.Internal
    ResourceSlot<Fluid, FluidStack> getSlot();
}
