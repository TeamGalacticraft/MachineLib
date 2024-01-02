/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.ResourceStorage;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class ResourceStorageSyncHandler<Resource, Slot extends ResourceSlot<Resource>> implements MenuSyncHandler {
    private final Slot[] slots;
    private final long[] modifications;

    public ResourceStorageSyncHandler(ResourceStorage<Resource, Slot> storage) {
        this.slots = storage.getSlots();
        this.modifications = new long[storage.size()];
    }

    @Override
    public boolean needsSyncing() {
        for (int i = 0; i < this.slots.length; i++) {
            if (this.slots[i].getModifications() != this.modifications[i]) return true;
        }
        return false;
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        int total = 0;
        for (int i = 0; i < this.slots.length; i++) {
            if (this.slots[i].getModifications() != this.modifications[i]) total++;
        }
        buf.writeVarInt(total);
        for (int i = 0; i < this.slots.length; i++) {
            Slot slot = this.slots[i];
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
            Slot slot = this.slots[j];
            slot.readPacket(buf);
            slot.markModified(); // modification count on the server and client do not have to match - it just needs to change.
        }
    }
}
