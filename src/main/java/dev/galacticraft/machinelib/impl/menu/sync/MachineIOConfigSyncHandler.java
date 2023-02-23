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

package dev.galacticraft.machinelib.impl.menu.sync;

import dev.galacticraft.machinelib.api.machine.configuration.MachineIOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.face.BlockFace;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class MachineIOConfigSyncHandler implements MenuSyncHandler {
    private final MenuSyncHandler[] syncHandlers = new MenuSyncHandler[6];

    public MachineIOConfigSyncHandler(MachineIOConfig config) {
        BlockFace[] values = BlockFace.values();
        for (int i = 0; i < values.length; i++) {
            this.syncHandlers[i] = config.get(values[i]).createSyncHandler();
        }
    }

    @Override
    public boolean needsSyncing() {
        for (MenuSyncHandler syncHandler : this.syncHandlers) {
            if (syncHandler.needsSyncing()) return true;
        }
        return false;
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        byte total = 0;
        for (MenuSyncHandler syncHandler : this.syncHandlers) {
            if (syncHandler.needsSyncing()) total++;
        }
        buf.writeByte(total);

        for (byte i = 0; i < 6; i++) {
            MenuSyncHandler syncHandler = this.syncHandlers[i];
            if (syncHandler.needsSyncing()) {
                buf.writeByte(i);
                syncHandler.sync(buf);
            }
        }
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        byte total = buf.readByte();
        for (byte i = 0; i < total; i++) {
            byte b = buf.readByte();
            this.syncHandlers[b].sync(buf);
        }
    }
}
