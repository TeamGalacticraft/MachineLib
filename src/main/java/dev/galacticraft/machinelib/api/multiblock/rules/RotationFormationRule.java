package dev.galacticraft.machinelib.api.multiblock.rules;

import dev.galacticraft.machinelib.api.multiblock.FormationContext;
import dev.galacticraft.machinelib.api.multiblock.FormationResult;
import dev.galacticraft.machinelib.api.multiblock.FormationRule;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.EnumSet;

public final class RotationFormationRule implements FormationRule {

    private final EnumSet<MultiblockOrientation> allowed;

    private RotationFormationRule(final EnumSet<MultiblockOrientation> allowed) {
        this.allowed = allowed.clone();
    }

    @Override
    public FormationResult check(final FormationContext context) {
        if (this.allowed.contains(context.orientation())) {
            return FormationResult.success();
        }

        return FormationResult.failure(
                Component.literal("Invalid multiblock orientation: " + context.orientation()),
                context.origin()
        );
    }

    public static RotationFormationRule allow(
            final MultiblockOrientation first,
            final MultiblockOrientation... rest
    ) {
        return new RotationFormationRule(EnumSet.of(first, rest));
    }

    public static RotationFormationRule allow(final Collection<MultiblockOrientation> orientations) {
        if (orientations.isEmpty()) {
            throw new IllegalArgumentException("Allowed orientation collection cannot be empty");
        }

        return new RotationFormationRule(EnumSet.copyOf(orientations));
    }

    public static RotationFormationRule deny(
            final MultiblockOrientation first,
            final MultiblockOrientation... rest
    ) {
        final EnumSet<MultiblockOrientation> allowed = MultiblockOrientation.all();

        allowed.remove(first);

        for (final MultiblockOrientation orientation : rest) {
            allowed.remove(orientation);
        }

        return new RotationFormationRule(allowed);
    }

    public static RotationFormationRule horizontalOnly() {
        return new RotationFormationRule(MultiblockOrientation.horizontal());
    }

    public static RotationFormationRule any() {
        return new RotationFormationRule(MultiblockOrientation.all());
    }

}