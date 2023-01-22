package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.storage.slot.InputType;
import net.minecraft.network.chat.TextColor;

public final class SlotGroupTypes {
    private SlotGroupTypes() {}

    public static final SlotGroupType CHARGE = SlotGroupType.createAndRegister(Constant.id("charge"), TextColor.fromRgb(0xffff55), InputType.TRANSFER);
    public static final SlotGroupType FLUID_TRANSFER = SlotGroupType.createAndRegister(Constant.id("fluid_transfer"), TextColor.fromRgb(0x55ffff), InputType.TRANSFER);
    public static final SlotGroupType ITEM_TRANSFER = SlotGroupType.createAndRegister(Constant.id("item_transfer"), TextColor.fromRgb(0xff55ff), InputType.TRANSFER);

    public static void initialize() {
    }
}
