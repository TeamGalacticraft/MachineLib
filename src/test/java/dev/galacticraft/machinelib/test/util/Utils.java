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

package dev.galacticraft.machinelib.test.util;

import io.netty.buffer.Unpooled;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;

import java.util.Random;

public class Utils {
    private static final Random RANDOM = new Random();
    private static long counter = 0;

    public static DataComponentPatch generateComponents() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putLong("UniqueId", counter++);
        return DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag)).build();
    }

    public static ItemStack itemStack(ItemLike item, DataComponentPatch components, int amount) {
        ItemStack stack = new ItemStack(item, amount);
        stack.applyComponents(components);
        return stack;
    }

    public static RegistryFriendlyByteBuf createBuf() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), StaticRegistryAccess.INSTANCE);
    }

    public static int random(int min, int not, int max) {
        int random = RANDOM.nextInt(min, max);
        return random == not ? random(min, not, max) : random;
    }

    public static long random(long min, long not, long max) {
        long random = RANDOM.nextLong(min, max);
        return random == not ? random(min, not, max) : random;
    }
}
