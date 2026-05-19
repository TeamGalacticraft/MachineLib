package dev.galacticraft.machinelib.impl.schematic.menu;

import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.impl.multiblock.MachineLibMultiblocks;
import dev.galacticraft.machinelib.impl.schematic.MachineLibSchematicContent;
import dev.galacticraft.machinelib.impl.schematic.block.entity.SchematicWorkbenchBlockEntity;
import dev.galacticraft.machinelib.impl.schematic.item.SchematicPaperItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Menu for the schematic workbench.
 *
 * <p>The menu contains one real machine slot for schematic paper. Multiblock selection is handled
 * by the client screen and sent to the server through a packet when the player presses write.</p>
 */
public final class SchematicWorkbenchMenu extends MachineMenu<SchematicWorkbenchBlockEntity> {
    public static final int PAPER_SLOT = 0;

    /**
     * Creates the server-side schematic workbench menu.
     *
     * @param syncId menu sync id
     * @param player player opening the menu
     * @param machine backing workbench block entity
     */
    public SchematicWorkbenchMenu(
            final int syncId,
            final @NotNull Player player,
            final @NotNull SchematicWorkbenchBlockEntity machine
    ) {
        super(MachineLibSchematicContent.schematicWorkbenchMenu(), syncId, player, machine);
    }

    /**
     * Creates the client-side schematic workbench menu.
     *
     * @param type menu type
     * @param syncId menu sync id
     * @param inventory player inventory
     * @param pos block position
     * @param invX player inventory x
     * @param invY player inventory y
     */
    public SchematicWorkbenchMenu(
            final MenuType<? extends SchematicWorkbenchMenu> type,
            final int syncId,
            final @NotNull Inventory inventory,
            final @NotNull BlockPos pos,
            final int invX,
            final int invY
    ) {
        super(type, syncId, inventory, pos, invX, invY);
    }

    /**
     * Writes the selected multiblock id onto the inserted schematic paper.
     *
     * @param multiblockId selected multiblock id
     * @return {@code true} if writing succeeded
     */
    public boolean writeSelectedSchematic(final ResourceLocation multiblockId) {
        if (MachineLibMultiblocks.getDefinition(multiblockId) == null) {
            return false;
        }

        final Slot slot = this.getSlot(PAPER_SLOT);
        final ItemStack stack = slot.getItem();

        if (!stack.is(MachineLibSchematicContent.schematicPaper())) {
            return false;
        }

        SchematicPaperItem.writeMultiblockId(stack, multiblockId);
        slot.setChanged();
        this.broadcastChanges();
        return true;
    }

    /**
     * Checks whether schematic paper is inserted.
     *
     * @return {@code true} if paper is present
     */
    public boolean hasPaper() {
        return this.getSlot(PAPER_SLOT).getItem().is(MachineLibSchematicContent.schematicPaper());
    }
}