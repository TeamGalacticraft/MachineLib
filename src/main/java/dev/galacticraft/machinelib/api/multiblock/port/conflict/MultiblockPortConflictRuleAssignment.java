package dev.galacticraft.machinelib.api.multiblock.port.conflict;

import java.util.Objects;

/**
 * Assigns one conflict rule to one scope.
 *
 * @param scope scope where the rule applies
 * @param rule conflict rule
 */
public record MultiblockPortConflictRuleAssignment(
        MultiblockPortConflictScope scope,
        MultiblockPortConflictRule rule
) {

    /**
     * Validates assignment values.
     */
    public MultiblockPortConflictRuleAssignment {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(rule, "rule");
    }

    /**
     * Checks whether this assignment applies to a validation context.
     *
     * @param context validation context
     * @return {@code true} if this assignment applies
     */
    public boolean matches(final MultiblockPortConflictContext context) {
        return this.scope.matches(context);
    }
}