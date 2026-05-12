package dev.galacticraft.machinelib.api.multiblock.port.conflict;

import dev.galacticraft.machinelib.api.multiblock.port.conflict.rules.OnlyOnePortPerBlockConflictRule;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.rules.OnlyOneUniquePortPerMultiblockConflictRule;

/**
 * Factory for MachineLib's built-in multiblock port conflict rules.
 *
 * <p>These rules are not applied automatically. Developers opt into them by
 * adding scoped conflict rule assignments to a multiblock definition.</p>
 */
public final class MultiblockPortConflictRules {

    private MultiblockPortConflictRules() {

    }

    /**
     * Creates a rule that allows only one configured port per block.
     *
     * @return one-port-per-block rule
     */
    public static MultiblockPortConflictRule onlyOnePortPerBlock() {
        return new OnlyOnePortPerBlockConflictRule();
    }

    /**
     * Creates a rule that allows only one matching type/mode/target route per
     * multiblock.
     *
     * @return unique-port-route rule
     */
    public static MultiblockPortConflictRule onlyOneUniquePortPerMultiblock() {
        return new OnlyOneUniquePortPerMultiblockConflictRule();
    }
}