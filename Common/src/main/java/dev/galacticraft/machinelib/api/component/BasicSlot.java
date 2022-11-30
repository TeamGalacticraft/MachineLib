package dev.galacticraft.machinelib.api.component;

import net.minecraft.world.item.ItemStack;

public interface BasicSlot {
    ItemStack getStack();

    default ItemStack copyStack() {
        return this.getStack().copy();
    }

    void setStack(ItemStack stack);

    void markDirty();
}
