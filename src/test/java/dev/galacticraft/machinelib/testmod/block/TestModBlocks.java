package dev.galacticraft.machinelib.testmod.block;

import dev.galacticraft.machinelib.testmod.Constant;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

public class TestModBlocks {
    public static final Block SIMPLE_MACHINE_BLOCK = new SimpleMachineBlock(FabricBlockSettings.of(Material.METAL));

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, Constant.id(Constant.SIMPLE_MACHINE), TestModBlocks.SIMPLE_MACHINE_BLOCK);
    }
}
