package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public final class FormationResult {

    private final boolean success;
    private final Component message;
    private final BlockPos errorPos;

    private FormationResult(
            final boolean success,
            final Component message,
            final BlockPos errorPos
    ) {
        this.success = success;
        this.message = message;
        this.errorPos = errorPos;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public Component message() {
        return this.message;
    }

    public BlockPos errorPos() {
        return this.errorPos;
    }

    public static FormationResult success() {
        return new FormationResult(
                true,
                null,
                null
        );
    }

    public static FormationResult failure(final Component message) {
        return new FormationResult(
                false,
                message,
                null
        );
    }

    public static FormationResult failure(
            final Component message,
            final BlockPos errorPos
    ) {
        return new FormationResult(
                false,
                message,
                errorPos
        );
    }

}