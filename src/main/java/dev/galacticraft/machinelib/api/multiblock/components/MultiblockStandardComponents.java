package dev.galacticraft.machinelib.api.multiblock.components;

import dev.galacticraft.machinelib.api.multiblock.MultiblockBuilder;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.resources.ResourceLocation;

/**
 * Stable component ids and registration helpers for MachineLib's standard
 * multiblock components.
 */
public final class MultiblockStandardComponents {

    public static final ResourceLocation SECURITY =
            Constant.id("security");

    public static final ResourceLocation REDSTONE =
            Constant.id("redstone");

    public static final ResourceLocation STATE =
            Constant.id("state");

    public static final ResourceLocation IO_CONFIG =
            Constant.id("io_config");

    public static final ResourceLocation STORAGE =
            Constant.id("storage");

    private MultiblockStandardComponents() {

    }

    /**
     * Adds the standard configured-machine components to a multiblock builder.
     *
     * <p>This should be used by multiblocks that want MachineLib-style security,
     * redstone mode, machine state, and side configuration.</p>
     *
     * @param builder builder to modify
     * @return the same builder
     */
    public static MultiblockBuilder configured(final MultiblockBuilder builder) {
        return builder
                .component(
                        SECURITY,
                        MultiblockSecurityComponent.class,
                        context -> new MultiblockSecurityComponent()
                )
                .component(
                        REDSTONE,
                        MultiblockRedstoneComponent.class,
                        context -> new MultiblockRedstoneComponent()
                )
                .component(
                        STATE,
                        MultiblockStateComponent.class,
                        context -> new MultiblockStateComponent()
                )
                .component(
                        IO_CONFIG,
                        MultiblockIOConfigComponent.class,
                        context -> new MultiblockIOConfigComponent()
                );
    }

    /**
     * Adds a persistent storage component to a multiblock builder.
     *
     * <p>This should be used by multiblocks that need MachineLib item, fluid, or
     * energy storage. The storage component is separate from
     * {@link #configured(MultiblockBuilder)}, because different machines need
     * different storage layouts.</p>
     *
     * @param builder builder to modify
     * @param spec storage specification
     * @return the same builder
     */
    public static MultiblockBuilder storage(
            final MultiblockBuilder builder,
            final StorageSpec spec
    ) {
        return builder.component(
                STORAGE,
                MultiblockStorageComponent.class,
                context -> new MultiblockStorageComponent(spec)
        );
    }
}