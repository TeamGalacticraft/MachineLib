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

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Backend data source for a reusable 3D port configuration preview.
 */
public interface PortPreviewScene {

    /**
     * Gets blocks to render in the preview.
     *
     * @return preview block list
     */
    List<PreviewBlock> blocks();

    /**
     * Gets selectable/configurable faces.
     *
     * @return preview port face list
     */
    List<PreviewPortFace> portFaces();

    /**
     * Gets the clamped camera bounds.
     *
     * @return preview bounds
     */
    default PortPreviewBounds bounds() {
        return PortPreviewBounds.fromPrimaryBlocks(this.blocks());
    }

    /**
     * Gets the preview title.
     *
     * @return title
     */
    default Component title() {
        return Component.literal("Port Config");
    }

    /**
     * Cycles the configuration for a selected face.
     *
     * @param face selected preview face
     * @param reverse whether to cycle backward
     */
    void cyclePort(PreviewPortFace face, boolean reverse);

    /**
     * Removes the configuration from a selected face.
     *
     * @param face selected preview face
     */
    void removePort(PreviewPortFace face);

    /**
     * Gets detail text for a selected preview face.
     *
     * <p>The face stores display data at creation time, so details update with the
     * same data used by the overlay renderer instead of relying on delayed menu
     * synchronization.</p>
     *
     * @param face selected preview face
     * @return detail lines
     */
    default List<Component> detailsFor(final PreviewPortFace face) {
        return face.detailLines();
    }

    /**
     * Resolves an updated face after a configuration change.
     *
     * <p>Port cycling usually rebuilds {@link PreviewPortFace} records with updated
     * display values. This method finds the replacement face with the same preview
     * position and direction.</p>
     *
     * @param previous previous face record
     * @return updated face record, or {@code previous} if not found
     */
    default PreviewPortFace resolveUpdatedFace(final PreviewPortFace previous) {
        for (final PreviewPortFace face : this.portFaces()) {
            if (face.previewPos().equals(previous.previewPos())
                    && face.previewFace() == previous.previewFace()) {
                return face;
            }
        }

        return previous;
    }

    /**
     * Gets selectable configuration options for a selected face.
     *
     * @param face selected face
     * @return available options
     */
    default List<PreviewPortOption> optionsFor(final PreviewPortFace face) {
        return List.of();
    }

    /**
     * Applies a selected configuration option to a face.
     *
     * @param face selected face
     * @param option selected option
     */
    default void setPort(final PreviewPortFace face, final PreviewPortOption option) {

    }
}