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

import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineType;
import dev.galacticraft.machinelib.api.machine.configuration.MachineConfiguration;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneActivation;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class MachineConfigurationSyncHandler implements MenuSyncHandler {
    private final MachineConfiguration configuration;
    private final MenuSyncHandler ioConfig;
    private final MenuSyncHandler security;
    private MachineStatus status;
    private RedstoneActivation redstone;
    private MachineType<?, ?> type;

    public MachineConfigurationSyncHandler(MachineConfiguration configuration) {
        this.ioConfig = configuration.getIOConfiguration().createSyncHandler();
        this.security = configuration.getSecurity().createSyncHandler();
        this.status = configuration.getStatus();
        this.redstone = configuration.getRedstoneActivation();
        this.type = configuration.getType();
        this.configuration = configuration;
    }

    @Override
    public boolean needsSyncing() {
        return this.ioConfig.needsSyncing() || this.security.needsSyncing() || this.status != configuration.getStatus() || this.redstone != configuration.getRedstoneActivation();
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        byte ref = 0b0000;
        if (this.ioConfig.needsSyncing()) ref |= 0b0001;
        if (this.security.needsSyncing()) ref |= 0b0010;
        if (this.status != this.configuration.getStatus()) ref |= 0b0100;
        if (this.redstone != this.configuration.getRedstoneActivation()) ref |= 0b1000;

        buf.writeByte(ref);

        if (this.ioConfig.needsSyncing()) {
            this.ioConfig.sync(buf);
        }
        if (this.security.needsSyncing()) {
            this.security.sync(buf);
        }
        if (this.status != this.configuration.getStatus()) {
            this.status = this.configuration.getStatus();
            buf.writeByte(this.type.statusDomain().indexOf(this.configuration.getStatus()));
        }
        if (this.redstone != this.configuration.getRedstoneActivation()) {
            this.redstone = this.configuration.getRedstoneActivation();
            buf.writeByte(this.configuration.getRedstoneActivation().ordinal());
        }
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        byte ref = buf.readByte();
        if ((ref & 0b0001) != 0) {
            this.ioConfig.read(buf);
        }
        if ((ref & 0b0010) != 0) {
            this.security.read(buf);
        }
        if ((ref & 0b0100) != 0) {
            this.status = this.type.statusDomain().get(buf.readByte());
            this.configuration.setStatus(this.status);
        }
        if ((ref & 0b1000) != 0) {
            this.redstone = RedstoneActivation.VALUES[buf.readByte()];
            this.configuration.setRedstoneActivation(this.redstone);
        }
    }
}
