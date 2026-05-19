package dev.galacticraft.machinelib.impl.data;

import dev.galacticraft.machinelib.impl.schematic.MachineLibSchematicContent;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 * Generates block loot tables for MachineLib's schematic helper blocks.
 */
public final class MachineLibSchematicLootTableProvider extends FabricBlockLootTableProvider {
    /**
     * Creates the schematic loot table provider.
     *
     * @param output datagen output
     * @param registries registry lookup future
     */
    public MachineLibSchematicLootTableProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    /**
     * Generates schematic block drops.
     */
    @Override
    public void generate() {
        this.dropSelf(MachineLibSchematicContent.schematicWorkbench());
        this.dropSelf(MachineLibSchematicContent.schematicProjector());
    }
}