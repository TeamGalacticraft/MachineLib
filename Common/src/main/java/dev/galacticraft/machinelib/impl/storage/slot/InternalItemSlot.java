package dev.galacticraft.machinelib.impl.storage.slot;

import dev.galacticraft.machinelib.api.storage.slot.ItemSlot;
import net.minecraft.world.item.ItemStack;

public interface InternalItemSlot extends ItemSlot, InternalChangeTracking {
    void silentSetStack(ItemStack stack);
}
