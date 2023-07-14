package dev.galacticraft.machinelib.test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class Utils {
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
