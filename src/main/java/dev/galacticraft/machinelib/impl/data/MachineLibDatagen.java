package dev.galacticraft.machinelib.impl.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Data generator entrypoint for MachineLib's own built-in resources.
 *
 * <p>This is separate from the MachineLib testmod datagen. The testmod generates resources for
 * example machines only, while this entrypoint generates resources that should ship with the
 * MachineLib jar itself.</p>
 */
public final class MachineLibDatagen implements DataGeneratorEntrypoint {
    /**
     * Registers MachineLib's datagen providers.
     *
     * @param generator the Fabric data generator
     */
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(MachineLibSchematicModelProvider::new);
        pack.addProvider(MachineLibSchematicRecipeProvider::new);
        pack.addProvider(MachineLibSchematicLootTableProvider::new);
    }
}