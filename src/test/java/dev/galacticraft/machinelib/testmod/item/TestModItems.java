/*
 * Copyright (c) 2021-2023 Team Galacticraft
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
    public static final Item SIMPLE_MACHINE_ITEM = new BlockItem(TestModBlocks.SIMPLE_MACHINE_BLOCK, new Item.Properties());
    public static final Item INFINITE_BATTERY = new Item(new Item.Properties());
    public static final BatteryItem BASIC_BATTERY = new BatteryItem(new Item.Properties(), 1500);

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, Constant.id(Constant.SIMPLE_MACHINE), TestModItems.SIMPLE_MACHINE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, Constant.id("infinite_battery"), TestModItems.INFINITE_BATTERY);
        Registry.register(BuiltInRegistries.ITEM, Constant.id("battery"), TestModItems.BASIC_BATTERY);

        EnergyStorage.ITEM.registerForItems((stack, context) -> InfiniteEnergyStorage.INSTANCE, TestModItems.INFINITE_BATTERY);
        EnergyStorage.ITEM.registerForItems((stack, context) -> SimpleEnergyItem.createStorage(context, BASIC_BATTERY.getEnergyCapacity(stack), BASIC_BATTERY.getEnergyMaxInput(stack), BASIC_BATTERY.getEnergyMaxOutput(stack)), BASIC_BATTERY);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(SIMPLE_MACHINE_ITEM);
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
