package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMenuFactory;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;
import dev.galacticraft.machinelib.api.multiblock.rules.RotationFormationRule;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.Blocks;

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
                            return ChestMenu.threeRows(
                                    syncId,
                                    inventory,
                                    new SimpleContainer(27)
                            );
                        }

                        @Override
                        public Component getDisplayName(final MultiblockMenuContext context) {
                            return Component.literal("Test Iron Cube");
                        }
                    });

                    builder.component(TestCounterComponent.class, context -> new TestCounterComponent());
                }
        );
    }

}