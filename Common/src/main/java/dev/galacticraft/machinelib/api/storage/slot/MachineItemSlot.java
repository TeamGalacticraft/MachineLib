package dev.galacticraft.machinelib.api.storage.slot;

import dev.galacticraft.machinelib.impl.storage.slot.InternalChangeTracking;
import net.minecraft.world.item.ItemStack;

public interface MachineItemSlot extends ItemSlot, InternalChangeTracking {
    void silentSetStack(ItemStack stack);
}
