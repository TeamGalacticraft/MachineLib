package dev.galacticraft.machinelib.testmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class BatteryItem extends Item implements SimpleEnergyItem {
    private final long capacity;

    public BatteryItem(Properties properties, long capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return this.capacity;
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return this.capacity / 50;
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return this.capacity / 50;
    }
}
