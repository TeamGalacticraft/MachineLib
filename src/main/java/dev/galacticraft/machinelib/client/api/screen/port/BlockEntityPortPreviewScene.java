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

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 3D port preview scene for ordinary single-block machine block entities.
 *
 * @param <Machine> machine block entity type
 * @param <Menu> machine menu type
 */
public final class BlockEntityPortPreviewScene<
        Machine extends MachineBlockEntity,
        Menu extends MachineMenu<Machine>
        > implements PortPreviewScene {

    private static final BlockPos CENTER = BlockPos.ZERO;

    private final Menu menu;

    /**
     * Creates a block-entity preview scene.
     *
     * @param menu backing menu
     */
    public BlockEntityPortPreviewScene(final Menu menu) {
        this.menu = menu;
    }

    @Override
    public List<PreviewBlock> blocks() {
        final List<PreviewBlock> blocks = new ArrayList<>();
        final BlockState machineState = this.menu.be.getBlockState();

        blocks.add(new PreviewBlock(
                CENTER,
                this.menu.be.getBlockPos(),
                machineState,
                true
        ));

        final Level level = Minecraft.getInstance().level;

        if (level == null) {
            return List.copyOf(blocks);
        }

        final BlockPos origin = this.menu.be.getBlockPos();

        for (final Direction direction : Direction.values()) {
            final BlockPos neighbourPos = origin.relative(direction);
            final BlockState neighbourState = level.getBlockState(neighbourPos);

            if (neighbourState.isAir()) {
                continue;
            }

            blocks.add(new PreviewBlock(
                    CENTER.relative(direction),
                    neighbourPos,
                    neighbourState,
                    false
            ));
        }

        return List.copyOf(blocks);
    }

    @Override
    public List<PreviewPortFace> portFaces() {
        final List<PreviewPortFace> faces = new ArrayList<>();
        final BlockState state = this.menu.be.getBlockState();
        final Direction facing = this.machineFacing(state);

        for (final Direction direction : Direction.values()) {
            final BlockFace blockFace = BlockFace.from(
                    facing,
                    direction
            );

            if (blockFace == null || this.menu.isFaceLocked(blockFace)) {
                continue;
            }

            final IOFace ioFace = this.menu.configuration.get(blockFace);

            faces.add(this.createFace(
                    CENTER,
                    direction,
                    blockFace.getName(),
                    ioFace
            ));
        }

        return List.copyOf(faces);
    }


    /**
     * Gets the translucent fill colour for a normal machine side.
     *
     * @param face configured IO face
     * @return ARGB fill colour
     */
    private int fillColor(final IOFace face) {
        return switch (face.getType()) {
            case ITEM -> 0x66FFD84D;
            case FLUID -> 0x664D8DFF;
            case ENERGY -> 0x6637D65C;
            default -> 0x00000000;
        };
    }

    /**
     * Gets the outline colour for a normal machine side.
     *
     * @param face configured IO face
     * @return ARGB outline colour
     */
    private int outlineColor(final IOFace face) {
        if (face.getType() == ResourceType.NONE) {
            return 0xFF9A9A9A;
        }

        return switch (face.getFlow()) {
            case INPUT -> 0xFF37D65C;
            case OUTPUT -> 0xFFD63737;
            case BOTH -> 0xFFFFD84D;
        };
    }

    @Override
    public void cyclePort(
            final PreviewPortFace face,
            final boolean reverse
    ) {
        final BlockFace blockFace = this.blockFace(face);

        if (blockFace == null || this.menu.isFaceLocked(blockFace)) {
            return;
        }

        this.menu.cycleFaceConfig(
                blockFace,
                reverse,
                false
        );
    }

    @Override
    public void removePort(final PreviewPortFace face) {
        final BlockFace blockFace = this.blockFace(face);

        if (blockFace == null || this.menu.isFaceLocked(blockFace)) {
            return;
        }

        this.menu.cycleFaceConfig(
                blockFace,
                false,
                true
        );
    }

    /**
     * Converts a selected preview face into the machine's logical block face.
     *
     * @param face selected preview face
     * @return logical block face, or {@code null}
     */
    private BlockFace blockFace(final PreviewPortFace face) {
        return BlockFace.from(
                this.machineFacing(this.menu.be.getBlockState()),
                face.previewFace()
        );
    }

    @Override
    public PortPreviewBounds bounds() {
        return PortPreviewBounds.singleBlock();
    }

    /**
     * Gets the machine's facing direction.
     *
     * @param state machine block state
     * @return horizontal facing direction
     */
    private Direction machineFacing(final BlockState state) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        return Direction.NORTH;
    }

    @Override
    public List<Component> detailsFor(final PreviewPortFace face) {
        final BlockFace blockFace = this.blockFace(face);

        if (blockFace == null) {
            return List.of(face.label());
        }

        final IOFace ioFace = this.menu.configuration.get(blockFace);

        final List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(
                Constant.TranslationKey.PORT_SELECTED,
                blockFace.getName()
        ));

        if (ioFace.getType() == ResourceType.NONE) {
            lines.add(Component.translatable(Constant.TranslationKey.PORT_NONE));
        } else {
            lines.add(Component.translatable(
                    Constant.TranslationKey.PORT_TYPE,
                    ioFace.getType().getName()
            ));
            lines.add(Component.translatable(
                    Constant.TranslationKey.PORT_FLOW,
                    ioFace.getFlow().getName()
            ));
        }

        return List.copyOf(lines);
    }

    /**
     * Creates a preview face for a normal block entity machine side.
     *
     * @param previewPos preview-space block position
     * @param previewFace preview-space face direction
     * @param label face label
     * @param ioFace configured IO face
     * @return preview face
     */
    private PreviewPortFace createFace(
            final BlockPos previewPos,
            final Direction previewFace,
            final Component label,
            final IOFace ioFace
    ) {
        if (ioFace.getType() == ResourceType.NONE) {
            return PreviewPortFace.none(
                    previewPos,
                    previewFace,
                    label
            );
        }

        return new PreviewPortFace(
                previewPos,
                previewFace,
                label,
                true,
                ioFace.getType().getName().getString(),
                ioFace.getFlow().getName().getString(),
                null,
                this.fillColor(ioFace),
                this.outlineColor(ioFace),
                List.of(
                        label,
                        Component.translatable(
                                Constant.TranslationKey.PORT_TYPE,
                                ioFace.getType().getName()
                        ),
                        Component.translatable(
                                Constant.TranslationKey.PORT_FLOW,
                                ioFace.getFlow().getName()
                        )
                )
        );
    }

}