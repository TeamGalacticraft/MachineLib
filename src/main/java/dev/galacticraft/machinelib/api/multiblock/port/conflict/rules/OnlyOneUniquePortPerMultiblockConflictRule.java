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

package dev.galacticraft.machinelib.api.multiblock.port.conflict.rules;

import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.AbstractMultiblockPortConflictRule;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.MultiblockPortConflictContext;
import dev.galacticraft.machinelib.api.multiblock.port.conflict.MultiblockPortConflictResult;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.network.chat.Component;

/**
 * Conflict rule that allows only one matching type/mode/target route per multiblock.
 *
 * <p>This does not prevent multiple item ports, multiple input ports, or multiple
 * ports targeting the same group individually. It only conflicts when another
 * configured face already uses the exact same type, mode, and target.</p>
 */
public final class OnlyOneUniquePortPerMultiblockConflictRule extends AbstractMultiblockPortConflictRule {

    /**
     * Validates a non-null configured port candidate.
     *
     * @param context validation context
     * @return validation result for the candidate port
     */
    @Override
    protected MultiblockPortConflictResult validatePort(final MultiblockPortConflictContext context) {
        final ConfiguredMultiblockPort candidate = context.port();

        for (final MultiblockPortFace otherFace : context.menu().exposedPortFaces()) {
            if (otherFace.equals(context.face())) {
                continue;
            }

            final ConfiguredMultiblockPort otherPort = context.menu().configuredPortAt(otherFace)
                    .orElse(null);

            if (otherPort == null) {
                continue;
            }

            if (otherPort.type() == candidate.type()
                    && otherPort.mode() == candidate.mode()
                    && otherPort.target().equals(candidate.target())) {
                return MultiblockPortConflictResult.conflict(
                        Component.translatable(Constant.TranslationKey.PORT_CONFLICT_ONLY_ONE_UNIQUE_PER_MULTIBLOCK)
                );
            }
        }

        return MultiblockPortConflictResult.valid();
    }
}