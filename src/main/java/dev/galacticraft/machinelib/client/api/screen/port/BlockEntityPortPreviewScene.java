package dev.galacticraft.machinelib.client.api.screen.port;

import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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

            faces.add(new PreviewPortFace(
                    CENTER,
                    direction,
                    blockFace.getName(),
                    ioFace.getType() != ResourceType.NONE
            ));
        }

        return List.copyOf(faces);
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

}