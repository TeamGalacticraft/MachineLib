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

package dev.galacticraft.machinelib.client.api.screen.port;

import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

/**
 * One selectable port configuration option shown in the advanced port sidebar.
 *
 * @param label short option label
 * @param details additional detail lines
 * @param port configured port to apply, or {@code null} when this option clears the port
 */
public record PreviewPortOption(
        Component label,
        List<Component> details,
        ConfiguredMultiblockPort port
) {

    /**
     * Normalizes immutable values.
     */
    public PreviewPortOption {
        details = List.copyOf(details);
    }

    /**
     * Creates a clear/no-port option.
     *
     * @return clear option
     */
    public static PreviewPortOption clear() {
        return new PreviewPortOption(
                Component.translatable(Constant.TranslationKey.PORT_CLEAR),
                List.of(Component.translatable(Constant.TranslationKey.PORT_CLEAR_TOOLTIP)),
                null
        );
    }

    /**
     * Creates an option from a configured multiblock port.
     *
     * @param port configured port
     * @return preview option
     */
    public static PreviewPortOption of(final ConfiguredMultiblockPort port) {
        final String targetPrefix = port.target().group() ? "#" : "";
        final String targetText = targetPrefix + port.target().id();

        return new PreviewPortOption(
                Component.translatable(
                        Constant.TranslationKey.PORT_OPTION,
                        port.type().name(),
                        port.mode().name(),
                        targetText
                ),
                List.of(
                        Component.translatable(Constant.TranslationKey.PORT_OPTION_TYPE, port.type().name()),
                        Component.translatable(Constant.TranslationKey.PORT_OPTION_MODE, port.mode().name()),
                        Component.translatable(Constant.TranslationKey.PORT_OPTION_TARGET, targetText)
                ),
                port
        );
    }

    /**
     * Checks whether this option clears the selected port.
     *
     * @return {@code true} if this is the clear option
     */
    public boolean clearsPort() {
        return this.port == null;
    }

    /**
     * Checks whether this option represents the currently configured selected face.
     *
     * @param face selected preview face
     * @return {@code true} if this option is currently active for the selected face
     */
    public boolean matches(final PreviewPortFace face) {
        if (face == null) {
            return false;
        }

        if (this.port == null) {
            return !face.configured();
        }

        return face.configured()
                && Objects.equals(face.typeName(), this.port.type().name())
                && Objects.equals(face.modeName(), this.port.mode().name())
                && Objects.equals(face.targetName(), this.port.target().id().toString());
    }

    /**
     * Gets a stable grouping key for this option.
     *
     * @return grouping key
     */
    public String groupKey() {
        if (this.port == null) {
            return "clear";
        }

        return this.port.type().name() + ":" + this.port.mode().name();
    }

    /**
     * Gets the expandable group label for this option.
     *
     * @return group label
     */
    public Component groupLabel() {
        if (this.port == null) {
            return this.label;
        }

        return Component.translatable(
                Constant.TranslationKey.PORT_OPTION_GROUP,
                this.port.type().name(),
                this.port.mode().name()
        );
    }

    /**
     * Gets the compact target label shown under a group.
     *
     * @return compact target label
     */
    public Component compactTargetLabel() {
        if (this.port == null) {
            return this.label;
        }

        final String prefix = this.port.target().group() ? "#" : "";

        return Component.translatable(
                Constant.TranslationKey.PORT_OPTION_TARGET_COMPACT,
                prefix,
                this.port.target().id().getPath()
        );
    }

    /**
     * Gets the full target label shown as a tooltip.
     *
     * @return full target label
     */
    public Component fullTargetLabel() {
        if (this.port == null) {
            return this.label;
        }

        final String prefix = this.port.target().group() ? "#" : "";

        return Component.translatable(
                Constant.TranslationKey.PORT_OPTION_TARGET_FULL,
                prefix,
                this.port.target().id().toString()
        );
    }
}