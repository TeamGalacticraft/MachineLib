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

package dev.galacticraft.machinelib.api.multiblock.port;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines which kind of port may be configured on one multiblock face.
 *
 * @param face allowed multiblock face
 * @param types allowed port types
 * @param modes allowed port modes
 * @param targets allowed exact endpoint or group targets
 */
public record MultiblockPortRule(
        MultiblockPortFace face,
        Set<MultiblockPortType> types,
        Set<MultiblockPortMode> modes,
        Set<MultiblockPortTarget> targets
) {

    /**
     * Creates a port rule.
     *
     * @param face allowed multiblock face
     * @param types allowed port types
     * @param modes allowed port modes
     * @param targets allowed exact endpoint or group targets
     */
    public MultiblockPortRule {
        types = Set.copyOf(types);
        modes = Set.copyOf(modes);
        targets = Set.copyOf(targets);
    }

    /**
     * Checks whether this rule allows the supplied configured port.
     *
     * @param port configured port
     * @return {@code true} if this rule allows the port
     */
    public boolean allows(final ConfiguredMultiblockPort port) {
        return this.face.equals(port.face())
                && this.types.contains(port.type())
                && this.modes.contains(port.mode())
                && this.targets.contains(port.target());
    }

    /**
     * Creates a new builder for a port rule.
     *
     * @param face port face
     * @return builder
     */
    public static Builder builder(final MultiblockPortFace face) {
        return new Builder(face);
    }

    /**
     * Mutable builder for {@link MultiblockPortRule}.
     */
    public static final class Builder {

        private final MultiblockPortFace face;
        private Set<MultiblockPortType> types = Set.of();
        private Set<MultiblockPortMode> modes = Set.of();
        private Set<MultiblockPortTarget> targets = Set.of();

        private Builder(final MultiblockPortFace face) {
            this.face = face;
        }

        /**
         * Sets the allowed port types.
         *
         * @param types allowed types
         * @return this builder
         */
        public Builder types(final MultiblockPortType... types) {
            this.types = Set.of(types);
            return this;
        }

        /**
         * Sets the allowed port modes.
         *
         * @param modes allowed modes
         * @return this builder
         */
        public Builder modes(final MultiblockPortMode... modes) {
            this.modes = Set.of(modes);
            return this;
        }

        /**
         * Sets the allowed exact target ids.
         *
         * @param targets allowed target ids
         * @return this builder
         */
        public Builder targetIds(final ResourceLocation... targets) {
            final Set<MultiblockPortTarget> builder = new HashSet<>();

            for (final ResourceLocation target : targets) {
                builder.add(MultiblockPortTarget.id(target));
            }

            this.targets = builder;
            return this;
        }

        /**
         * Sets the allowed grouped target ids.
         *
         * @param targets allowed group ids
         * @return this builder
         */
        public Builder targetGroups(final ResourceLocation... targets) {
            final Set<MultiblockPortTarget> builder = new HashSet<>();

            for (final ResourceLocation target : targets) {
                builder.add(MultiblockPortTarget.group(target));
            }

            this.targets = builder;
            return this;
        }

        /**
         * Builds the immutable port rule.
         *
         * @return port rule
         */
        public MultiblockPortRule build() {
            return new MultiblockPortRule(
                    this.face,
                    this.types,
                    this.modes,
                    this.targets
            );
        }

    }

}