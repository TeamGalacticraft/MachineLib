package dev.galacticraft.machinelib.api.util;

import dev.galacticraft.machinelib.api.storage.SlotProvider;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.impl.storage.AbstractSlotProvider;

public class SlotGrouping<Resource, Slot extends ResourceSlot<Resource>> extends AbstractSlotProvider<Resource, Slot> implements SlotProvider<Resource, Slot> { //todo: rename when refactor is done
    private final long[] modifications;
    private long modCount = 0;

    public SlotGrouping(Slot[] slots) {
        super(slots);
        this.modifications = new long[this.slots.length];
    }

    @Override
    public long getModifications() {
        for (int i = 0; i < this.slots.length; i++) {
            if (this.modifications[i] != (this.modifications[i] = this.slots[i].getModifications())) {
                this.modCount++;
            }
        }
        return this.modCount;
    }
}
