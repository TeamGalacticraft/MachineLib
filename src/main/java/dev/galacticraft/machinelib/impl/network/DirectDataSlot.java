package dev.galacticraft.machinelib.impl.network;

import net.minecraft.world.inventory.DataSlot;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class DirectDataSlot extends DataSlot { //todo: look into sync to SH rather than BE
    private final IntSupplier getter;
    private final IntConsumer setter;

    public DirectDataSlot(IntSupplier getter, IntConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }
    @Override
    public int get() {
        return this.getter.getAsInt();
    }

    @Override
    public void set(int i) {
        this.setter.accept(i);
    }
}
