package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Parser for MachineLib static glTF visual models.
 *
 * <p>This implementation supports embedded base64 {@code .gltf} files exported
 * by Blockbench, including positions, normals, UVs, indices, node transforms,
 * embedded PNG textures, and simple material alpha modes.</p>
 */
public final class GltfVisualModelLoader {

    private static final int COMPONENT_FLOAT = 5126;
    private static final int COMPONENT_UNSIGNED_BYTE = 5121;
    private static final int COMPONENT_UNSIGNED_SHORT = 5123;
    private static final int COMPONENT_UNSIGNED_INT = 5125;
    private static final int MODE_TRIANGLES = 4;

    private GltfVisualModelLoader() {

    }

    /**
     * Loads a glTF visual model from a parsed root object.
     *
     * @param id visual model id
     * @param root glTF root object
     * @param textureIds registered texture ids for embedded images
     * @return loaded visual model
     */
    public static GltfVisualModel loadModel(
            final ResourceLocation id,
            final JsonObject root,
            final List<ResourceLocation> textureIds
    ) {
        final List<byte[]> buffers = loadBuffers(root);
        final List<GltfVisualMaterial> materials = loadMaterials(
                root,
                textureIds
        );
        final List<GltfVisualTriangle> triangles = new ArrayList<>();

        final int sceneIndex = root.has("scene")
                ? root.get("scene").getAsInt()
                : 0;

        final JsonObject scene = root.getAsJsonArray("scenes")
                .get(sceneIndex)
                .getAsJsonObject();

        final JsonArray nodes = scene.getAsJsonArray("nodes");

        if (nodes != null) {
            final Matrix4f identity = new Matrix4f();

            for (final JsonElement nodeElement : nodes) {
                loadNode(
                        root,
                        buffers,
                        nodeElement.getAsInt(),
                        identity,
                        triangles
                );
            }
        }

        return new GltfVisualModel(
                id,
                new GltfVisualMesh(triangles),
                materials
        );
    }

    /**
     * Loads all embedded image bytes from the glTF root.
     *
     * @param root glTF root object
     * @return embedded image byte arrays
     */
    public static List<byte[]> loadEmbeddedImages(final JsonObject root) {
        final List<byte[]> images = new ArrayList<>();

        if (!root.has("images")) {
            return images;
        }

        for (final JsonElement imageElement : root.getAsJsonArray("images")) {
            final JsonObject image = imageElement.getAsJsonObject();

            if (!image.has("uri")) {
                throw new IllegalArgumentException("Only embedded image URI textures are supported for now.");
            }

            final String uri = image.get("uri").getAsString();

            if (!uri.startsWith("data:")) {
                throw new IllegalArgumentException("Only embedded base64 image textures are supported for now.");
            }

            images.add(decodeDataUri(uri));
        }

        return images;
    }

    /**
     * Recursively loads one node and its children.
     */
    private static void loadNode(
            final JsonObject root,
            final List<byte[]> buffers,
            final int nodeIndex,
            final Matrix4f parentTransform,
            final List<GltfVisualTriangle> triangles
    ) {
        final JsonObject node = root.getAsJsonArray("nodes")
                .get(nodeIndex)
                .getAsJsonObject();

        final Matrix4f transform = new Matrix4f(parentTransform)
                .mul(readNodeTransform(node));

        if (node.has("mesh")) {
            loadMeshPrimitives(
                    root,
                    buffers,
                    node.get("mesh").getAsInt(),
                    transform,
                    triangles
            );
        }

        if (node.has("children")) {
            for (final JsonElement childElement : node.getAsJsonArray("children")) {
                loadNode(
                        root,
                        buffers,
                        childElement.getAsInt(),
                        transform,
                        triangles
                );
            }
        }
    }

    /**
     * Loads all primitives from a glTF mesh.
     */
    private static void loadMeshPrimitives(
            final JsonObject root,
            final List<byte[]> buffers,
            final int meshIndex,
            final Matrix4f transform,
            final List<GltfVisualTriangle> triangles
    ) {
        final JsonObject mesh = root.getAsJsonArray("meshes")
                .get(meshIndex)
                .getAsJsonObject();

        final Matrix3f normalTransform = new Matrix3f(transform)
                .invert()
                .transpose();

        for (final JsonElement primitiveElement : mesh.getAsJsonArray("primitives")) {
            final JsonObject primitive = primitiveElement.getAsJsonObject();

            final int mode = primitive.has("mode")
                    ? primitive.get("mode").getAsInt()
                    : MODE_TRIANGLES;

            if (mode != MODE_TRIANGLES) {
                throw new IllegalArgumentException("Only glTF TRIANGLES primitives are supported.");
            }

            final JsonObject attributes = primitive.getAsJsonObject("attributes");

            if (attributes == null || !attributes.has("POSITION")) {
                throw new IllegalArgumentException("glTF primitive is missing POSITION attribute.");
            }

            final List<Vector3f> positions = readVec3Accessor(
                    root,
                    buffers,
                    attributes.get("POSITION").getAsInt()
            );

            final List<Vector3f> normals = attributes.has("NORMAL")
                    ? readVec3Accessor(
                    root,
                    buffers,
                    attributes.get("NORMAL").getAsInt()
            )
                    : defaultNormals(positions.size());

            final List<Vector2f> uvs = attributes.has("TEXCOORD_0")
                    ? readVec2Accessor(
                    root,
                    buffers,
                    attributes.get("TEXCOORD_0").getAsInt()
            )
                    : defaultUvs(positions.size());

            final List<Integer> indices = primitive.has("indices")
                    ? readScalarIndexAccessor(
                    root,
                    buffers,
                    primitive.get("indices").getAsInt()
            )
                    : sequentialIndices(positions.size());

            final int materialIndex = primitive.has("material")
                    ? primitive.get("material").getAsInt()
                    : 0;

            for (int i = 0; i < indices.size(); i += 3) {
                final int ai = indices.get(i);
                final int bi = indices.get(i + 1);
                final int ci = indices.get(i + 2);

                triangles.add(new GltfVisualTriangle(
                        createVertex(
                                positions.get(ai),
                                normals.get(ai),
                                uvs.get(ai),
                                transform,
                                normalTransform
                        ),
                        createVertex(
                                positions.get(bi),
                                normals.get(bi),
                                uvs.get(bi),
                                transform,
                                normalTransform
                        ),
                        createVertex(
                                positions.get(ci),
                                normals.get(ci),
                                uvs.get(ci),
                                transform,
                                normalTransform
                        ),
                        materialIndex
                ));
            }
        }
    }

    /**
     * Creates one transformed visual vertex.
     */
    private static GltfVisualVertex createVertex(
            final Vector3f position,
            final Vector3f normal,
            final Vector2f uv,
            final Matrix4f transform,
            final Matrix3f normalTransform
    ) {
        final Vector3f transformedPosition = transform.transformPosition(new Vector3f(position));
        final Vector3f transformedNormal = normalTransform.transform(new Vector3f(normal)).normalize();

        return new GltfVisualVertex(
                transformedPosition,
                transformedNormal,
                uv
        );
    }

    /**
     * Loads material definitions.
     */
    private static List<GltfVisualMaterial> loadMaterials(
            final JsonObject root,
            final List<ResourceLocation> textureIds
    ) {
        final List<GltfVisualMaterial> materials = new ArrayList<>();

        if (!root.has("materials")) {
            materials.add(new GltfVisualMaterial(
                    textureIds.isEmpty() ? null : textureIds.get(0),
                    false
            ));
            return materials;
        }

        for (final JsonElement materialElement : root.getAsJsonArray("materials")) {
            final JsonObject material = materialElement.getAsJsonObject();

            ResourceLocation textureId = textureIds.isEmpty()
                    ? null
                    : textureIds.get(0);

            if (material.has("pbrMetallicRoughness")) {
                final JsonObject pbr = material.getAsJsonObject("pbrMetallicRoughness");

                if (pbr.has("baseColorTexture")) {
                    final JsonObject textureInfo = pbr.getAsJsonObject("baseColorTexture");
                    final int textureIndex = textureInfo.get("index").getAsInt();

                    if (root.has("textures")) {
                        final JsonObject texture = root.getAsJsonArray("textures")
                                .get(textureIndex)
                                .getAsJsonObject();

                        final int sourceIndex = texture.get("source").getAsInt();

                        if (sourceIndex >= 0 && sourceIndex < textureIds.size()) {
                            textureId = textureIds.get(sourceIndex);
                        }
                    }
                }
            }

            final String alphaMode = material.has("alphaMode")
                    ? material.get("alphaMode").getAsString()
                    : "OPAQUE";

            materials.add(new GltfVisualMaterial(
                    textureId,
                    "BLEND".equals(alphaMode)
            ));
        }

        if (materials.isEmpty()) {
            materials.add(new GltfVisualMaterial(
                    textureIds.isEmpty() ? null : textureIds.get(0),
                    false
            ));
        }

        return materials;
    }

    private static Matrix4f readNodeTransform(final JsonObject node) {
        if (node.has("matrix")) {
            final JsonArray matrix = node.getAsJsonArray("matrix");

            return new Matrix4f(
                    matrix.get(0).getAsFloat(),
                    matrix.get(1).getAsFloat(),
                    matrix.get(2).getAsFloat(),
                    matrix.get(3).getAsFloat(),
                    matrix.get(4).getAsFloat(),
                    matrix.get(5).getAsFloat(),
                    matrix.get(6).getAsFloat(),
                    matrix.get(7).getAsFloat(),
                    matrix.get(8).getAsFloat(),
                    matrix.get(9).getAsFloat(),
                    matrix.get(10).getAsFloat(),
                    matrix.get(11).getAsFloat(),
                    matrix.get(12).getAsFloat(),
                    matrix.get(13).getAsFloat(),
                    matrix.get(14).getAsFloat(),
                    matrix.get(15).getAsFloat()
            );
        }

        final Vector3f translation = node.has("translation")
                ? readVec3(node.getAsJsonArray("translation"), 0.0F)
                : new Vector3f();

        final Quaternionf rotation = node.has("rotation")
                ? readQuaternion(node.getAsJsonArray("rotation"))
                : new Quaternionf();

        final Vector3f scale = node.has("scale")
                ? readVec3(node.getAsJsonArray("scale"), 1.0F)
                : new Vector3f(1.0F, 1.0F, 1.0F);

        return new Matrix4f()
                .translationRotateScale(
                        translation,
                        rotation,
                        scale
                );
    }

    private static List<Vector3f> readVec3Accessor(
            final JsonObject root,
            final List<byte[]> buffers,
            final int accessorIndex
    ) {
        final JsonObject accessor = root.getAsJsonArray("accessors")
                .get(accessorIndex)
                .getAsJsonObject();

        if (accessor.get("componentType").getAsInt() != COMPONENT_FLOAT) {
            throw new IllegalArgumentException("Only FLOAT VEC3 accessors are supported.");
        }

        if (!"VEC3".equals(accessor.get("type").getAsString())) {
            throw new IllegalArgumentException("Expected VEC3 accessor.");
        }

        final AccessorView view = createAccessorView(
                root,
                buffers,
                accessor
        );

        final List<Vector3f> values = new ArrayList<>();

        for (int i = 0; i < view.count(); i++) {
            final int offset = view.offset() + i * view.stride();

            values.add(new Vector3f(
                    view.buffer().getFloat(offset),
                    view.buffer().getFloat(offset + 4),
                    view.buffer().getFloat(offset + 8)
            ));
        }

        return values;
    }

    private static List<Vector2f> readVec2Accessor(
            final JsonObject root,
            final List<byte[]> buffers,
            final int accessorIndex
    ) {
        final JsonObject accessor = root.getAsJsonArray("accessors")
                .get(accessorIndex)
                .getAsJsonObject();

        if (accessor.get("componentType").getAsInt() != COMPONENT_FLOAT) {
            throw new IllegalArgumentException("Only FLOAT VEC2 accessors are supported.");
        }

        if (!"VEC2".equals(accessor.get("type").getAsString())) {
            throw new IllegalArgumentException("Expected VEC2 accessor.");
        }

        final AccessorView view = createAccessorView(
                root,
                buffers,
                accessor
        );

        final List<Vector2f> values = new ArrayList<>();

        for (int i = 0; i < view.count(); i++) {
            final int offset = view.offset() + i * view.stride();

            values.add(new Vector2f(
                    view.buffer().getFloat(offset),
                    view.buffer().getFloat(offset + 4)
            ));
        }

        return values;
    }

    private static List<Integer> readScalarIndexAccessor(
            final JsonObject root,
            final List<byte[]> buffers,
            final int accessorIndex
    ) {
        final JsonObject accessor = root.getAsJsonArray("accessors")
                .get(accessorIndex)
                .getAsJsonObject();

        if (!"SCALAR".equals(accessor.get("type").getAsString())) {
            throw new IllegalArgumentException("Expected SCALAR index accessor.");
        }

        final int componentType = accessor.get("componentType").getAsInt();
        final int count = accessor.get("count").getAsInt();

        final JsonObject bufferView = root.getAsJsonArray("bufferViews")
                .get(accessor.get("bufferView").getAsInt())
                .getAsJsonObject();

        final int bufferIndex = bufferView.get("buffer").getAsInt();

        final int accessorOffset = accessor.has("byteOffset")
                ? accessor.get("byteOffset").getAsInt()
                : 0;

        final int bufferViewOffset = bufferView.has("byteOffset")
                ? bufferView.get("byteOffset").getAsInt()
                : 0;

        final java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(
                        buffers.get(bufferIndex)
                )
                .order(java.nio.ByteOrder.LITTLE_ENDIAN);

        final int start = bufferViewOffset + accessorOffset;

        final List<Integer> values = new ArrayList<>();

        switch (componentType) {
            case COMPONENT_UNSIGNED_BYTE -> {
                for (int i = 0; i < count; i++) {
                    values.add(Byte.toUnsignedInt(
                            buffer.get(start + i)
                    ));
                }
            }

            case COMPONENT_UNSIGNED_SHORT -> {
                for (int i = 0; i < count; i++) {
                    values.add(Short.toUnsignedInt(
                            buffer.getShort(start + i * 2)
                    ));
                }
            }

            case COMPONENT_UNSIGNED_INT -> {
                for (int i = 0; i < count; i++) {
                    values.add(
                            buffer.getInt(start + i * 4)
                    );
                }
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported index component type: " + componentType
            );
        }

        return values;
    }

    private static AccessorView createAccessorView(
            final JsonObject root,
            final List<byte[]> buffers,
            final JsonObject accessor
    ) {
        final int accessorOffset = accessor.has("byteOffset")
                ? accessor.get("byteOffset").getAsInt()
                : 0;

        final int count = accessor.get("count").getAsInt();

        final JsonObject bufferView = root.getAsJsonArray("bufferViews")
                .get(accessor.get("bufferView").getAsInt())
                .getAsJsonObject();

        final int bufferIndex = bufferView.get("buffer").getAsInt();
        final int bufferViewOffset = bufferView.has("byteOffset")
                ? bufferView.get("byteOffset").getAsInt()
                : 0;

        final int stride = bufferView.has("byteStride")
                ? bufferView.get("byteStride").getAsInt()
                : defaultStride(accessor);

        return new AccessorView(
                java.nio.ByteBuffer.wrap(buffers.get(bufferIndex))
                        .order(java.nio.ByteOrder.LITTLE_ENDIAN),
                bufferViewOffset + accessorOffset,
                stride,
                count
        );
    }

    private static int defaultStride(final JsonObject accessor) {
        final int componentType = accessor.get("componentType").getAsInt();

        final int componentSize = switch (componentType) {
            case COMPONENT_FLOAT, COMPONENT_UNSIGNED_INT -> 4;
            case COMPONENT_UNSIGNED_SHORT -> 2;
            case COMPONENT_UNSIGNED_BYTE -> 1;
            default -> throw new IllegalArgumentException("Unsupported component type: " + componentType);
        };

        final int componentCount = switch (accessor.get("type").getAsString()) {
            case "SCALAR" -> 1;
            case "VEC2" -> 2;
            case "VEC3" -> 3;
            case "VEC4" -> 4;
            default -> throw new IllegalArgumentException("Unsupported accessor type.");
        };

        return componentSize * componentCount;
    }

    private static List<byte[]> loadBuffers(final JsonObject root) {
        final List<byte[]> buffers = new ArrayList<>();

        for (final JsonElement bufferElement : root.getAsJsonArray("buffers")) {
            final String uri = bufferElement.getAsJsonObject()
                    .get("uri")
                    .getAsString();

            buffers.add(decodeDataUri(uri));
        }

        return buffers;
    }

    private static byte[] decodeDataUri(final String uri) {
        final int comma = uri.indexOf(',');

        if (comma < 0) {
            throw new IllegalArgumentException("Invalid embedded glTF data URI.");
        }

        return Base64.getDecoder().decode(uri.substring(comma + 1));
    }

    private static List<Integer> sequentialIndices(final int count) {
        final List<Integer> indices = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            indices.add(i);
        }

        return indices;
    }

    private static List<Vector3f> defaultNormals(final int count) {
        final List<Vector3f> normals = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            normals.add(new Vector3f(0.0F, 1.0F, 0.0F));
        }

        return normals;
    }

    private static List<Vector2f> defaultUvs(final int count) {
        final List<Vector2f> uvs = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            uvs.add(new Vector2f());
        }

        return uvs;
    }

    private static Vector3f readVec3(
            final JsonArray array,
            final float defaultValue
    ) {
        return new Vector3f(
                array.size() > 0 ? array.get(0).getAsFloat() : defaultValue,
                array.size() > 1 ? array.get(1).getAsFloat() : defaultValue,
                array.size() > 2 ? array.get(2).getAsFloat() : defaultValue
        );
    }

    private static Quaternionf readQuaternion(final JsonArray array) {
        return new Quaternionf(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat(),
                array.get(3).getAsFloat()
        );
    }

    private record AccessorView(
            java.nio.ByteBuffer buffer,
            int offset,
            int stride,
            int count
    ) {

    }

}