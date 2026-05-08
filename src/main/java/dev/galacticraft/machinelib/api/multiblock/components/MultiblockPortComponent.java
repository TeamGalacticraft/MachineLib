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

import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortMode;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortRule;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortTarget;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Persistent runtime component that stores configured multiblock ports.
 *
 * <p>Port rules are defined by the immutable multiblock definition. This
 * component stores the player-selected configured ports for a formed instance.
 * A configured port maps one exposed face of one multiblock part to an internal
 * target id or target group.</p>
 */
public final class MultiblockPortComponent implements MultiblockComponent {

    private static final String PORTS = "Ports";

    private static final String X = "X";
    private static final String Y = "Y";
    private static final String Z = "Z";
    private static final String FACE = "Face";
    private static final String TYPE = "Type";
    private static final String MODE = "Mode";
    private static final String TARGET = "Target";
    private static final String TARGET_GROUP = "TargetGroup";

    private final Map<MultiblockPortFace, ConfiguredMultiblockPort> ports =
            new LinkedHashMap<>();

    private MultiblockComponentContext context;
    private boolean loaded;

    /**
     * Gets all configured ports.
     *
     * @return immutable configured port list
     */
    public List<ConfiguredMultiblockPort> ports() {
        return List.copyOf(this.ports.values());
    }

    /**
     * Gets the configured port on a face.
     *
     * @param face multiblock face
     * @return configured port, if present
     */
    public Optional<ConfiguredMultiblockPort> portAt(final MultiblockPortFace face) {
        return Optional.ofNullable(this.ports.get(face));
    }

    /**
     * Gets all port rules allowed for a face.
     *
     * @param face multiblock face
     * @return matching rules
     */
    public List<MultiblockPortRule> rulesFor(final MultiblockPortFace face) {
        if (this.context == null) {
            return List.of();
        }

        final List<MultiblockPortRule> rules = new ArrayList<>();

        for (final MultiblockPortRule rule : this.context.definition().portRules()) {
            if (rule.face().equals(face)) {
                rules.add(rule);
            }
        }

        return rules;
    }

    /**
     * Checks whether the definition allows a configured port.
     *
     * @param port configured port
     * @return {@code true} if the port is allowed
     */
    public boolean isAllowed(final ConfiguredMultiblockPort port) {
        if (this.context == null) {
            return false;
        }

        for (final MultiblockPortRule rule : this.context.definition().portRules()) {
            if (rule.allows(port)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sets or replaces a configured port.
     *
     * @param port configured port
     * @return {@code true} if the port was accepted
     */
    public boolean setPort(final ConfiguredMultiblockPort port) {
        if (!this.isAllowed(port)) {
            return false;
        }

        this.ports.put(
                port.face(),
                port
        );

        this.setChanged();
        return true;
    }

    /**
     * Removes the configured port on a face.
     *
     * @param face multiblock face
     * @return {@code true} if a port was removed
     */
    public boolean removePort(final MultiblockPortFace face) {
        if (this.ports.remove(face) == null) {
            return false;
        }

        this.setChanged();
        return true;
    }

    /**
     * Clears all configured ports.
     */
    public void clearPorts() {
        if (this.ports.isEmpty()) {
            return;
        }

        this.ports.clear();
        this.setChanged();
    }

    /**
     * Marks this component as changed.
     */
    public void setChanged() {
        if (this.context != null) {
            this.context.setChanged();
        }
    }

    /**
     * Loads configured ports from persistent NBT.
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
        this.loaded = true;
        this.ports.clear();

        if (!tag.contains(PORTS, Tag.TAG_LIST)) {
            return;
        }

        final ListTag list = tag.getList(
                PORTS,
                Tag.TAG_COMPOUND
        );

        for (int i = 0; i < list.size(); i++) {
            final Optional<ConfiguredMultiblockPort> port =
                    this.readPort(list.getCompound(i));

            if (port.isPresent() && this.isAllowed(port.get())) {
                this.ports.put(
                        port.get().face(),
                        port.get()
                );
            }
        }
    }

    /**
     * Saves configured ports to persistent NBT.
     *
     * @param context component context
     * @param tag component tag to write into
     */
    @Override
    public void save(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        if (this.ports.isEmpty()) {
            return;
        }

        final ListTag list = new ListTag();

        for (final ConfiguredMultiblockPort port : this.ports.values()) {
            list.add(this.writePort(port));
        }

        tag.put(
                PORTS,
                list
        );
    }

    /**
     * Initializes default ports when the formed machine is first created.
     *
     * <p>If this component was loaded from persistent data, saved ports are kept.
     * Otherwise, valid default ports from the definition are installed.</p>
     *
     * @param context component context
     */
    @Override
    public void onFormed(final MultiblockComponentContext context) {
        this.context = context;

        if (this.loaded || !this.ports.isEmpty()) {
            return;
        }

        this.addDefaults(context.definition().defaultPorts());
    }

    private void addDefaults(final Collection<ConfiguredMultiblockPort> defaults) {
        boolean changed = false;

        for (final ConfiguredMultiblockPort port : defaults) {
            if (!this.isAllowed(port)) {
                continue;
            }

            this.ports.put(
                    port.face(),
                    port
            );

            changed = true;
        }

        if (changed) {
            this.setChanged();
        }
    }

    private CompoundTag writePort(final ConfiguredMultiblockPort port) {
        final CompoundTag tag = new CompoundTag();
        final BlockPos relativePos = port.face().relativePos();

        tag.putInt(
                X,
                relativePos.getX()
        );
        tag.putInt(
                Y,
                relativePos.getY()
        );
        tag.putInt(
                Z,
                relativePos.getZ()
        );
        tag.putString(
                FACE,
                port.face().face().getName()
        );
        tag.putString(
                TYPE,
                port.type().name()
        );
        tag.putString(
                MODE,
                port.mode().name()
        );
        tag.putString(
                TARGET,
                port.target().id().toString()
        );
        tag.putBoolean(
                TARGET_GROUP,
                port.target().group()
        );

        return tag;
    }

    private Optional<ConfiguredMultiblockPort> readPort(final CompoundTag tag) {
        final Direction face = Direction.byName(tag.getString(FACE));

        if (face == null) {
            return Optional.empty();
        }

        final Optional<MultiblockPortType> type = this.readEnum(
                MultiblockPortType.class,
                tag.getString(TYPE)
        );

        final Optional<MultiblockPortMode> mode = this.readEnum(
                MultiblockPortMode.class,
                tag.getString(MODE)
        );

        final ResourceLocation targetId = ResourceLocation.tryParse(
                tag.getString(TARGET)
        );

        if (type.isEmpty() || mode.isEmpty() || targetId == null) {
            return Optional.empty();
        }

        final MultiblockPortFace portFace = new MultiblockPortFace(
                new BlockPos(
                        tag.getInt(X),
                        tag.getInt(Y),
                        tag.getInt(Z)
                ),
                face
        );

        final MultiblockPortTarget target = new MultiblockPortTarget(
                targetId,
                tag.getBoolean(TARGET_GROUP)
        );

        return Optional.of(new ConfiguredMultiblockPort(
                portFace,
                type.get(),
                mode.get(),
                target
        ));
    }

    private <T extends Enum<T>> Optional<T> readEnum(
            final Class<T> type,
            final String name
    ) {
        try {
            return Optional.of(Enum.valueOf(
                    type,
                    name
            ));
        } catch (final IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

}