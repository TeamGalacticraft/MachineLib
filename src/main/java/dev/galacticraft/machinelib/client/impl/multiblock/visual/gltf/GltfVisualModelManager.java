package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Client resource manager for MachineLib static glTF visual models.
 *
 * <p>Models are loaded from:</p>
 *
 * <pre>{@code
 * assets/<namespace>/machinelib_visuals/<path>.gltf
 * }</pre>
 *
 * <p>The registered model id omits the folder and extension. For example:</p>
 *
 * <pre>{@code
 * assets/example/machinelib_visuals/test_cube.gltf
 * }</pre>
 *
 * <p>is looked up as:</p>
 *
 * <pre>{@code
 * example:test_cube
 * }</pre>
 */
public final class GltfVisualModelManager implements SimpleSynchronousResourceReloadListener {

    public static final GltfVisualModelManager INSTANCE = new GltfVisualModelManager();

    private static final String DIRECTORY = "machinelib_visuals";
    private static final String EXTENSION = ".gltf";

    private final Map<ResourceLocation, GltfVisualModel> models = new HashMap<>();

    private GltfVisualModelManager() {

    }

    /**
     * Gets the reload listener id.
     *
     * @return reload listener id
     */
    @Override
    public ResourceLocation getFabricId() {
        return Constant.id("gltf_visual_models");
    }

    /**
     * Reloads every MachineLib glTF visual model from client resources.
     *
     * @param manager active resource manager
     */
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

                if (!element.isJsonObject()) {
                    throw new IllegalArgumentException("Root glTF element is not an object.");
                }

                loaded.put(
                        modelId,
                        new GltfVisualModel(
                                modelId,
                                GltfVisualModelLoader.loadMesh(element.getAsJsonObject())
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

    /**
     * Gets a loaded glTF visual model by id.
     *
     * @param id visual model id
     * @return loaded model, or {@code null}
     */
    public GltfVisualModel get(final ResourceLocation id) {
        return this.models.get(id);
    }

    /**
     * Converts a resource id to a model id.
     *
     * @param resourceId resource id
     * @return model id
     */
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