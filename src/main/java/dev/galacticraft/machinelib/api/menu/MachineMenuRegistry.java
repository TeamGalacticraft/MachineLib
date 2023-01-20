package dev.galacticraft.machinelib.api.menu;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.MenuType;

public interface MachineMenuRegistry {
    static <M extends MachineBlockEntity, T extends MachineMenu<M>> void register(MenuType<T> type, SyncHandler<M, T> handler) {

    }

    static <M extends MachineBlockEntity, T extends MachineMenu<M>> SyncHandler<M, T> get(MenuType<T> type) {

    }

    interface SyncHandler<M extends MachineBlockEntity, T extends MachineMenu<M>> {
        void sync(T menu, FriendlyByteBuf buf);
    }
}
