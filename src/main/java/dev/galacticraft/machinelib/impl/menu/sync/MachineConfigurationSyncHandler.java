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

import dev.galacticraft.machinelib.api.machine.configuration.MachineConfiguration;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneActivation;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class MachineConfigurationSyncHandler implements MenuSyncHandler {
    private final MachineConfiguration configuration;

    private final MenuSyncHandler ioConfig;
    private final MenuSyncHandler security;
    private RedstoneActivation redstone;

    public MachineConfigurationSyncHandler(MachineConfiguration configuration) {
        this.configuration = configuration;

        this.ioConfig = configuration.getIOConfiguration().createSyncHandler();
        this.security = configuration.getSecurity().createSyncHandler();
        this.redstone = configuration.getRedstoneActivation();
    }

    @Override
    public boolean needsSyncing() {
        return this.ioConfig.needsSyncing() || this.security.needsSyncing() || this.redstone != configuration.getRedstoneActivation();
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        byte ref = 0b000;
        if (this.ioConfig.needsSyncing()) ref |= 0b001;
        if (this.security.needsSyncing()) ref |= 0b010;
        if (this.redstone != this.configuration.getRedstoneActivation()) ref |= 0b100;

        buf.writeByte(ref);

        if (this.ioConfig.needsSyncing()) {
            this.ioConfig.sync(buf);
        }
        if (this.security.needsSyncing()) {
            this.security.sync(buf);
        }

        if (this.redstone != this.configuration.getRedstoneActivation()) {
            this.redstone = this.configuration.getRedstoneActivation();
            buf.writeByte(this.configuration.getRedstoneActivation().ordinal());
        }
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        byte ref = buf.readByte();
        if ((ref & 0b001) != 0) {
            this.ioConfig.read(buf);
        }
        if ((ref & 0b010) != 0) {
            this.security.read(buf);
        }
        if ((ref & 0b100) != 0) {
            this.redstone = RedstoneActivation.VALUES[buf.readByte()];
            this.configuration.setRedstoneActivation(this.redstone);
        }
    }
}
