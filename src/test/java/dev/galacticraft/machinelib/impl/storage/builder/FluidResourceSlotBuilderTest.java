/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.storage.builder;

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.test.MinecraftTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FluidResourceSlotBuilderTest implements MinecraftTest {
    FluidResourceSlot.Spec spec;

    @BeforeEach
    void setup() {
        this.spec = FluidResourceSlot.builder(TransferType.STORAGE);
    }

    @Test
    void invalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> spec.capacity(0).create());
    }

    @Test
    void capacity() {
        FluidResourceSlot slot = spec.capacity(FluidConstants.BUCKET * 64).create();

        assertEquals(FluidConstants.BUCKET * 64, slot.getCapacity());
    }

    @Test
    void hidden() {
        spec.hidden();

        assertThrows(UnsupportedOperationException.class, () -> spec.x(0));
        assertThrows(UnsupportedOperationException.class, () -> spec.y(0));
        assertThrows(UnsupportedOperationException.class, () -> spec.width(0));
        assertThrows(UnsupportedOperationException.class, () -> spec.height(0));

        assertNull(spec.create().getDisplay());
        assertTrue(spec.create().isHidden());
    }

    @Test
    void hiddenPost() {
        spec.x(10).hidden();

        assertThrows(UnsupportedOperationException.class, () -> spec.create());
    }

    @Test
    void height() {
        FluidResourceSlot slot = spec.height(16).create();

        assertEquals(16, slot.getDisplay().height());
    }

    @Test
    void invalidHeight() {
        spec.height(-4);

        assertThrows(IllegalArgumentException.class, () -> spec.create());
    }

    @Test
    void width() {
        FluidResourceSlot slot = spec.width(16).create();

        assertEquals(16, slot.getDisplay().width());
    }

    @Test
    void invalidWidth() {
        spec.width(-4);

        assertThrows(IllegalArgumentException.class, () -> spec.create());
    }

    @Test
    void displayPosition() {
        FluidResourceSlot slot = spec.pos(11, 43).create();

        assertEquals(11, slot.getDisplay().x());
        assertEquals(43, slot.getDisplay().y());
    }

    @Test
    void displayPositionXY() {
        FluidResourceSlot slot = spec.x(5).y(7).create();

        assertEquals(5, slot.getDisplay().x());
        assertEquals(7, slot.getDisplay().y());
    }

    @Test
    void defaultFilter() {
        FluidResourceSlot slot = spec.create();

        assertSame(ResourceFilters.any(), slot.getFilter());
    }

    @Test
    void filter() {
        FluidResourceSlot slot = spec.filter(ResourceFilters.none()).create();

        assertSame(ResourceFilters.none(), slot.getFilter());
        assertEquals(1, slot.insert(Fluids.WATER, 1)); // filters only affect players
    }
}
