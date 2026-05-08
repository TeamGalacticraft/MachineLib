package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.impl.multiblock.port_lookup.MultiblockEnergyPortLookup;
import dev.galacticraft.machinelib.impl.multiblock.port_lookup.MultiblockFluidPortLookup;
import dev.galacticraft.machinelib.impl.multiblock.port_lookup.MultiblockItemPortLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import team.reborn.energy.api.EnergyStorage;

import java.util.Collection;

/**
 * Registers Fabric and Reborn Energy provider lookups for formed multiblock
 * parts.
 */
public final class MultiblockPortProviders {

    private MultiblockPortProviders() {

    }

    /**
     * Registers item provider lookup support for blocks that may appear inside
     * formed multiblocks with item ports.
     *
     * @param blocks blocks that should expose configured multiblock item ports
     */
    public static void registerItemProviders(final Collection<Block> blocks) {
        if (blocks.isEmpty()) {
            MultiblockPortDebug.LOGGER.info("No item port provider blocks collected.");
            return;
        }

        MultiblockPortDebug.LOGGER.info("Registering item port providers for {} blocks: {}", blocks.size(), describe(blocks));

        ItemStorage.SIDED.registerForBlocks(
                (world, pos, state, blockEntity, side) -> {
                    if (!(world instanceof ServerLevel serverLevel) || side == null) {
                        return null;
                    }

                    return MultiblockItemPortLookup.find(serverLevel, pos, side);
                },
                blocks.toArray(Block[]::new)
        );
    }

    /**
     * Registers fluid provider lookup support for blocks that may appear inside
     * formed multiblocks with fluid ports.
     *
     * @param blocks blocks that should expose configured multiblock fluid ports
     */
    public static void registerFluidProviders(final Collection<Block> blocks) {
        if (blocks.isEmpty()) {
            MultiblockPortDebug.LOGGER.info("No fluid port provider blocks collected.");
            return;
        }

        MultiblockPortDebug.LOGGER.info("Registering fluid port providers for {} blocks: {}", blocks.size(), describe(blocks));

        FluidStorage.SIDED.registerForBlocks(
                (world, pos, state, blockEntity, side) -> {
                    if (!(world instanceof ServerLevel serverLevel) || side == null) {
                        return null;
                    }

                    return MultiblockFluidPortLookup.find(serverLevel, pos, side);
                },
                blocks.toArray(Block[]::new)
        );
    }

    /**
     * Registers energy provider lookup support for blocks that may appear inside
     * formed multiblocks with energy ports.
     *
     * @param blocks blocks that should expose configured multiblock energy ports
     */
    public static void registerEnergyProviders(final Collection<Block> blocks) {
        if (blocks.isEmpty()) {
            MultiblockPortDebug.LOGGER.info("No energy port provider blocks collected.");
            return;
        }

        MultiblockPortDebug.LOGGER.info("Registering energy port providers for {} blocks: {}", blocks.size(), describe(blocks));

        EnergyStorage.SIDED.registerForBlocks(
                (world, pos, state, blockEntity, side) -> {
                    if (!(world instanceof ServerLevel serverLevel) || side == null) {
                        return null;
                    }

                    return MultiblockEnergyPortLookup.find(serverLevel, pos, side);
                },
                blocks.toArray(Block[]::new)
        );
    }

    /**
     * Creates a readable block list for provider registration logs.
     *
     * @param blocks registered provider blocks
     * @return readable block id list
     */
    private static String describe(final Collection<Block> blocks) {
        final StringBuilder builder = new StringBuilder();

        for (final Block block : blocks) {
            if (!builder.isEmpty()) {
                builder.append(", ");
            }

            builder.append(BuiltInRegistries.BLOCK.getKey(block));
        }

        return builder.toString();
    }
}