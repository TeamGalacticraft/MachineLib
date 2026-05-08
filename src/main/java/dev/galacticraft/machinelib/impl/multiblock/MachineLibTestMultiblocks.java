package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import dev.galacticraft.machinelib.api.multiblock.MultiblockSlotPredicate;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockStandardComponents;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortMode;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortRule;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortTarget;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortType;
import dev.galacticraft.machinelib.api.multiblock.rules.RotationFormationRule;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.TestMenuTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;

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
        final SimpleMultiblockPattern pattern = new SimpleMultiblockPattern(
                3,
                2,
                2
        );

        pattern.set(0, 0, 0, MultiblockSlotPredicate.block(Blocks.RED_WOOL));
        pattern.set(1, 0, 0, MultiblockSlotPredicate.block(Blocks.DIAMOND_BLOCK));
        pattern.set(2, 0, 0, MultiblockSlotPredicate.block(Blocks.LIME_WOOL));
        pattern.set(0, 0, 1, MultiblockSlotPredicate.block(Blocks.IRON_BLOCK));
        pattern.set(1, 0, 1, MultiblockSlotPredicate.block(Blocks.IRON_BLOCK));
        pattern.set(2, 0, 1, MultiblockSlotPredicate.block(Blocks.IRON_BLOCK));
        pattern.set(0, 1, 1, MultiblockSlotPredicate.block(Blocks.BLUE_WOOL));
        pattern.set(2, 1, 1, MultiblockSlotPredicate.block(Blocks.YELLOW_WOOL));

        MachineLibMultiblocks.register(
                ResourceLocation.fromNamespaceAndPath(
                        Constant.MOD_ID,
                        "test_iron_cube"
                ),
                builder -> {
                    builder.pattern(pattern);
                    builder.rule(RotationFormationRule.allow(MultiblockOrientation.horizontal()));

                    MultiblockStandardComponents.configured(builder);

                    builder.useDefaultMenu(TestMenuTypeRegistry.TEST_IRON_CUBE);

                    registerPortRules(builder);
                    registerDefaultPorts(builder);

//                    builder.onUsePart(context -> {
//                        context.player().sendSystemMessage(Component.literal(
//                                "Clicked formed multiblock " + context.definition().id()
//                        ));
//
//                        return InteractionResult.PASS;
//                    });

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

    /**
     * Registers the allowed configurable port faces for the test iron cube.
     *
     * <p>These faces are pattern-local. The multiblock provider lookup converts
     * world-space access sides back into these local faces using the formed
     * multiblock orientation.</p>
     *
     * @param builder multiblock builder
     */
    private static void registerPortRules(final SimpleMultiblockBuilder builder) {
        builder.portRulesForAllExposedFaces(
                new BlockPos(0, 0, 0),
                Set.of(MultiblockPortType.ITEM),
                Set.of(MultiblockPortMode.OUTPUT),
                Set.of(MultiblockPortTarget.group(Constant.id("item_outputs")))
        );

        builder.portRulesForAllExposedFaces(
                new BlockPos(2, 0, 0),
                Set.of(MultiblockPortType.ITEM),
                Set.of(MultiblockPortMode.INPUT, MultiblockPortMode.OUTPUT),
                Set.of(MultiblockPortTarget.group(Constant.id("item_inputs")))
        );

//        builder.portRule(new MultiblockPortRule(
//                new MultiblockPortFace(
//                        new BlockPos(0, 0, 0),
//                        Direction.DOWN
//                ),
//                Set.of(MultiblockPortType.ITEM),
//                Set.of(MultiblockPortMode.OUTPUT),
//                Set.of(MultiblockPortTarget.group(Constant.id("item_outputs")))
//        ));

//        builder.portRule(MultiblockPortRule.builder(new MultiblockPortFace(
//                        new BlockPos(1, 1, 0),
//                        Direction.NORTH
//                ))
//                .types(MultiblockPortType.ENERGY)
//                .modes(MultiblockPortMode.INPUT, MultiblockPortMode.OUTPUT, MultiblockPortMode.BOTH)
//                .targetGroups(Constant.id("energy"))
//                .build());
//
//        builder.portRule(MultiblockPortRule.builder(new MultiblockPortFace(
//                        new BlockPos(1, 1, 2),
//                        Direction.SOUTH
//                ))
//                .types(MultiblockPortType.FLUID)
//                .modes(MultiblockPortMode.INPUT, MultiblockPortMode.BOTH)
//                .targetGroups(Constant.id("fluid_inputs"))
//                .build());
    }

    /**
     * Registers the default installed ports for newly formed test iron cubes.
     *
     * <p>These defaults must match the rules registered in
     * {@link #registerPortRules(SimpleMultiblockBuilder)}. The
     * {@code MultiblockPortComponent} installs these defaults on first formation
     * and then persists any later player changes.</p>
     *
     * @param builder multiblock builder
     */
    private static void registerDefaultPorts(final SimpleMultiblockBuilder builder) {
        builder.defaultPortsForAllExposedFaces(
                new BlockPos(0, 0, 0),
                MultiblockPortType.ITEM,
                MultiblockPortMode.OUTPUT,
                MultiblockPortTarget.group(Constant.id("item_outputs"))
        );

        builder.defaultPortsForAllExposedFaces(
                new BlockPos(2, 0, 0),
                MultiblockPortType.ITEM,
                MultiblockPortMode.INPUT,
                MultiblockPortTarget.group(Constant.id("item_inputs"))
        );

//        builder.defaultPort(new ConfiguredMultiblockPort(
//                new MultiblockPortFace(
//                        new BlockPos(0, 0, 0),
//                        Direction.DOWN
//                ),
//                MultiblockPortType.ITEM,
//                MultiblockPortMode.OUTPUT,
//                MultiblockPortTarget.group(Constant.id("item_outputs"))
//        ));

//        builder.defaultPort(new ConfiguredMultiblockPort(
//                new MultiblockPortFace(
//                        new BlockPos(1, 1, 0),
//                        Direction.NORTH
//                ),
//                MultiblockPortType.ENERGY,
//                MultiblockPortMode.BOTH,
//                MultiblockPortTarget.group(Constant.id("energy"))
//        ));
//
//        builder.defaultPort(new ConfiguredMultiblockPort(
//                new MultiblockPortFace(
//                        new BlockPos(1, 1, 2),
//                        Direction.SOUTH
//                ),
//                MultiblockPortType.FLUID,
//                MultiblockPortMode.INPUT,
//                MultiblockPortTarget.group(Constant.id("fluid_inputs"))
//        ));
    }

}