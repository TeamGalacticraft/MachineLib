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

import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Persistent I/O configuration component for a formed multiblock machine.
 *
 * <p>This component mirrors the side configuration system used by normal
 * MachineLib machines. It owns a six-face {@link IOConfig}, persists it to
 * component NBT, and marks the formed multiblock dirty whenever a face changes.</p>
 *
 * <p>The six stored faces are MachineLib {@link BlockFace} values, not raw world
 * directions. A later port system can map these logical faces to actual
 * multiblock part positions and world directions.</p>
 */
public final class MultiblockIOConfigComponent implements MultiblockComponent {

    private static final String CONFIGURATION = "Configuration";

    private final IOConfig configuration;

    private MultiblockComponentContext context;

    /**
     * Creates a multiblock I/O component with all faces disabled.
     */
    public MultiblockIOConfigComponent() {
        this.configuration = new IOConfig(this.createFaces());
    }

    /**
     * Gets the MachineLib I/O configuration object.
     *
     * <p>This object can be registered with menu data sync later, exactly like
     * normal configured machine menus do.</p>
     *
     * @return I/O configuration
     */
    public IOConfig configuration() {
        return this.configuration;
    }

    /**
     * Gets the configuration for a logical MachineLib block face.
     *
     * @param face logical face
     * @return configured I/O face
     */
    public IOFace get(final BlockFace face) {
        return this.configuration.get(face);
    }

    /**
     * Sets a logical face configuration.
     *
     * @param face logical face
     * @param type resource type
     * @param flow resource flow
     */
    public void set(
            final BlockFace face,
            final ResourceType type,
            final ResourceFlow flow
    ) {
        this.configuration.get(face).setOption(
                type,
                flow
        );
    }

    /**
     * Resets all logical faces to no I/O.
     */
    public void clear() {
        for (final BlockFace face : BlockFace.values()) {
            this.set(
                    face,
                    ResourceType.NONE,
                    ResourceFlow.BOTH
            );
        }
    }

    /**
     * Loads persisted I/O configuration.
     *
     * @param context component context
     * @param tag saved component tag
     */
    @Override
    public void load(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        this.context = context;

        if (tag.contains(CONFIGURATION, Tag.TAG_BYTE_ARRAY)) {
            this.configuration.readTag(new ByteArrayTag(tag.getByteArray(CONFIGURATION)));
        }
    }

    /**
     * Saves persisted I/O configuration.
     *
     * @param context component context
     * @param tag component tag to write into
     */
    @Override
    public void save(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        tag.put(
                CONFIGURATION,
                this.configuration.createTag()
        );
    }

    /**
     * Stores the active component context.
     *
     * @param context component context
     */
    @Override
    public void onFormed(final MultiblockComponentContext context) {
        this.context = context;
    }

    private IOFace[] createFaces() {
        final IOFace[] faces = new IOFace[BlockFace.values().length];

        for (int i = 0; i < faces.length; i++) {
            faces[i] = new PersistentIOFace();
        }

        return faces;
    }

    private void setChanged() {
        if (this.context != null) {
            this.context.setChanged();
        }
    }

    /**
     * I/O face implementation that marks the parent multiblock component dirty
     * whenever the face option changes.
     */
    private final class PersistentIOFace extends IOFace {

        private PersistentIOFace() {
            super(
                    ResourceType.NONE,
                    ResourceFlow.BOTH
            );
        }

        /**
         * Updates this face's resource type and flow.
         *
         * @param type resource type
         * @param flow resource flow
         */
        @Override
        public void setOption(
                final ResourceType type,
                final ResourceFlow flow
        ) {
            if (this.type == type && this.flow == flow) {
                return;
            }

            this.type = type;
            this.flow = flow;

            MultiblockIOConfigComponent.this.setChanged();
        }

    }

}