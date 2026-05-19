/*
 * Copyright (c) 2021-2025 Team Galacticraft
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