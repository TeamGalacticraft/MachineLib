/*
 * Copyright (c) 2021-2025 Team Galacticraft
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

package dev.galacticraft.machinelib.api.menu;

import com.google.common.base.Preconditions;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.impl.menu.TankImpl;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Somewhat like a {@link net.minecraft.world.inventory.Slot} but for fluids and gases.
 * Resources can be inserted into the tank and extracted from it via the gui.
 *
 * @see ResourceType#FLUID
 */
public interface Tank {
    /**
     * Creates a new tank.
     *
     * @param slot the slot that this tank is associated with
     * @param display the display properties of the tank
     * @param transferType the input type of the tank
     * @param index the index of the tank in the storage
     * @return a new tank
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    static @NotNull Tank create(@NotNull ResourceSlot<Fluid> slot, @NotNull TankDisplay display, @NotNull TransferType transferType, int index) {
        Preconditions.checkNotNull(slot);
        return new TankImpl(slot, transferType, index, display.x(), display.y(), display.width(), display.height(), display.marked());
    }

    /**
     * {@return the resource that is currently in this tank}
     */
    @Nullable
    Fluid getFluid();

    /**
     * {@return the components of the fluid in the tank}
     */
    @NotNull
    DataComponentPatch getComponents();

    /**
     * {@return the amount of fluid in the tank}
     */
    long getAmount();

    /**
     * {@return the capacity of the tank}
     */
    long getCapacity();

    /**
     * {@return whether the tank is empty}
     */
    boolean isEmpty();

    /**
     * {@return a fluid variant constructed from the fluid and components in the tank}
     */
    FluidVariant createVariant();

    /**
     * {@return the index of this tank in the storage}
     */
    int getIndex();

    /**
     * {@return the x-position of this tank}
     */
    int getX();

    /**
     * {@return the y-position of this tank}
     */
    int getY();

    /**
     * {@return the sizeY of this tank}
     */
    int getHeight();

    /**
     * {@return the sizeX of this tank} Currently, always returns {@code 16}.
     */
    int getWidth();

    /**
     * {@return whether this tank has markings}
     */
    boolean isMarked();

    /**
     * {@return the id of this tank (the index of the tank in the screen)}
     */
    int getId();

    /**
     * Sets the id of this tank. For internal use only.
     *
     * @param id The id of this tank.
     */
    @ApiStatus.Internal
    void setId(int id);

    /**
     * Generates a tooltip based on the fluid and capacity of the tank.
     *
     * @return tooltip text
     */
    List<Component> getTooltip();

    /**
     * Attempts to insert/extract fluid from/into the given item.
     *
     * @param context the item interacting with the tank
     * @return whether the contents of the tank changed
     */
    boolean acceptStack(@NotNull ContainerItemContext context);

    /**
     * {@return the slot that this tank is associated with}
     */
    @ApiStatus.Internal
    ResourceSlot<Fluid> getSlot();

    /**
     * {@return the input type of this tank}
     */
    TransferType getInputType();
}
