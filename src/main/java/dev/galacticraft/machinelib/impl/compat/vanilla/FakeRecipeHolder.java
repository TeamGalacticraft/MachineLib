package dev.galacticraft.machinelib.impl.compat.vanilla;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @see net.minecraft.world.inventory.RecipeHolder
 */
public interface FakeRecipeHolder {
    @Nullable Set<ResourceLocation> takeRecipes();

    void recipeCrafted(@NotNull ResourceLocation id);
}
