package dev.galacticraft.machinelib.api.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

public interface MachineRecipe<C extends Container> extends Recipe<C> {
    int getProcessingTime();


}
