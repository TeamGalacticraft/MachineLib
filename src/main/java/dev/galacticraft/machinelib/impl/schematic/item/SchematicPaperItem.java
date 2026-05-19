package dev.galacticraft.machinelib.impl.schematic.item;

import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Item used to store and preview a multiblock schematic.
 *
 * <p>The paper stores only the selected multiblock id. The actual structure is resolved from the
 * MachineLib multiblock registry at runtime, which keeps schematic paper small and avoids stale
 * copied structure data.</p>
 */
public final class SchematicPaperItem extends Item {
    private static final String SCHEMATIC_TAG = "Schematic";
    private static final String MULTIBLOCK_ID_TAG = "MultiblockId";

    /**
     * Creates a schematic paper item.
     *
     * @param properties item properties
     */
    public SchematicPaperItem(final Properties properties) {
        super(properties);
    }

    /**
     * Writes a multiblock id onto schematic paper.
     *
     * @param stack schematic paper stack
     * @param multiblockId selected multiblock id
     */
    public static void writeMultiblockId(
            final ItemStack stack,
            final ResourceLocation multiblockId
    ) {
        final CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag();

        final CompoundTag schematic = root.getCompound(SCHEMATIC_TAG);
        schematic.putString(MULTIBLOCK_ID_TAG, multiblockId.toString());
        root.put(SCHEMATIC_TAG, schematic);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    /**
     * Reads the selected multiblock id from schematic paper.
     *
     * @param stack schematic paper stack
     * @return selected multiblock id, or {@code null} if unwritten
     */
    public static @Nullable ResourceLocation readMultiblockId(final ItemStack stack) {
        final CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag();

        if (!root.contains(SCHEMATIC_TAG)) {
            return null;
        }

        final CompoundTag schematic = root.getCompound(SCHEMATIC_TAG);

        if (!schematic.contains(MULTIBLOCK_ID_TAG)) {
            return null;
        }

        return ResourceLocation.tryParse(schematic.getString(MULTIBLOCK_ID_TAG));
    }

    /**
     * Checks whether a stack is written schematic paper.
     *
     * @param stack stack to check
     * @return {@code true} if the stack contains a selected multiblock id
     */
    public static boolean isWritten(final ItemStack stack) {
        return readMultiblockId(stack) != null;
    }

    /**
     * Adds tooltip text.
     *
     * @param stack inspected stack
     * @param context tooltip context
     * @param tooltip tooltip output
     * @param flag tooltip flag
     */
    @Override
    public void appendHoverText(
            final ItemStack stack,
            final TooltipContext context,
            final List<Component> tooltip,
            final @NotNull TooltipFlag flag
    ) {
        final ResourceLocation multiblockId = readMultiblockId(stack);

        if (multiblockId != null) {
            tooltip.add(Component.translatable(
                    Constant.TranslationKey.SCHEMATIC_PAPER_SELECTED,
                    Component.literal(multiblockId.toString()).withStyle(Constant.Text.AQUA_STYLE)
            ).withStyle(Constant.Text.GRAY_STYLE));
            return;
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(this.getDescriptionId() + ".description").withStyle(Constant.Text.GRAY_STYLE));
        } else {
            tooltip.add(Component.translatable(Constant.TranslationKey.PRESS_SHIFT).withStyle(Constant.Text.DARK_GRAY_STYLE));
        }
    }
}