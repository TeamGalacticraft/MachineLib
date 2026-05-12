package dev.galacticraft.machinelib.api.multiblock.port.conflict;

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenu;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.client.api.screen.port.PreviewPortFace;
import dev.galacticraft.machinelib.client.api.screen.port.PreviewPortOption;

/**
 * Context passed to a multiblock port conflict rule.
 *
 * @param definition multiblock definition being configured
 * @param menu active multiblock menu
 * @param face logical pattern-relative port face
 * @param previewFace client preview face
 * @param option candidate option being validated
 */
public record MultiblockPortConflictContext(
        MultiblockDefinition definition,
        MultiblockMachineMenu menu,
        MultiblockPortFace face,
        PreviewPortFace previewFace,
        PreviewPortOption option
) {

    /**
     * Gets the candidate configured port.
     *
     * @return configured port, or {@code null} when the option clears the port
     */
    public ConfiguredMultiblockPort port() {
        return this.option.port();
    }
}