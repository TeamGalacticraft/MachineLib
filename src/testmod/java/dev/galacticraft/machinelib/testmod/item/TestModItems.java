/*
 * Copyright (c) 2021-2025 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.machinelib.testmod.item;

import dev.galacticraft.machinelib.testmod.Constant;
import dev.galacticraft.machinelib.testmod.block.TestModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.InfiniteEnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class TestModItems {
    public static final Item GENERATOR = new BlockItem(TestModBlocks.GENERATOR, new Item.Properties());
    public static final Item MIXER = new BlockItem(TestModBlocks.MIXER, new Item.Properties());
    public static final Item MELTER = new BlockItem(TestModBlocks.MELTER, new Item.Properties());
    public static final Item WIRE = new BlockItem(TestModBlocks.WIRE, new Item.Properties());
    public static final Item THIN_WIRE = new BlockItem(TestModBlocks.THIN_WIRE, new Item.Properties());
    public static final Item ENERGY_SOURCE = new BlockItem(TestModBlocks.ENERGY_SOURCE, new Item.Properties());
    public static final Item ENERGY_SINK = new BlockItem(TestModBlocks.ENERGY_SINK, new Item.Properties());
    public static final Item WEAK_ENERGY_SINK = new BlockItem(TestModBlocks.WEAK_ENERGY_SINK, new Item.Properties());

    public static final Item INFINITE_BATTERY = new Item(new Item.Properties());
    public static final BatteryItem BASIC_BATTERY = new BatteryItem(new Item.Properties(), 15000);

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, Constant.id(Constant.GENERATOR), TestModItems.GENERATOR);
        Registry.register(BuiltInRegistries.ITEM, Constant.id(Constant.MIXER), TestModItems.MIXER);
        Registry.register(BuiltInRegistries.ITEM, Constant.id(Constant.MELTER), TestModItems.MELTER);
        Registry.register(BuiltInRegistries.ITEM, Constant.id(Constant.WIRE), TestModItems.WIRE);
        Registry.register(BuiltInRegistries.ITEM, Constant.id(Constant.THIN_WIRE), TestModItems.THIN_WIRE);
        Registry.register(BuiltInRegistries.ITEM, Constant.id(Constant.ENERGY_SOURCE), TestModItems.ENERGY_SOURCE);
        Registry.register(BuiltInRegistries.ITEM, Constant.id(Constant.ENERGY_SINK), TestModItems.ENERGY_SINK);
        Registry.register(BuiltInRegistries.ITEM, Constant.id(Constant.WEAK_ENERGY_SINK), TestModItems.WEAK_ENERGY_SINK);

        Registry.register(BuiltInRegistries.ITEM, Constant.id("infinite_battery"), TestModItems.INFINITE_BATTERY);
        Registry.register(BuiltInRegistries.ITEM, Constant.id("battery"), TestModItems.BASIC_BATTERY);

        EnergyStorage.ITEM.registerForItems((stack, context) -> InfiniteEnergyStorage.INSTANCE, TestModItems.INFINITE_BATTERY);
        EnergyStorage.ITEM.registerForItems((stack, context) -> SimpleEnergyItem.createStorage(context, BASIC_BATTERY.getEnergyCapacity(stack), BASIC_BATTERY.getEnergyMaxInput(stack), BASIC_BATTERY.getEnergyMaxOutput(stack)), BASIC_BATTERY);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(GENERATOR);
            entries.accept(MIXER);
            entries.accept(MELTER);
            entries.accept(THIN_WIRE);
            entries.accept(WIRE);
            entries.accept(ENERGY_SOURCE);
            entries.accept(ENERGY_SINK);
            entries.accept(WEAK_ENERGY_SINK);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(INFINITE_BATTERY);
            entries.accept(BASIC_BATTERY);
            ItemStack chargedBattery = new ItemStack(BASIC_BATTERY);
            BASIC_BATTERY.setStoredEnergy(chargedBattery, BASIC_BATTERY.getEnergyCapacity(chargedBattery));
            entries.accept(chargedBattery);
        });
    }
}
