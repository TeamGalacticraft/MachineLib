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

import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MachineStateSyncHandler implements MenuSyncHandler {
    private final MachineState state;
    private @Nullable MachineStatus status;
    private boolean powered = false;

    public MachineStateSyncHandler(MachineState state) {
        this.state = state;
    }

    @Override
    public boolean needsSyncing() {
        return this.status != this.state.getStatus() || this.powered != this.state.isPowered();
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        this.status = this.state.getStatus();
        this.powered = this.state.isPowered();

        MachineStatus.writePacket(this.status, buf);
        buf.writeBoolean(this.powered);
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        this.status = MachineStatus.readPacket(buf);
        this.powered = buf.readBoolean();

        this.state.setStatus(this.status);
        this.state.setPowered(this.powered);
    }
}
