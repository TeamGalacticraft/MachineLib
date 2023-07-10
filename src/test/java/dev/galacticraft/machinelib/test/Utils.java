package dev.galacticraft.machinelib.test;

import dev.galacticraft.machinelib.api.fluid.FluidStack;
import dev.galacticraft.machinelib.api.storage.ResourceFilters;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.api.storage.slot.display.TankDisplay;
import dev.galacticraft.machinelib.impl.storage.slot.InputType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Supplier;

public class Utils {
    public static final Supplier<FluidResourceSlot> FLUID_SLOT_SUPPLIER = () -> FluidResourceSlot.create(TankDisplay.create(0, 0), FluidConstants.BUCKET, ResourceFilters.any());
    public static final Supplier<SlotGroup<Fluid, FluidStack, FluidResourceSlot>> FLUID_GROUP_SUPPLIER = () -> SlotGroup.ofFluid(FLUID_SLOT_SUPPLIER.get());

    public static final Supplier<ItemResourceSlot> ITEM_SLOT_SUPPLIER = () -> ItemResourceSlot.create(ItemSlotDisplay.create(0, 0), ResourceFilters.any());
    public static final Supplier<SlotGroup<Item, ItemStack, ItemResourceSlot>> ITEM_GROUP_SUPPLIER = () -> SlotGroup.ofItem(ITEM_SLOT_SUPPLIER.get());

    public static final SlotGroupType INPUT_1 = SlotGroupType.create("", 0, InputType.INPUT);
    public static final SlotGroupType INPUT_2 = SlotGroupType.create("", 0, InputType.INPUT);
    public static final SlotGroupType INPUT_3 = SlotGroupType.create("", 0, InputType.INPUT);
    public static final SlotGroupType OUTPUT_1 = SlotGroupType.create("", 0, InputType.OUTPUT);
    public static final SlotGroupType OUTPUT_2 = SlotGroupType.create("", 0, InputType.OUTPUT);
    public static final SlotGroupType OUTPUT_3 = SlotGroupType.create("", 0, InputType.OUTPUT);
    public static final SlotGroupType TRANSFER_1 = SlotGroupType.create("", 0, InputType.TRANSFER);
    public static final SlotGroupType TRANSFER_2 = SlotGroupType.create("", 0, InputType.TRANSFER);
    public static final SlotGroupType TRANSFER_3 = SlotGroupType.create("", 0, InputType.TRANSFER);
    public static final SlotGroupType STORAGE_1 = SlotGroupType.create("", 0, InputType.STORAGE);
    public static final SlotGroupType STORAGE_2 = SlotGroupType.create("", 0, InputType.STORAGE);
    public static final SlotGroupType STORAGE_3 = SlotGroupType.create("", 0, InputType.STORAGE);

    public static final CompoundTag EMPTY_NBT = new CompoundTag();

    private static long counter = 0;
    public static CompoundTag generateNbt() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putLong("UniqueId", counter++);
        return compoundTag;
    }

    public static ItemStack itemStack(Item item, CompoundTag tag, int amount) {
        ItemStack itemStack = new ItemStack(item, amount);
        itemStack.setTag(tag);
        return itemStack;
    }
}
