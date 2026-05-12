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

import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockMachineMenu;
import dev.galacticraft.machinelib.api.multiblock.MultiblockPattern;
import dev.galacticraft.machinelib.api.multiblock.port.*;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.multiblock.MachineLibMultiblocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * 3D port preview scene for formed multiblock machines.
 *
 * @param <Menu> multiblock menu type
 */
public final class MultiblockPortPreviewScene<Menu extends MultiblockMachineMenu> implements PortPreviewScene {

    private final Menu menu;
    private final Map<PreviewPortFace, MultiblockPortFace> faceMap = new HashMap<>();
    private PortPreviewBounds cachedBounds;

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
                            worldPos,
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
    public PortPreviewBounds bounds() {
        if (this.cachedBounds != null) {
            return this.cachedBounds;
        }

        final MultiblockDefinition definition = MachineLibMultiblocks.getDefinition(this.menu.definitionId);

        if (definition == null) {
            this.cachedBounds = PortPreviewBounds.singleBlock();
            return this.cachedBounds;
        }

        final MultiblockPattern pattern = definition.pattern();

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (int x = 0; x < pattern.sizeX(); x++) {
            for (int y = 0; y < pattern.sizeY(); y++) {
                for (int z = 0; z < pattern.sizeZ(); z++) {
                    if (pattern.predicateAt(x, y, z) == null) {
                        continue;
                    }

                    final BlockPos previewPos = this.transform(new BlockPos(
                            x,
                            y,
                            z
                    ));

                    minX = Math.min(minX, previewPos.getX());
                    minY = Math.min(minY, previewPos.getY());
                    minZ = Math.min(minZ, previewPos.getZ());

                    maxX = Math.max(maxX, previewPos.getX() + 1);
                    maxY = Math.max(maxY, previewPos.getY() + 1);
                    maxZ = Math.max(maxZ, previewPos.getZ() + 1);
                }
            }
        }

        if (minX == Integer.MAX_VALUE) {
            this.cachedBounds = PortPreviewBounds.singleBlock();
            return this.cachedBounds;
        }

        this.cachedBounds = new PortPreviewBounds(
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ
        );

        return this.cachedBounds;
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

            final ConfiguredMultiblockPort configuredPort = this.menu.configuredPortAt(face)
                    .orElse(null);

            final PreviewPortFace previewFaceData = this.createFace(
                    previewPos,
                    previewFace,
                    face,
                    configuredPort
            );

            this.faceMap.put(
                    previewFaceData,
                    face
            );

            faces.add(previewFaceData);
        }

        return List.copyOf(faces);
    }

    /**
     * Creates a preview face for one multiblock port face.
     *
     * @param previewPos preview-space block position
     * @param previewFace preview-space face direction
     * @param portFace logical multiblock port face
     * @param port configured port, or {@code null}
     * @return preview face
     */
    private PreviewPortFace createFace(
            final BlockPos previewPos,
            final Direction previewFace,
            final MultiblockPortFace portFace,
            final ConfiguredMultiblockPort port
    ) {
        final Component label = this.describe(portFace);

        if (port == null) {
            return new PreviewPortFace(
                    previewPos,
                    previewFace,
                    label,
                    false,
                    null,
                    null,
                    null,
                    0x00000000,
                    0xFF9A9A9A,
                    List.of(
                            label,
                            Component.translatable(Constant.TranslationKey.PORT_NONE)
                    )
            );
        }

        return new PreviewPortFace(
                previewPos,
                previewFace,
                label,
                true,
                port.type().name(),
                port.mode().name(),
                port.target().id().toString(),
                this.fillColor(port),
                this.outlineColor(port),
                List.of(
                        label,
                        Component.translatable(
                                Constant.TranslationKey.PORT_TYPE,
                                port.type().name()
                        ),
                        Component.translatable(
                                Constant.TranslationKey.PORT_MODE,
                                port.mode().name()
                        ),
                        Component.translatable(
                                Constant.TranslationKey.PORT_TARGET,
                                port.target().id()
                        )
                )
        );
    }

    /**
     * Gets the translucent fill colour for a multiblock port.
     *
     * @param port configured port
     * @return ARGB fill colour
     */
    private int fillColor(final ConfiguredMultiblockPort port) {
        return switch (port.type()) {
            case ITEM -> 0x66FFD84D;
            case FLUID -> 0x664D8DFF;
            case ENERGY -> 0x6637D65C;
            case REDSTONE -> 0x66D63737;
        };
    }

    /**
     * Gets the outline colour for a multiblock port.
     *
     * @param port configured port
     * @return ARGB outline colour
     */
    private int outlineColor(final ConfiguredMultiblockPort port) {
        return switch (port.mode()) {
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

            final BlockPos worldNeighbourPos = worldPartPos.relative(previewFace);
            final BlockState neighbourState = level.getBlockState(worldNeighbourPos);

            if (neighbourState.isAir()) {
                continue;
            }

            blocks.add(new PreviewBlock(
                    previewNeighbourPos,
                    worldNeighbourPos,
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

        final Component configured = this.menu.configuredPortAt(face)
                .map(ConfiguredMultiblockPort::mode)
                .map(mode -> Component.literal(mode.name()))
                .orElse(Component.translatable(Constant.TranslationKey.NONE));

        return Component.translatable(
                Constant.TranslationKey.PORT_FACE_LABEL,
                position,
                face.face().getName(),
                configured
        );
    }

    @Override
    public List<Component> detailsFor(final PreviewPortFace face) {
        return face.detailLines();
    }

    @Override
    public List<PreviewPortOption> optionsFor(final PreviewPortFace face) {
        final MultiblockPortFace portFace = this.portFaceFor(face);

        if (portFace == null) {
            return List.of();
        }

        final List<PreviewPortOption> options = new ArrayList<>();
        options.add(PreviewPortOption.clear());

        for (final ConfiguredMultiblockPort port : this.menu.portOptionsFor(portFace)) {
            options.add(PreviewPortOption.of(port));
        }

        return List.copyOf(options);
    }

    @Override
    public PreviewPortOptionState optionStateFor(
            final PreviewPortFace face,
            final PreviewPortOption option
    ) {
        if (option.matches(face)) {
            return PreviewPortOptionState.CURRENT;
        }

        if (option.clearsPort()) {
            return PreviewPortOptionState.VALID;
        }

        final MultiblockPortFace portFace = this.portFaceFor(face);

        if (portFace == null) {
            return PreviewPortOptionState.DISABLED;
        }

        final ConfiguredMultiblockPort port = option.port();

        if (port == null) {
            return PreviewPortOptionState.VALID;
        }

        if (this.hasDuplicateTargetConflict(portFace, port)) {
            return PreviewPortOptionState.CONFLICT;
        }

        if (this.hasMissingNeighbourConflict(face, port)) {
            return PreviewPortOptionState.CONFLICT;
        }

        return PreviewPortOptionState.VALID;
    }

    /**
     * Checks whether another configured face already uses the same type, mode, and target.
     *
     * <p>This treats duplicate routing to the same internal target as a conflict.
     * If later you want to allow multiple faces to share one target, this method is
     * the only duplicate-target rule that needs changing.</p>
     *
     * @param selectedFace selected logical port face
     * @param candidate candidate port option
     * @return {@code true} if another face already uses the same target route
     */
    private boolean hasDuplicateTargetConflict(
            final MultiblockPortFace selectedFace,
            final ConfiguredMultiblockPort candidate
    ) {
        for (final MultiblockPortFace otherFace : this.menu.exposedPortFaces()) {
            if (otherFace.equals(selectedFace)) {
                continue;
            }

            final ConfiguredMultiblockPort otherPort = this.menu.configuredPortAt(otherFace)
                    .orElse(null);

            if (otherPort == null) {
                continue;
            }

            if (otherPort.type() == candidate.type()
                    && otherPort.mode() == candidate.mode()
                    && otherPort.target().equals(candidate.target())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a physical neighbour is missing for transfer-style ports.
     *
     * <p>Item and fluid ports normally need an adjacent block, pipe, inventory, or
     * tank-like neighbour to be useful. This marks the option as conflicting when the
     * neighbour position is currently air. Energy and redstone are left valid for now
     * because their neighbour requirements may be looser or custom later.</p>
     *
     * @param face preview face
     * @param candidate candidate port option
     * @return {@code true} if the candidate expects a neighbour but none is present
     */
    private boolean hasMissingNeighbourConflict(
            final PreviewPortFace face,
            final ConfiguredMultiblockPort candidate
    ) {
        if (candidate.type() != MultiblockPortType.ITEM
                && candidate.type() != MultiblockPortType.FLUID) {
            return false;
        }

        final Level level = Minecraft.getInstance().level;

        if (level == null) {
            return false;
        }

        final BlockPos worldPartPos = this.menu.origin.offset(
                face.previewPos().getX(),
                face.previewPos().getY(),
                face.previewPos().getZ()
        );

        final BlockPos neighbourPos = worldPartPos.relative(face.previewFace());

        return level.getBlockState(neighbourPos).isAir();
    }

    @Override
    public void setPort(
            final PreviewPortFace face,
            final PreviewPortOption option
    ) {
        final MultiblockPortFace portFace = this.portFaceFor(face);

        if (portFace == null) {
            return;
        }

        if (option.clearsPort()) {
            this.menu.removePort(portFace);
            return;
        }

        this.menu.setPort(option.port());
    }

    /**
     * Resolves a logical multiblock port face from a preview face.
     *
     * <p>The preview face record may be recreated when configuration changes, so
     * this method first checks the direct map and then falls back to matching by
     * preview position and preview direction.</p>
     *
     * @param face preview face
     * @return logical multiblock port face, or {@code null}
     */
    private MultiblockPortFace portFaceFor(final PreviewPortFace face) {
        final MultiblockPortFace direct = this.faceMap.get(face);

        if (direct != null) {
            return direct;
        }

        for (final Map.Entry<PreviewPortFace, MultiblockPortFace> entry : this.faceMap.entrySet()) {
            final PreviewPortFace candidate = entry.getKey();

            if (candidate.previewPos().equals(face.previewPos())
                    && candidate.previewFace() == face.previewFace()) {
                return entry.getValue();
            }
        }

        return null;
    }

}