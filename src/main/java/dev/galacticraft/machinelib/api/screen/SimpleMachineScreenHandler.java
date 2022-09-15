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

package dev.galacticraft.machinelib.api.screen;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SimpleMachineScreenHandler<T extends MachineBlockEntity> extends MachineScreenHandler<T> {
    protected SimpleMachineScreenHandler(int syncId, Player player, T machine, MenuType<? extends MachineScreenHandler<T>> type, int invX, int invY) {
        super(syncId, player, machine, type);
        this.addPlayerInventorySlots(invX, invY);
    }

    @Contract(pure = true)
    public static <T extends MachineBlockEntity> ExtendedScreenHandlerType.@NotNull ExtendedFactory<SimpleMachineScreenHandler<T>> createFactory(Supplier<MenuType<? extends MachineScreenHandler<T>>> handlerType) {
        return createFactory(handlerType, 8, 84);
    }

    @Contract(pure = true)
    public static <T extends MachineBlockEntity> ExtendedScreenHandlerType.@NotNull ExtendedFactory<SimpleMachineScreenHandler<T>> createFactory(Supplier<MenuType<? extends MachineScreenHandler<T>>> handlerType, int invY) {
        return createFactory(handlerType, 8, invY);
    }

    @Contract(pure = true)
    public static <T extends MachineBlockEntity> ExtendedScreenHandlerType.@NotNull ExtendedFactory<SimpleMachineScreenHandler<T>> createFactory(Supplier<MenuType<? extends MachineScreenHandler<T>>> handlerType, int invX, int invY) {
        return (syncId, inventory, buf) -> create(syncId, inventory.player, (T)inventory.player.level.getBlockEntity(buf.readBlockPos()), handlerType.get(), invX, invY);
    }

    @Contract("_, _, _, _ -> new")
    public static <T extends MachineBlockEntity> @NotNull SimpleMachineScreenHandler<T> create(int syncId, Player playerEntity, T machine, MenuType<? extends MachineScreenHandler<T>> handlerType) {
        return new SimpleMachineScreenHandler<>(syncId, playerEntity, machine, handlerType, 8, 84);
    }

    @Contract("_, _, _, _, _ -> new")
    public static <T extends MachineBlockEntity> @NotNull SimpleMachineScreenHandler<T> create(int syncId, Player playerEntity, T machine, MenuType<? extends MachineScreenHandler<T>> handlerType, int invY) {
        return new SimpleMachineScreenHandler<>(syncId, playerEntity, machine, handlerType, 8, invY);
    }

    @Contract("_, _, _, _, _, _ -> new")
    public static <T extends MachineBlockEntity> @NotNull SimpleMachineScreenHandler<T> create(int syncId, Player playerEntity, T machine, MenuType<? extends MachineScreenHandler<T>> handlerType, int invX, int invY) {
        return new SimpleMachineScreenHandler<>(syncId, playerEntity, machine, handlerType, invX, invY);
    }
}
