package dev.galacticraft.machinelib.client.api.screen.port;

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenu;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import dev.galacticraft.machinelib.api.multiblock.port.ConfiguredMultiblockPort;
import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortFace;
import dev.galacticraft.machinelib.impl.multiblock.MachineLibMultiblocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 3D port preview scene for formed multiblock machines.
 *
 * @param <Menu> multiblock menu type
 */
public final class MultiblockPortPreviewScene<Menu extends MultiblockMachineMenu> implements PortPreviewScene {

    private final Menu menu;
    private final Map<PreviewPortFace, MultiblockPortFace> faceMap = new HashMap<>();

    /**
     * Creates a multiblock preview scene.
     *
     * @param menu backing multiblock menu
     */
    public MultiblockPortPreviewScene(final Menu menu) {
        this.menu = menu;
    }

    @Override
    public List<PreviewBlock> blocks() {
        final MultiblockDefinition definition = MachineLibMultiblocks.getDefinition(this.menu.definitionId);

        if (definition == null) {
            return List.of();
        }

        final List<PreviewBlock> blocks = new ArrayList<>();
        final Level level = Minecraft.getInstance().level;
        final MultiblockPattern pattern = definition.pattern();

        for (int x = 0; x < pattern.sizeX(); x++) {
            for (int y = 0; y < pattern.sizeY(); y++) {
                for (int z = 0; z < pattern.sizeZ(); z++) {
                    if (pattern.predicateAt(x, y, z) == null) {
                        continue;
                    }

                    final BlockPos originalRelative = new BlockPos(
                            x,
                            y,
                            z
                    );

                    final BlockPos previewPos = this.transform(originalRelative);
                    final BlockPos worldPos = this.menu.origin.offset(
                            previewPos.getX(),
                            previewPos.getY(),
                            previewPos.getZ()
                    );

                    if (level == null) {
                        continue;
                    }

                    blocks.add(new PreviewBlock(
                            previewPos,
                            level.getBlockState(worldPos),
                            true
                    ));
                }
            }
        }

        this.addAdjacentBlocks(
                blocks,
                level
        );

        return List.copyOf(blocks);
    }

    @Override
    public List<PreviewPortFace> portFaces() {
        this.faceMap.clear();

        final List<PreviewPortFace> faces = new ArrayList<>();

        for (final MultiblockPortFace face : this.menu.exposedPortFaces()) {
            if (this.menu.portRulesFor(face).isEmpty()) {
                continue;
            }

            final BlockPos previewPos = this.transform(face.relativePos());
            final Direction previewFace = this.menu.orientation.transformDirection(face.face());
            final boolean configured = this.menu.configuredPortAt(face).isPresent();

            final PreviewPortFace previewFaceData = new PreviewPortFace(
                    previewPos,
                    previewFace,
                    this.describe(face),
                    configured
            );

            this.faceMap.put(
                    previewFaceData,
                    face
            );

            faces.add(previewFaceData);
        }

        return List.copyOf(faces);
    }

    @Override
    public void cyclePort(
            final PreviewPortFace face,
            final boolean reverse
    ) {
        final MultiblockPortFace portFace = this.faceMap.get(face);

        if (portFace == null) {
            return;
        }

        this.menu.cyclePort(
                portFace,
                reverse
        );
    }

    @Override
    public void removePort(final PreviewPortFace face) {
        final MultiblockPortFace portFace = this.faceMap.get(face);

        if (portFace == null) {
            return;
        }

        this.menu.removePort(portFace);
    }

    /**
     * Adds non-air neighbour blocks adjacent to valid port faces.
     *
     * @param blocks block list to append to
     * @param level client level
     */
    private void addAdjacentBlocks(
            final List<PreviewBlock> blocks,
            final Level level
    ) {
        if (level == null) {
            return;
        }

        for (final MultiblockPortFace face : this.menu.exposedPortFaces()) {
            if (this.menu.portRulesFor(face).isEmpty()) {
                continue;
            }

            final BlockPos previewPos = this.transform(face.relativePos());
            final Direction previewFace = this.menu.orientation.transformDirection(face.face());
            final BlockPos previewNeighbourPos = previewPos.relative(previewFace);

            final BlockPos worldPartPos = this.menu.origin.offset(
                    previewPos.getX(),
                    previewPos.getY(),
                    previewPos.getZ()
            );

            final BlockState neighbourState = level.getBlockState(worldPartPos.relative(previewFace));

            if (neighbourState.isAir()) {
                continue;
            }

            blocks.add(new PreviewBlock(
                    previewNeighbourPos,
                    neighbourState,
                    false
            ));
        }
    }

    /**
     * Transforms an original pattern-relative position into preview-space.
     *
     * @param originalRelative original pattern-relative position
     * @return transformed preview position
     */
    private BlockPos transform(final BlockPos originalRelative) {
        final MultiblockDefinition definition = MachineLibMultiblocks.getDefinition(this.menu.definitionId);

        if (definition == null) {
            return originalRelative;
        }

        final MultiblockPattern pattern = definition.pattern();

        return this.menu.orientation.transformRelative(
                originalRelative,
                pattern.sizeX(),
                pattern.sizeY(),
                pattern.sizeZ()
        );
    }

    /**
     * Creates a readable face label.
     *
     * @param face port face
     * @return face label
     */
    private Component describe(final MultiblockPortFace face) {
        final String position = face.relativePos().getX()
                + ","
                + face.relativePos().getY()
                + ","
                + face.relativePos().getZ();

        final String configured = this.menu.configuredPortAt(face)
                .map(ConfiguredMultiblockPort::mode)
                .map(Enum::name)
                .orElse("NONE");

        return Component.literal(position + " " + face.face().getName() + " " + configured);
    }

}