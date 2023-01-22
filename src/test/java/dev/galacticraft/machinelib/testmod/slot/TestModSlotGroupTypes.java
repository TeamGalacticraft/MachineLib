package dev.galacticraft.machinelib.testmod.slot;

import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.impl.storage.slot.InputType;
import dev.galacticraft.machinelib.testmod.Constant;
import net.minecraft.network.chat.TextColor;

public class TestModSlotGroupTypes {
    public static final SlotGroupType DIRT = SlotGroupType.createAndRegister(Constant.id("dirt"), TextColor.fromRgb(0x774422), InputType.INPUT);
    public static final SlotGroupType DIAMONDS = SlotGroupType.createAndRegister(Constant.id("diamonds"), TextColor.fromRgb(0x55ffff), InputType.OUTPUT);

    public static final SlotGroupType SOLID_FUEL = SlotGroupType.createAndRegister(Constant.id("solid_fuel"), TextColor.fromRgb(0x000000), InputType.INPUT);
    public static final SlotGroupType WASTE = SlotGroupType.createAndRegister(Constant.id("waste"), TextColor.fromRgb(0xaa0000), InputType.OUTPUT);

    public static void initialize() {
    }
}
