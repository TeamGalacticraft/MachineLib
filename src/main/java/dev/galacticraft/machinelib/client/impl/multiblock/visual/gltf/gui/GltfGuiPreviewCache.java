/*
 * Copyright (c) 2021-2025 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software.
 */

package dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.gui;

import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.GltfVisualMaterial;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.GltfVisualModel;
import dev.galacticraft.machinelib.client.impl.multiblock.visual.gltf.GltfVisualTriangle;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Cache for glTF models rendered inside GUI preview panels.
 *
 * <p>The normal world renderer can iterate the model directly, but GUI previews may be rendered
 * very often and can appear in many UI contexts. This cache precomputes model bounds and groups
 * triangles by material once per model instance. Rendering then only needs to apply the current
 * preview pose and emit already-grouped triangles.</p>
 */
public final class GltfGuiPreviewCache {
    private static final Map<GltfVisualModel, CachedModel> CACHE = new HashMap<>();

    private GltfGuiPreviewCache() {
    }

    /**
     * Gets or builds cached GUI preview data for a model.
     *
     * @param model glTF model
     * @return cached preview model
     */
    public static @NotNull CachedModel get(final @NotNull GltfVisualModel model) {
        return CACHE.computeIfAbsent(model, GltfGuiPreviewCache::build);
    }

    /**
     * Clears all cached GUI preview data.
     *
     * <p>This should be called if the glTF model manager is ever changed to reuse model instances
     * across resource reloads. With the current implementation, model instances are recreated on
     * reload, so stale cache entries naturally become unreachable.</p>
     */
    public static void clear() {
        CACHE.clear();
    }

    /**
     * Builds cached GUI preview data for one model.
     *
     * @param model model to cache
     * @return cached model
     */
    private static @NotNull CachedModel build(final @NotNull GltfVisualModel model) {
        final List<GltfVisualMaterial> materials = model.materials();
        final List<List<GltfVisualTriangle>> buckets = new ArrayList<>();

        for (int i = 0; i < materials.size(); i++) {
            buckets.add(new ArrayList<>());
        }

        final BoundsBuilder bounds = new BoundsBuilder();

        for (final GltfVisualTriangle triangle : model.mesh().triangles()) {
            if (triangle.materialIndex() < 0 || triangle.materialIndex() >= buckets.size()) {
                continue;
            }

            buckets.get(triangle.materialIndex()).add(triangle);
            bounds.include(triangle.a().position());
            bounds.include(triangle.b().position());
            bounds.include(triangle.c().position());
        }

        final List<MaterialGroup> groups = new ArrayList<>();

        for (int i = 0; i < materials.size(); i++) {
            groups.add(new MaterialGroup(
                    materials.get(i),
                    buckets.get(i)
            ));
        }

        return new CachedModel(model, groups, bounds.build());
    }

    /**
     * Cached GUI representation of a glTF model.
     *
     * @param source source model
     * @param groups triangles grouped by material
     * @param bounds precomputed model-space bounds
     */
    public record CachedModel(
            GltfVisualModel source,
            List<MaterialGroup> groups,
            Bounds bounds
    ) {
        /**
         * Creates cached model data.
         */
        public CachedModel {
            groups = List.copyOf(groups);
        }
    }

    /**
     * One material group in a cached GUI preview model.
     *
     * @param material material
     * @param triangles triangles using the material
     */
    public record MaterialGroup(
            GltfVisualMaterial material,
            List<GltfVisualTriangle> triangles
    ) {
        /**
         * Creates a material group.
         */
        public MaterialGroup {
            triangles = List.copyOf(triangles);
        }
    }

    /**
     * Model-space bounds used for preview fitting.
     *
     * @param min minimum point
     * @param max maximum point
     * @param center bounds center
     * @param size bounds size
     * @param maxDimension largest bounds dimension
     */
    public record Bounds(
            Vector3f min,
            Vector3f max,
            Vector3f center,
            Vector3f size,
            float maxDimension
    ) {
        /**
         * Creates immutable bounds.
         */
        public Bounds {
            min = new Vector3f(min);
            max = new Vector3f(max);
            center = new Vector3f(center);
            size = new Vector3f(size);
        }
    }

    /**
     * Mutable bounds builder.
     */
    private static final class BoundsBuilder {
        private final Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        private final Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);
        private boolean empty = true;

        /**
         * Includes one point in the bounds.
         *
         * @param point point to include
         */
        private void include(final Vector3f point) {
            this.empty = false;

            this.min.min(point);
            this.max.max(point);
        }

        /**
         * Builds immutable bounds.
         *
         * @return bounds
         */
        private Bounds build() {
            if (this.empty) {
                return new Bounds(
                        new Vector3f(-0.5F, -0.5F, -0.5F),
                        new Vector3f(0.5F, 0.5F, 0.5F),
                        new Vector3f(),
                        new Vector3f(1.0F),
                        1.0F
                );
            }

            final Vector3f size = new Vector3f(this.max).sub(this.min);
            final Vector3f center = new Vector3f(this.min).add(this.max).mul(0.5F);
            final float maxDimension = Math.max(0.0001F, Math.max(size.x(), Math.max(size.y(), size.z())));

            return new Bounds(
                    this.min,
                    this.max,
                    center,
                    size,
                    maxDimension
            );
        }
    }
}