package dev.galacticraft.machinelib.api.util;

import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackUtil {
    public static @NotNull ItemStack create(ResourceSlot<Item> slot) {
        if (slot.isEmpty()) return ItemStack.EMPTY;
        assert slot.getResource() != null && slot.getAmount() < Integer.MAX_VALUE;
        ItemStack stack = new ItemStack(slot.getResource(), (int) slot.getAmount());
        stack.setTag(slot.getTag());
        return stack;
    }

    public static @NotNull ItemStack copy(ResourceSlot<Item> slot) {
        if (slot.isEmpty()) return ItemStack.EMPTY;
        assert slot.getResource() != null && slot.getAmount() < Integer.MAX_VALUE;
        ItemStack stack = new ItemStack(slot.getResource(), (int) slot.getAmount());
        stack.setTag(slot.copyTag());
        return stack;
    }
}
