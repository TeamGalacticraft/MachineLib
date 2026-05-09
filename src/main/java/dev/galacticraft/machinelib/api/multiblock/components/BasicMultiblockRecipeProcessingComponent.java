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

package dev.galacticraft.machinelib.api.multiblock.components;

import dev.galacticraft.machinelib.api.storage.SlottedStorageAccess;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Simple item-input/item-output recipe processor for formed multiblocks.
 *
 * <p>This mirrors {@code BasicRecipeMachineBlockEntity}: it uses a contiguous
 * input slot range, a contiguous output slot range, consumes one item from each
 * input slot when crafting completes, and inserts the assembled recipe output
 * into output slots.</p>
 *
 * @param <I> recipe input type
 * @param <R> recipe type
 */
public abstract class BasicMultiblockRecipeProcessingComponent<I extends RecipeInput, R extends Recipe<I>>
        extends MultiblockRecipeProcessingComponent<I, R> {

    private final int inputSlotsStart;
    private final int inputSlotsLen;
    private final int outputSlotsStart;
    private final int outputSlotsLen;
    private final Function<ItemResourceSlot[], I> inputFactory;

    private SlottedStorageAccess<Item, ItemResourceSlot> inputSlots;
    private SlottedStorageAccess<Item, ItemResourceSlot> outputSlots;

    /**
     * Creates a simple multiblock recipe processor.
     *
     * @param recipeType recipe type
     * @param inputSlotsStart first input slot index
     * @param inputSlotsLen number of input slots
     * @param outputSlotsStart first output slot index
     * @param outputSlotsLen number of output slots
     * @param inputFactory recipe input factory
     */
    protected BasicMultiblockRecipeProcessingComponent(
            final RecipeType<R> recipeType,
            final int inputSlotsStart,
            final int inputSlotsLen,
            final int outputSlotsStart,
            final int outputSlotsLen,
            final Function<ItemResourceSlot[], I> inputFactory
    ) {
        super(recipeType);

        this.inputSlotsStart = inputSlotsStart;
        this.inputSlotsLen = inputSlotsLen;
        this.outputSlotsStart = outputSlotsStart;
        this.outputSlotsLen = outputSlotsLen;
        this.inputFactory = inputFactory;
    }

    @Override
    public void onFormed(final dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext context) {
        super.onFormed(context);

        this.inputSlots = this.storage().itemStorage().subStorage(
                this.inputSlotsStart,
                this.inputSlotsLen
        );

        this.outputSlots = this.storage().itemStorage().subStorage(
                this.outputSlotsStart,
                this.outputSlotsLen
        );
    }

    @Override
    protected @NotNull I craftingInv() {
        final ItemResourceSlot[] slots = new ItemResourceSlot[this.inputSlotsLen];

        for (int i = 0; i < this.inputSlotsLen; i++) {
            slots[i] = this.storage().itemStorage().slot(this.inputSlotsStart + i);
        }

        return this.inputFactory.apply(slots);
    }

    @Override
    protected void outputStacks(final @NotNull RecipeHolder<R> recipe) {
        final ItemStack assembled = recipe.value().assemble(
                this.craftingInv(),
                this.context().level().registryAccess()
        );

        this.outputSlots.insertMatching(
                assembled.getItem(),
                assembled.getComponentsPatch(),
                assembled.getCount()
        );
    }

    @Override
    protected boolean canOutputStacks(final @NotNull RecipeHolder<R> recipe) {
        final ItemStack assembled = recipe.value().assemble(
                this.craftingInv(),
                this.context().level().registryAccess()
        );

        return this.outputSlots.canInsert(
                assembled.getItem(),
                assembled.getComponentsPatch(),
                assembled.getCount()
        );
    }

    @Override
    protected void extractCraftingMaterials(final @NotNull RecipeHolder<R> recipe) {
        for (final ItemResourceSlot slot : this.inputSlots) {
            slot.consumeOne();
        }
    }

    @Override
    protected int decreaseProgressAmount() {
        return 1;
    }
}