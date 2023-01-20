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

package dev.galacticraft.machinelib.testmod;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.menu.SimpleMachineMenu;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.testmod.block.SimpleMachineBlock;
import dev.galacticraft.machinelib.testmod.block.entity.SimpleMachineBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.InfiniteEnergyStorage;
import team.reborn.energy.api.base.LimitingEnergyStorage;

import java.util.function.Predicate;

public class TestMod implements ModInitializer {
    public static final String MOD_ID = "machinelib-test";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final SlotGroupType CHARGE_SLOT = SlotGroupType.create(TextColor.fromLegacyFormat(ChatFormatting.YELLOW), Component.translatable("machinelib.testmod.charge_slot"), false);
    public static final SlotGroupType NO_DIAMOND_SLOT = SlotGroupType.create(TextColor.fromLegacyFormat(ChatFormatting.RED), Component.translatable("machinelib.testmod.no_diamond_slot"), true);
    public static final SlotGroupType ANY_FLUID_SLOT = SlotGroupType.create(TextColor.fromLegacyFormat(ChatFormatting.BLACK), Component.translatable("machinelib.testmod.fluids"), true);

    public static final Predicate<ItemVariant> NO_DIAMONDS = v -> v.getItem() != Items.DIAMOND;
    public static final Predicate<ItemVariant> ANY_ITEM = v -> true;
    public static final Predicate<FluidVariant> ANY_FLUID = v -> true;

    public static final String SIMPLE_MACHINE = "simple_machine";
    public static final MachineStatus WORKING = MachineStatus.createAndRegister(id("charge_slot"), Component.translatable("machinelib.testmod.working"), MachineStatus.Type.WORKING);
    public static final Block SIMPLE_MACHINE_BLOCK = new SimpleMachineBlock(FabricBlockSettings.of(Material.METAL));
    public static final Item SIMPLE_MACHINE_ITEM = new BlockItem(SIMPLE_MACHINE_BLOCK, new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS));
    public static final Item INFINITE_BATTERY = new Item(new Item.Properties().tab(CreativeModeTab.TAB_MISC));

    @Contract("_ -> new")
    public static @NotNull ResourceLocation id(@NotNull String id) {
        return new ResourceLocation(MOD_ID, id);
    }    public static final BlockEntityType<SimpleMachineBlockEntity> SIMPLE_MACHINE_BE_TYPE = FabricBlockEntityTypeBuilder.create(SimpleMachineBlockEntity::new, SIMPLE_MACHINE_BLOCK).build();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing MachineLib test mod");
        assert FabricLoader.getInstance().isDevelopmentEnvironment() : "Test mod loaded outside of development environment!?";

        EnergyStorage.ITEM.registerForItems((itemStack, context) -> new LimitingEnergyStorage(InfiniteEnergyStorage.INSTANCE, 150, 150), INFINITE_BATTERY);

        Registry.register(Registry.BLOCK, id(SIMPLE_MACHINE), SIMPLE_MACHINE_BLOCK);
        Registry.register(Registry.ITEM, id(SIMPLE_MACHINE), SIMPLE_MACHINE_ITEM);
        Registry.register(Registry.ITEM, id("battery"), INFINITE_BATTERY);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, id(SIMPLE_MACHINE), SIMPLE_MACHINE_BE_TYPE);
        Registry.register(Registry.MENU, id(SIMPLE_MACHINE), SIMPLE_MACHINE_SH_TYPE);

        MachineBlockEntity.registerComponents(SIMPLE_MACHINE_BLOCK);
    }    public static final MenuType<SimpleMachineMenu<SimpleMachineBlockEntity>> SIMPLE_MACHINE_SH_TYPE = new ExtendedScreenHandlerType<>(SimpleMachineMenu.createFactory(() -> TestMod.SIMPLE_MACHINE_SH_TYPE));




}
