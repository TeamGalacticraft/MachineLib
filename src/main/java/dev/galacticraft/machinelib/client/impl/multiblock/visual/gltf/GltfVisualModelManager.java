package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.platform.NativeImage;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GltfVisualModelManager implements SimpleSynchronousResourceReloadListener {

    public static final GltfVisualModelManager INSTANCE = new GltfVisualModelManager();

    private static final String DIRECTORY = "machinelib_visuals";
    private static final String EXTENSION = ".gltf";

    private final Map<ResourceLocation, GltfVisualModel> models = new HashMap<>();

    private GltfVisualModelManager() {

    }

    @Override
    public ResourceLocation getFabricId() {
        return Constant.id("gltf_visual_models");
    }

    @Override
    public void onResourceManagerReload(final ResourceManager manager) {
        final Map<ResourceLocation, GltfVisualModel> loaded = new HashMap<>();

        final Map<ResourceLocation, Resource> resources = manager.listResources(
                DIRECTORY,
                id -> id.getPath().endsWith(EXTENSION)
        );

        for (final Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            final ResourceLocation resourceId = entry.getKey();
            final ResourceLocation modelId = modelIdFromResource(resourceId);

            try (
                    JsonReader reader = new JsonReader(new InputStreamReader(
                            entry.getValue().open(),
                            Charsets.UTF_8
                    ))
            ) {
                final JsonElement element = Streams.parse(reader);
                final List<ResourceLocation> textureIds = registerEmbeddedTextures(
                        modelId,
                        GltfVisualModelLoader.loadEmbeddedImages(element.getAsJsonObject())
                );

                loaded.put(
                        modelId,
                        GltfVisualModelLoader.loadModel(
                                modelId,
                                element.getAsJsonObject(),
                                textureIds
                        )
                );
            } catch (final Exception exception) {
                throw new RuntimeException(
                        "Failed to load MachineLib glTF visual model " + resourceId,
                        exception
                );
            }
        }

        this.models.clear();
        this.models.putAll(loaded);
    }

    public GltfVisualModel get(final ResourceLocation id) {
        return this.models.get(id);
    }

    private static List<ResourceLocation> registerEmbeddedTextures(
            final ResourceLocation modelId,
            final List<byte[]> imageBytes
    ) throws Exception {
        final List<ResourceLocation> ids = new ArrayList<>();

        for (int i = 0; i < imageBytes.size(); i++) {
            final ResourceLocation textureId = ResourceLocation.fromNamespaceAndPath(
                    modelId.getNamespace(),
                    "machinelib_visuals/generated/"
                            + modelId.getPath()
                            + "/texture_"
                            + i
            );

            final NativeImage image = NativeImage.read(new ByteArrayInputStream(imageBytes.get(i)));
            final DynamicTexture texture = new DynamicTexture(image);

            Minecraft.getInstance()
                    .getTextureManager()
                    .register(
                            textureId,
                            texture
                    );

            ids.add(textureId);
        }

        return ids;
    }

    private static ResourceLocation modelIdFromResource(final ResourceLocation resourceId) {
        final String path = resourceId.getPath();

        return ResourceLocation.fromNamespaceAndPath(
                resourceId.getNamespace(),
                path.substring(
                        DIRECTORY.length() + 1,
                        path.length() - EXTENSION.length()
                )
        );
    }

}