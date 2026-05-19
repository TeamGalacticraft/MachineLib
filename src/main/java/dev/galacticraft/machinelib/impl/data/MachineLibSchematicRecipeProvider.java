package dev.galacticraft.machinelib.impl.data;

import dev.galacticraft.machinelib.impl.schematic.MachineLibSchematicContent;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Generates crafting recipes for MachineLib's schematic helper content.
 */
public final class MachineLibSchematicRecipeProvider extends FabricRecipeProvider {
    /**
     * Creates the schematic recipe provider.
     *
     * @param output datagen output
     * @param registries registry lookup future
     */
    public MachineLibSchematicRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    /**
     * Generates schematic crafting recipes.
     *
     * @param output recipe output
     */
    @Override
    public void buildRecipes(@NotNull RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MachineLibSchematicContent.schematicPaper())
                .pattern(" R ")
                .pattern("PPP")
                .pattern(" R ")
                .define('R', Items.REDSTONE)
                .define('P', Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, MachineLibSchematicContent.schematicWorkbench())
                .pattern("IRI")
                .pattern("WCW")
                .pattern("WWW")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('W', ItemTags.PLANKS)
                .define('C', Blocks.CRAFTING_TABLE)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, MachineLibSchematicContent.schematicProjector())
                .pattern("GLG")
                .pattern("RCR")
                .pattern("III")
                .define('G', Blocks.GLASS)
                .define('L', Items.GLOWSTONE_DUST)
                .define('R', Items.REDSTONE)
                .define('C', Items.COPPER_INGOT)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_glowstone_dust", has(Items.GLOWSTONE_DUST))
                .save(output);
    }
}