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
                        Component.translatable(
                                Constant.TranslationKey.PORT_OPTION_TYPE,
                                port.type().name()
                        ),
                        Component.translatable(
                                Constant.TranslationKey.PORT_OPTION_MODE,
                                port.mode().name()
                        ),
                        Component.translatable(
                                Constant.TranslationKey.PORT_OPTION_TARGET,
                                targetText
                        )
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
}