package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Parser for the initial MachineLib static glTF visual format.
 *
 * <p>This loader intentionally supports a small, well-defined subset first:
 * Blockbench-exported embedded {@code .gltf} files with one embedded buffer,
 * triangle primitives, {@code POSITION} attributes, optional indices, and node
 * transforms using translation, rotation quaternion, and scale.</p>
 *
 * <p>The output is a transformed triangle mesh. Texture, material, alpha, normal,
 * and UV support should be added after this geometry path is verified in-game.</p>
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
     * Loads a transformed mesh from a parsed glTF root object.
     *
     * @param root glTF root object
     * @return transformed visual mesh
     */
    public static GltfVisualMesh loadMesh(final JsonObject root) {
        final List<byte[]> buffers = loadBuffers(root);
        final List<GltfVisualTriangle> triangles = new ArrayList<>();

        final int sceneIndex = root.has("scene")
                ? root.get("scene").getAsInt()
                : 0;

        final JsonObject scene = root.getAsJsonArray("scenes")
                .get(sceneIndex)
                .getAsJsonObject();

        final JsonArray nodes = scene.getAsJsonArray("nodes");

        if (nodes == null) {
            return new GltfVisualMesh(triangles);
        }

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

        return new GltfVisualMesh(triangles);
    }

    /**
     * Recursively loads one glTF node and its children.
     *
     * @param root glTF root object
     * @param buffers decoded buffers
     * @param nodeIndex node index
     * @param parentTransform parent transform
     * @param triangles output triangle list
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
     * Loads every primitive in one glTF mesh.
     *
     * @param root glTF root object
     * @param buffers decoded buffers
     * @param meshIndex mesh index
     * @param transform node transform
     * @param triangles output triangle list
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

            final List<Integer> indices = primitive.has("indices")
                    ? readScalarIndexAccessor(
                    root,
                    buffers,
                    primitive.get("indices").getAsInt()
            )
                    : sequentialIndices(positions.size());

            if (indices.size() % 3 != 0) {
                throw new IllegalArgumentException("glTF triangle index count must be divisible by 3.");
            }

            for (int i = 0; i < indices.size(); i += 3) {
                final Vector3f a = transformPosition(
                        positions.get(indices.get(i)),
                        transform
                );
                final Vector3f b = transformPosition(
                        positions.get(indices.get(i + 1)),
                        transform
                );
                final Vector3f c = transformPosition(
                        positions.get(indices.get(i + 2)),
                        transform
                );

                triangles.add(new GltfVisualTriangle(
                        new GltfVisualVertex(a),
                        new GltfVisualVertex(b),
                        new GltfVisualVertex(c)
                ));
            }
        }
    }

    /**
     * Reads one node transform from matrix or TRS fields.
     *
     * @param node node object
     * @return local node transform
     */
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

    /**
     * Reads a VEC3 float accessor.
     *
     * @param root glTF root object
     * @param buffers decoded buffers
     * @param accessorIndex accessor index
     * @return vector list
     */
    private static List<Vector3f> readVec3Accessor(
            final JsonObject root,
            final List<byte[]> buffers,
            final int accessorIndex
    ) {
        final JsonObject accessor = root.getAsJsonArray("accessors")
                .get(accessorIndex)
                .getAsJsonObject();

        if (accessor.get("componentType").getAsInt() != COMPONENT_FLOAT) {
            throw new IllegalArgumentException("Only FLOAT VEC3 accessors are supported for POSITION.");
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

    /**
     * Reads a scalar index accessor.
     *
     * @param root glTF root object
     * @param buffers decoded buffers
     * @param accessorIndex accessor index
     * @return index list
     */
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
        final AccessorView view = createAccessorView(
                root,
                buffers,
                accessor
        );

        final List<Integer> values = new ArrayList<>();

        for (int i = 0; i < view.count(); i++) {
            final int offset = view.offset() + i * view.stride();

            values.add(switch (componentType) {
                case COMPONENT_UNSIGNED_BYTE -> Byte.toUnsignedInt(view.buffer().get(offset));
                case COMPONENT_UNSIGNED_SHORT -> Short.toUnsignedInt(view.buffer().getShort(offset));
                case COMPONENT_UNSIGNED_INT -> view.buffer().getInt(offset);
                default -> throw new IllegalArgumentException("Unsupported index component type: " + componentType);
            });
        }

        return values;
    }

    /**
     * Creates a byte-buffer view for one glTF accessor.
     *
     * @param root glTF root object
     * @param buffers decoded buffers
     * @param accessor accessor object
     * @return accessor view
     */
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

        final ByteBuffer buffer = ByteBuffer.wrap(buffers.get(bufferIndex))
                .order(ByteOrder.LITTLE_ENDIAN);

        return new AccessorView(
                buffer,
                bufferViewOffset + accessorOffset,
                stride,
                count
        );
    }

    /**
     * Calculates the default tightly packed stride for an accessor.
     *
     * @param accessor accessor object
     * @return stride in bytes
     */
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
            default -> throw new IllegalArgumentException("Unsupported accessor type: " + accessor.get("type").getAsString());
        };

        return componentSize * componentCount;
    }

    /**
     * Decodes all embedded base64 buffers.
     *
     * @param root glTF root object
     * @return decoded buffers
     */
    private static List<byte[]> loadBuffers(final JsonObject root) {
        final List<byte[]> buffers = new ArrayList<>();

        for (final JsonElement bufferElement : root.getAsJsonArray("buffers")) {
            final JsonObject buffer = bufferElement.getAsJsonObject();
            final String uri = buffer.get("uri").getAsString();

            if (!uri.startsWith("data:")) {
                throw new IllegalArgumentException("Only embedded base64 glTF buffers are supported in this first implementation.");
            }

            final int comma = uri.indexOf(',');

            if (comma < 0) {
                throw new IllegalArgumentException("Invalid embedded glTF buffer URI.");
            }

            buffers.add(Base64.getDecoder().decode(uri.substring(comma + 1)));
        }

        return buffers;
    }

    /**
     * Creates a sequential index list for non-indexed primitives.
     *
     * @param count vertex count
     * @return index list
     */
    private static List<Integer> sequentialIndices(final int count) {
        final List<Integer> indices = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            indices.add(i);
        }

        return indices;
    }

    /**
     * Transforms one position vector by a matrix.
     *
     * @param position source position
     * @param transform transform matrix
     * @return transformed position
     */
    private static Vector3f transformPosition(
            final Vector3f position,
            final Matrix4f transform
    ) {
        return transform.transformPosition(new Vector3f(position));
    }

    /**
     * Reads a vector from JSON.
     *
     * @param array source array
     * @param defaultValue fallback value for missing components
     * @return vector
     */
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

    /**
     * Reads a glTF quaternion.
     *
     * <p>glTF stores quaternions as {@code [x, y, z, w]}, matching JOML's
     * constructor order.</p>
     *
     * @param array source array
     * @return quaternion
     */
    private static Quaternionf readQuaternion(final JsonArray array) {
        return new Quaternionf(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat(),
                array.get(3).getAsFloat()
        );
    }

    /**
     * Accessor byte-buffer view.
     *
     * @param buffer byte buffer
     * @param offset absolute byte offset
     * @param stride byte stride
     * @param count element count
     */
    private record AccessorView(
            ByteBuffer buffer,
            int offset,
            int stride,
            int count
    ) {

    }

}