package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ResourceStorageSyncHandler<Resource, Stack, Slot extends ResourceSlot<Resource, Stack>, Group extends SlotGroup<Resource, Stack, Slot>> implements MenuSyncHandler {
    private final List<Slot> slots = new ArrayList<>();
    private final long[] modifications;

    public ResourceStorageSyncHandler(ResourceStorage<Resource, Stack, Slot, Group> storage) {
        LongList list = new LongArrayList();

        for (Group group : storage) {
            for (Slot slot : group) {
                list.add(slot.getModifications());
                this.slots.add(slot);
            }
        }
        this.modifications = list.toLongArray();
    }

    @Override
    public boolean needsSyncing() {
        for (int i = 0; i < this.slots.size(); i++) {
            if (this.slots.get(i).getModifications() != this.modifications[i]) return true;
        }
        return false;
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        int total = 0;
        for (int i = 0; i < this.slots.size(); i++) {
            if (this.slots.get(i).getModifications() != this.modifications[i]) total++;
        }
        buf.writeVarInt(total);
        for (int i = 0; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            if (slot.getModifications() != this.modifications[i]) {
                this.modifications[i] = slot.getModifications();
                buf.writeVarInt(i);
                slot.writePacket(buf);
            }
        }
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        int total = buf.readVarInt();
        for (int i = 0; i < total; i++) {
            int j = buf.readVarInt();
            Slot slot = this.slots.get(j);
            slot.readPacket(buf);
            slot.markModified(); // modification count on the server and client do not have to match - it just needs to change.
        }
    }
}
