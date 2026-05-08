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