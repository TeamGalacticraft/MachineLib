package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface MultiblockSlotPredicate {

    boolean matches(Level level, BlockPos pos, BlockState state);

    boolean canBeDetectionCenter();

    Block[] detectionBlocks();

    String signatureKey();

    static MultiblockSlotPredicate block(final Block block) {
        return new MultiblockSlotPredicate() {

            @Override
            public boolean matches(final Level level, final BlockPos pos, final BlockState state) {
                return state.is(block);
            }

            @Override
            public boolean canBeDetectionCenter() {
                return true;
            }

            @Override
            public Block[] detectionBlocks() {
                return new Block[] { block };
            }

            @Override
            public String signatureKey() {
                return "block:" + BuiltInRegistries.BLOCK.getKey(block);
            }
        };
    }

    static MultiblockSlotPredicate anyBlock() {
        return new MultiblockSlotPredicate() {

            @Override
            public boolean matches(final Level level, final BlockPos pos, final BlockState state) {
                return !state.isAir();
            }

            @Override
            public boolean canBeDetectionCenter() {
                return false;
            }

            @Override
            public Block[] detectionBlocks() {
                return new Block[0];
            }

            @Override
            public String signatureKey() {
                return "any_block";
            }
        };
    }

    static MultiblockSlotPredicate air() {
        return new MultiblockSlotPredicate() {

            @Override
            public boolean matches(final Level level, final BlockPos pos, final BlockState state) {
                return state.isAir();
            }

            @Override
            public boolean canBeDetectionCenter() {
                return false;
            }

            @Override
            public Block[] detectionBlocks() {
                return new Block[0];
            }

            @Override
            public String signatureKey() {
                return "air";
            }
        };
    }

}