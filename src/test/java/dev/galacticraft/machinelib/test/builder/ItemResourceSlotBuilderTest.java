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

package dev.galacticraft.machinelib.test.builder;

import com.mojang.datafixers.util.Pair;
import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.test.JUnitTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemResourceSlotBuilderTest implements JUnitTest {
    private ItemResourceSlot.Builder builder;
    
    @BeforeEach
    public void setup() {
        this.builder = ItemResourceSlot.builder();
    }

    @Test
    public void invalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> builder.capacity(0).build());
    }

    @Test
    public void capacity() {
        ItemResourceSlot slot = builder.capacity(32).build();

        assertEquals(32, slot.getCapacity());
    }

    @Test
    public void icon() {
        Pair<ResourceLocation, ResourceLocation> icon = new Pair<>(new ResourceLocation("null"), new ResourceLocation("null"));
        ItemResourceSlot slot = builder.icon(icon).build();

        assertEquals(icon, slot.getDisplay().icon());
    }

    @Test
    public void displayPosition() {
        ItemResourceSlot slot = builder.pos(11, 43).build();

        assertEquals(11, slot.getDisplay().x());
        assertEquals(43, slot.getDisplay().y());
    }

    @Test
    public void displayPositionXY() {
        ItemResourceSlot slot = builder.x(5).y(7).build();

        assertEquals(5, slot.getDisplay().x());
        assertEquals(7, slot.getDisplay().y());
    }

    @Test
    public void defaultFilter() {
        ItemResourceSlot slot = builder.build();

        assertSame(ResourceFilters.any(), slot.getFilter());
    }

    @Test
    public void filter() {
        ItemResourceSlot slot = builder.filter(ResourceFilters.none()).build();

        assertSame(ResourceFilters.none(), slot.getFilter());
        assertEquals(0, slot.insert(Items.DIRT, 1));
    }

    @Test
    public void strictFilter() {
        ItemResourceSlot slot = builder.strictFilter(ResourceFilters.none()).build();

        assertEquals(1, slot.insert(Items.DIRT, 1));
    }
}
