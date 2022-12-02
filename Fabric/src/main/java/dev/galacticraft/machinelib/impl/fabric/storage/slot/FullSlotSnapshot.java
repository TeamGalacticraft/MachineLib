package dev.galacticraft.machinelib.impl.fabric.storage.slot;

import net.minecraft.world.item.ItemStack;

public record FullSlotSnapshot(ItemStack stack, long slotModCount, long storageModCount) {
}
