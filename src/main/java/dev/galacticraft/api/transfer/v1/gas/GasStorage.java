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

package dev.galacticraft.api.transfer.v1.gas;

import com.google.common.base.Preconditions;
import dev.galacticraft.api.gas.GasVariant;
import dev.galacticraft.impl.machine.Constant;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class GasStorage {
    public static final BlockApiLookup<Storage<GasVariant>, Direction> SIDED =
            BlockApiLookup.get(new Identifier(Constant.MOD_ID, "sided_gas_storage"), Storage.asClass(), Direction.class);

    public static final ItemApiLookup<Storage<GasVariant>, ContainerItemContext> ITEM =
            ItemApiLookup.get(new Identifier(Constant.MOD_ID, "gas_storage"), Storage.asClass(), ContainerItemContext.class);

    private GasStorage() {
    }

    static {
        // Ensure that the lookup is only queried on the server side.
        GasStorage.SIDED.registerFallback((world, pos, state, blockEntity, context) -> {
            Preconditions.checkArgument(!world.isClient(), "Sided gas storage may only be queried for a server world.");
            return null;
        });
    }
}
