package dev.galacticraft.machinelib.impl.multiblock.detection;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CompiledVariantBucket {

    private final Block centerBlock;

    private final List<CompiledLocalVariant> variants = new ArrayList<>();

    public CompiledVariantBucket(final Block centerBlock) {
        this.centerBlock = centerBlock;
    }

    public Block centerBlock() {
        return this.centerBlock;
    }

    public void add(final CompiledLocalVariant variant) {
        this.variants.add(variant);
    }

    public void sort() {
        this.variants.sort(
                Comparator.comparingInt(CompiledLocalVariant::specificity).reversed()
        );
    }

    public void test(
            final ServerLevel level,
            final BlockPos changedPos,
            final MultiblockValidationQueue validationQueue
    ) {
        if (this.variants.isEmpty()) {
            return;
        }

        final BlockState upState = level.getBlockState(changedPos.above());
        final BlockState downState = level.getBlockState(changedPos.below());
        final BlockState northState = level.getBlockState(changedPos.north());
        final BlockState southState = level.getBlockState(changedPos.south());
        final BlockState eastState = level.getBlockState(changedPos.east());
        final BlockState westState = level.getBlockState(changedPos.west());

        for (final CompiledLocalVariant variant : this.variants) {
            if (!variant.matches(
                    level,
                    changedPos,
                    upState,
                    downState,
                    northState,
                    southState,
                    eastState,
                    westState
            )) {
                continue;
            }

            validationQueue.enqueue(
                    new MultiblockCandidate(
                            level,
                            variant.resolveOrigin(changedPos),
                            variant.orientation(),
                            variant.definition()
                    )
            );
        }
    }

}