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