package dev.galacticraft.machinelib.testmod.item;

import dev.galacticraft.machinelib.testmod.Constant;
import dev.galacticraft.machinelib.testmod.block.TestModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.InfiniteEnergyStorage;
import team.reborn.energy.api.base.LimitingEnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class TestModItems {
    public static final Item SIMPLE_MACHINE_ITEM = new BlockItem(TestModBlocks.SIMPLE_MACHINE_BLOCK, new Item.Properties());
    public static final Item INFINITE_BATTERY = new Item(new Item.Properties());
    public static final BatteryItem BASIC_BATTERY = new BatteryItem(new Item.Properties(), 1500);

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, Constant.id(Constant.SIMPLE_MACHINE), TestModItems.SIMPLE_MACHINE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, Constant.id("infinite_battery"), TestModItems.INFINITE_BATTERY);
        Registry.register(BuiltInRegistries.ITEM, Constant.id("battery"), TestModItems.BASIC_BATTERY);

        EnergyStorage.ITEM.registerForItems((stack, context) -> new LimitingEnergyStorage(InfiniteEnergyStorage.INSTANCE, 150, 150), TestModItems.INFINITE_BATTERY);
        EnergyStorage.ITEM.registerForItems((stack, context) -> SimpleEnergyItem.createStorage(context, BASIC_BATTERY.getEnergyCapacity(stack), BASIC_BATTERY.getEnergyMaxInput(stack), BASIC_BATTERY.getEnergyMaxOutput(stack)), BASIC_BATTERY);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(SIMPLE_MACHINE_ITEM);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(INFINITE_BATTERY);
        });
    }
}
