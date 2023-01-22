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

import dev.galacticraft.machinelib.api.block.face.MachineIOFace;
import dev.galacticraft.machinelib.api.menu.sync.MenuSyncHandler;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.api.storage.io.StorageSelection;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MachineIOFaceSyncHandler implements MenuSyncHandler {
    private final MachineIOFace face;

    private @NotNull ResourceType prevType;
    private @NotNull ResourceFlow prevFlow;
    private @Nullable StorageSelection prevSelection;

    public MachineIOFaceSyncHandler(MachineIOFace face) {
        this.face = face;
        this.prevType = face.getType();
        this.prevFlow = face.getFlow();
        this.prevSelection = face.getSelection();
    }

    @Override
    public boolean needsSyncing() {
        return this.prevType != face.getType() || this.prevFlow != face.getFlow() || !Objects.equals(this.prevSelection, face.getSelection());
    }

    @Override
    public void sync(@NotNull FriendlyByteBuf buf) {
        byte ref = 0b00000;
        if (this.prevType != face.getType()) ref |= 0b00001;
        if (this.prevFlow != face.getFlow()) ref |= 0b00010;
        StorageSelection selection = face.getSelection();
        if (this.prevSelection != selection) {
            ref |= 0b00100;
            if (selection != null) {
                ref |= 0b01000;
                if (selection.isSlot()) {
                    ref |= 0b10000;
                }
            }
        }

        buf.writeByte(ref);

        if (this.prevType != face.getType()) {
            this.prevType = face.getType();
            buf.writeByte(face.getType().ordinal());
        }
        if (this.prevFlow != face.getFlow()) {
            this.prevFlow = face.getFlow();
            buf.writeByte(face.getFlow().ordinal());
        }
        if (this.prevSelection != selection) {
            this.prevSelection = selection;
            if (selection != null) {
                buf.writeUtf(Objects.requireNonNull(MachineLib.SLOT_GROUP_TYPE_REGISTRY.getKey(selection.getGroup())).toString());
                if (selection.isSlot()) {
                    buf.writeInt(selection.getSlot());
                }
            }
        }
    }

    @Override
    public void read(@NotNull FriendlyByteBuf buf) {
        byte ref = buf.readByte();
        if ((ref & 0b00001) != 0) {
            face.setOption(ResourceType.getFromOrdinal(buf.readByte()), face.getFlow());
        }
        if ((ref & 0b00010) != 0) {
            face.setOption(face.getType(), ResourceFlow.getFromOrdinal(buf.readByte()));
        }
        if ((ref & 0b00100) != 0) {
            StorageSelection selection;
            if ((ref & 0b01000) != 0) {
                SlotGroupType groupType = MachineLib.SLOT_GROUP_TYPE_REGISTRY.get(new ResourceLocation(buf.readUtf()));
                assert groupType != null;
                if ((ref & 0b10000) != 0) {
                    selection = StorageSelection.create(groupType, buf.readInt());
                } else {
                    selection = StorageSelection.create(groupType);
                }
            } else {
                selection = null;
            }
            face.setSelection(selection);
        }
    }
}
