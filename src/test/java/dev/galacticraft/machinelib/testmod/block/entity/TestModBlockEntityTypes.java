package dev.galacticraft.machinelib.testmod.block.entity;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.testmod.Constant;
import dev.galacticraft.machinelib.testmod.block.TestModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TestModBlockEntityTypes {
    public static final BlockEntityType<SimpleMachineBlockEntity> SIMPLE_MACHINE = FabricBlockEntityTypeBuilder.create(SimpleMachineBlockEntity::new, TestModBlocks.SIMPLE_MACHINE_BLOCK).build();

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constant.id(Constant.SIMPLE_MACHINE), TestModBlockEntityTypes.SIMPLE_MACHINE);

        MachineBlockEntity.registerComponents(TestModBlocks.SIMPLE_MACHINE_BLOCK);
    }
}
