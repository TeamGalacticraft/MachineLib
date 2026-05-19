package dev.galacticraft.machinelib.api.multiblock.visual;

import net.minecraft.resources.ResourceLocation;

/**
 * Optional extension for multiblock visual factories that can expose a model id
 * suitable for GUI previews.
 */
public interface PreviewableMultiblockVisualFactory {
    /**
     * Gets the glTF model id that should be used when rendering this visual inside
     * GUI preview panels.
     *
     * @return preview glTF model id
     */
    ResourceLocation previewVisualModelId();
}