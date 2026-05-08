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

package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockStandardComponents;
import dev.galacticraft.machinelib.api.multiblock.rules.RotationFormationRule;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.TestMenuTypeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;

/**
 * Registers MachineLib's built-in test multiblocks.
 */
public final class MachineLibTestMultiblocks {

    private MachineLibTestMultiblocks() {

    }

    /**
     * Registers MachineLib's test multiblock definitions.
     */
    public static void register() {
        final SimpleMultiblockPattern pattern =
                new SimpleMultiblockPattern(
                        3,
                        3,
                        3
                );

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    final boolean center = x == 1 && y == 1 && z == 1;

                    if (center) {
                        pattern.set(
                                x,
                                y,
                                z,
                                MultiblockSlotPredicate.block(
                                        Blocks.DIAMOND_BLOCK
                                )
                        );
                    } else {
                        pattern.set(
                                x,
                                y,
                                z,
                                MultiblockSlotPredicate.block(
                                        Blocks.IRON_BLOCK
                                )
                        );
                    }
                }
            }
        }

        MachineLibMultiblocks.register(
                ResourceLocation.fromNamespaceAndPath(
                        Constant.MOD_ID,
                        "test_iron_cube"
                ),
                builder -> {
                    builder.pattern(pattern);
                    builder.rule(RotationFormationRule.allow(MultiblockOrientation.horizontal()));

                    MultiblockStandardComponents.configured(builder);

                    builder.useDefaultMenu(
                            TestMenuTypeRegistry.TEST_IRON_CUBE
                    );

                    builder.onUsePart(context -> {
                        context.player().sendSystemMessage(Component.literal(
                                "Clicked formed multiblock " + context.definition().id()
                        ));

                        return InteractionResult.PASS;
                    });

                    builder.component(
                            ResourceLocation.fromNamespaceAndPath(
                                    Constant.MOD_ID,
                                    "test_counter"
                            ),
                            TestCounterComponent.class,
                            context -> new TestCounterComponent()
                    );
                }
        );
    }

}