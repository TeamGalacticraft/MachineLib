package dev.galacticraft.machinelib.impl.data;

import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.data.model.MachineModelGenerator;
import dev.galacticraft.machinelib.client.api.model.MachineTextureBase;
import dev.galacticraft.machinelib.client.api.model.TextureProvider;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.schematic.MachineLibSchematicContent;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;

/**
 * Generates blockstate and model JSON for MachineLib's schematic helper content.
 *
 * <p>The schematic workbench and projector use MachineLib's generated machine model format.
 * The schematic paper uses a normal generated item model.</p>
 */
public final class MachineLibSchematicModelProvider extends FabricModelProvider {
    /**
     * Creates the schematic model provider.
     *
     * @param output datagen output
     */
    public MachineLibSchematicModelProvider(FabricDataOutput output) {
        super(output);
    }

    /**
     * Generates schematic machine block models and blockstates.
     *
     * @param gen block model generator
     */
    @Override
    public void generateBlockStateModels(BlockModelGenerators gen) {
        MachineModelGenerator.setupMachineBaseTextures(
                gen,
                Constant.MOD_ID,
                MachineTextureBase.prefixed(Constant.MOD_ID, "block/machine")
        );

        ResourceLocation workbench = MachineModelGenerator.generateMachineModel(
                gen,
                MachineLibSchematicContent.schematicWorkbench(),
                TextureProvider.none()
        );

        ResourceLocation projector = MachineModelGenerator.generateMachineModel(
                gen,
                MachineLibSchematicContent.schematicProjector(),
                TextureProvider.none()
        );

        gen.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(MachineLibSchematicContent.schematicWorkbench())
                        .with(BlockModelGenerators.createBooleanModelDispatch(MachineBlock.ACTIVE, workbench, workbench))
        );

        gen.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(MachineLibSchematicContent.schematicProjector())
                        .with(BlockModelGenerators.createBooleanModelDispatch(MachineBlock.ACTIVE, projector, projector))
        );

        gen.skipAutoItemBlock(MachineLibSchematicContent.schematicWorkbench());
        gen.skipAutoItemBlock(MachineLibSchematicContent.schematicProjector());
    }

    /**
     * Generates schematic item models.
     *
     * @param gen item model generator
     */
    @Override
    public void generateItemModels(ItemModelGenerators gen) {
        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(MachineLibSchematicContent.schematicPaper()),
                TextureMapping.layer0(Constant.id("item/schematic_paper")),
                gen.output
        );
    }
}