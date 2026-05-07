package dev.galacticraft.machinelib.api.multiblock;

import dev.galacticraft.machinelib.impl.multiblock.MultiblockMenuOpener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

import java.util.List;

/**
 * Immutable definition of a MachineLib multiblock.
 *
 * <p>A definition describes the structure pattern, formation rules, and optional
 * runtime behaviour for a formed multiblock.</p>
 */
public interface MultiblockDefinition {

    /**
     * Gets the unique registry id for this multiblock.
     *
     * @return multiblock id
     */
    ResourceLocation id();

    /**
     * Gets the multiblock structure pattern.
     *
     * @return pattern
     */
    MultiblockPattern pattern();

    /**
     * Gets the formation rules for this multiblock.
     *
     * @return immutable rule list
     */
    List<FormationRule> rules();

    /**
     * Gets the optional menu factory for this multiblock.
     *
     * @return menu factory, or {@code null}
     */
    default MultiblockMenuFactory menuFactory() {
        return null;
    }

    /**
     * Gets runtime component factories attached to this definition.
     *
     * @return immutable component factory list
     */
    default List<MultiblockComponentFactoryEntry<?>> componentFactories() {
        return List.of();
    }

    /**
     * Handles interaction with one formed part of this multiblock.
     *
     * <p>The default implementation opens the configured multiblock menu if one
     * exists. Otherwise, the interaction passes through to the original clicked
     * block.</p>
     *
     * @param context interaction context
     * @return interaction result
     */
    default InteractionResult usePart(final MultiblockPartInteractionContext context) {
        final MultiblockMenuFactory factory = this.menuFactory();

        if (factory == null) {
            return InteractionResult.PASS;
        }

        return MultiblockMenuOpener.open(
                context,
                factory
        );
    }

}