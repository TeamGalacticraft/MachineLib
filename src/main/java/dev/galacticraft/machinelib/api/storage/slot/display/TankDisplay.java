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

package dev.galacticraft.machinelib.api.storage.slot.display;

import dev.galacticraft.machinelib.impl.storage.exposed.slot.display.TankDisplayImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Display information for a tank.
 */
public interface TankDisplay {
    /**
     * Creates a display with the specified x and y coordinates.
     * The width of the tank display is set to 48 pixels.
     *
     * @param x the x-coordinate of the tank display
     * @param y the y-coordinate of the tank display
     * @return a new display
     */
    @Contract("_, _ -> new")
    static @NotNull TankDisplay create(int x, int y) {
        return create(x, y, 48);
    }

    /**
     * Creates a display with the specified x and y coordinates and height.
     *
     * @param x the x-coordinate of the tank display
     * @param y the y-coordinate of the tank display
     * @param height the height of the tank display in pixels
     * @return a new display
     */
    @Contract("_, _, _ -> new")
    static @NotNull TankDisplay create(int x, int y, int height) {
        return create(x, y, 16, height);
    }

    @Contract("_, _, _, _ -> new")
    static @NotNull TankDisplay create(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid size!");
        return new TankDisplayImpl(x, y, width, height);
    }

    /**
     * Returns the x-position of the tank.
     *
     * @return the x-position of the tank.
     */
    int x();

    /**
     * Returns the y-position of the tank.
     *
     * @return the y-position of the tank.
     */
    int y();

    /**
     * Returns the width of the tank.
     *
     * @return the width of the tank
     */
    int width();


    /**
     * Returns the height of the tank.
     *
     * @return the height of the tank
     */
    int height();
}
