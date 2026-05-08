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

import dev.galacticraft.machinelib.api.filter.ResourceFilters;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuFactory;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockStandardComponents;
import dev.galacticraft.machinelib.api.multiblock.rules.RotationFormationRule;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.TestMenuTypeRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public final class MachineLibTestMultiblocks {

    private MachineLibTestMultiblocks() {

    }

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

                    MultiblockStandardComponents.storage(
                            builder,
                            TestMenuTypeRegistry.TEST_IRON_CUBE_STORAGE
                    );

                    builder.onUsePart(context -> {
                        context.player().sendSystemMessage(Component.literal(
                                "Clicked formed multiblock " + context.definition().id()
                        ));

                        return InteractionResult.PASS;
                    });

                    builder.menu(new MultiblockMenuFactory() {
                        @Override
                        public AbstractContainerMenu createMenu(
                                final MultiblockMenuContext context,
                                final int syncId,
                                final Inventory inventory,
                                final Player player
                        ) {
                            final FormedMultiblockMachine machine =
                                    MultiblockManager.get(context.level()).getById(context.instanceId());

                            if (machine == null || !(player instanceof ServerPlayer serverPlayer)) {
                                return null;
                            }

                            return new TestIronCubeMultiblockMenu(
                                    syncId,
                                    serverPlayer,
                                    machine,
                                    context.clickedPos()
                            );
                        }

                        @Override
                        public Component getDisplayName(final MultiblockMenuContext context) {
                            return Component.literal("Test Iron Cube");
                        }
                    });

                    builder.component(
                            ResourceLocation.fromNamespaceAndPath(Constant.MOD_ID, "test_counter"),
                            TestCounterComponent.class,
                            context -> new TestCounterComponent()
                    );
                }
        );
    }

}