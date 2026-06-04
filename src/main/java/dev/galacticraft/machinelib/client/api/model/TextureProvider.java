/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

package dev.galacticraft.machinelib.client.api.model;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.galacticraft.machinelib.api.util.BlockFace;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public record TextureProvider(@Nullable Material front, @Nullable Material back,
                              @Nullable Material left, @Nullable Material right,
                              @Nullable Material top, @Nullable Material bottom,
                              @Nullable Material particle, @Nullable Material topOverride) {
    public static final Codec<Material> MATERIAL_CODEC = Codec.withAlternative(
            ResourceLocation.CODEC.flatComapMap(tex -> new Material(TextureAtlas.LOCATION_BLOCKS, tex), mat -> mat.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS) ? DataResult.success(mat.texture()) : DataResult.error(() -> "not block", mat.texture())),
            RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("atlas").xmap(t -> t.orElse(TextureAtlas.LOCATION_BLOCKS), l -> l.equals(TextureAtlas.LOCATION_BLOCKS) ? Optional.empty() : Optional.of(l)).forGetter(Material::atlasLocation),
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(Material::texture)
            ).apply(instance, Material::new))
    );
    public static final Codec<TextureProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MATERIAL_CODEC.optionalFieldOf("front").forGetter(t -> Optional.ofNullable(t.front)),
            MATERIAL_CODEC.optionalFieldOf("back").forGetter(t -> Optional.ofNullable(t.back)),
            MATERIAL_CODEC.optionalFieldOf("left").forGetter(t -> Optional.ofNullable(t.left)),
            MATERIAL_CODEC.optionalFieldOf("right").forGetter(t -> Optional.ofNullable(t.right)),
            MATERIAL_CODEC.optionalFieldOf("top").forGetter(t -> Optional.ofNullable(t.top)),
            MATERIAL_CODEC.optionalFieldOf("bottom").forGetter(t -> Optional.ofNullable(t.bottom)),
            MATERIAL_CODEC.optionalFieldOf("particle").forGetter(t -> Optional.ofNullable(t.particle)),
            MATERIAL_CODEC.optionalFieldOf("top_item_override").forGetter(t -> Optional.ofNullable(t.topOverride))
    ).apply(instance, (a, b, c, d, e, f, g, h) -> new TextureProvider(
            a.orElse(null),
            b.orElse(null),
            c.orElse(null),
            d.orElse(null),
            e.orElse(null),
            f.orElse(null),
            g.orElse(null),
            h.orElse(null)
    )));

    public BoundTextureProvider bind(Function<Material, TextureAtlasSprite> atlas) {
        return new BoundTextureProvider(
                this.front != null ? atlas.apply(this.front) : null,
                this.back != null ? atlas.apply(this.back) : null,
                this.left != null ? atlas.apply(this.left) : null,
                this.right != null ? atlas.apply(this.right) : null,
                this.top != null ? atlas.apply(this.top) : null,
                this.bottom != null ? atlas.apply(this.bottom) : null,
                this.particle != null ? atlas.apply(this.particle) : null,
                this.topOverride != null ? atlas.apply(this.topOverride) : null
        );
    }

    public static TextureProvider.Builder builder() {
        return new TextureProvider.Builder(null);
    }

    public static TextureProvider.Builder builder(String modId) {
        return new TextureProvider.Builder(modId);
    }

    public static TextureProvider none() {
        return new TextureProvider(null, null, null, null, null, null, null, null);
    }

    public static TextureProvider all(Material material) {
        return new TextureProvider(material, material, material, material, material, material, material, null);
    }

    public static TextureProvider all(ResourceLocation material) {
        return TextureProvider.all(new Material(TextureAtlas.LOCATION_BLOCKS, material));
    }

    public static TextureProvider all(String namespace, String path) {
        return TextureProvider.all(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(namespace, path)));
    }

    public static class Builder {
        private final @Nullable String modId;
        private @Nullable Material front = null;
        private @Nullable Material back = null;
        private @Nullable Material left = null;
        private @Nullable Material right = null;
        private @Nullable Material top = null;
        private @Nullable Material bottom = null;
        private @Nullable Material particle = null;
        private @Nullable Material topOverride = null;

        private Builder(@Nullable String modId) {
            this.modId = modId;
        }

        public Builder front(String texture) {
            Preconditions.checkNotNull(this.modId);
            return this.front(ResourceLocation.fromNamespaceAndPath(this.modId, texture));
        }

        public Builder back(String texture) {
            Preconditions.checkNotNull(this.modId);
            return this.back(ResourceLocation.fromNamespaceAndPath(this.modId, texture));
        }

        public Builder left(String texture) {
            Preconditions.checkNotNull(this.modId);
            return this.left(ResourceLocation.fromNamespaceAndPath(this.modId, texture));
        }

        public Builder right(String texture) {
            Preconditions.checkNotNull(this.modId);
            return this.right(ResourceLocation.fromNamespaceAndPath(this.modId, texture));
        }

        public Builder top(String texture) {
            Preconditions.checkNotNull(this.modId);
            return this.top(ResourceLocation.fromNamespaceAndPath(this.modId, texture));
        }

        public Builder bottom(String texture) {
            Preconditions.checkNotNull(this.modId);
            return this.bottom(ResourceLocation.fromNamespaceAndPath(this.modId, texture));
        }

        public Builder particle(String texture) {
            Preconditions.checkNotNull(this.modId);
            return this.particle(ResourceLocation.fromNamespaceAndPath(this.modId, texture));
        }

        public Builder topOverride(String texture) {
            Preconditions.checkNotNull(this.modId);
            return this.topOverride(ResourceLocation.fromNamespaceAndPath(this.modId, texture));
        }

        public Builder all(String texture) {
            Preconditions.checkNotNull(this.modId);
            return this.all(ResourceLocation.fromNamespaceAndPath(this.modId, texture));
        }

        public Builder sides(String texture) {
            Preconditions.checkNotNull(this.modId);
            return this.sides(ResourceLocation.fromNamespaceAndPath(this.modId, texture));
        }

        public Builder front(ResourceLocation texture) {
            return this.front(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }

        public Builder back(ResourceLocation texture) {
            return this.back(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }

        public Builder left(ResourceLocation texture) {
            return this.left(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }

        public Builder right(ResourceLocation texture) {
            return this.right(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }

        public Builder top(ResourceLocation texture) {
            return this.top(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }

        public Builder bottom(ResourceLocation texture) {
            return this.bottom(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }

        public Builder particle(ResourceLocation texture) {
            return this.particle(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }

        public Builder topOverride(ResourceLocation texture) {
            return this.topOverride(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }

        public Builder all(ResourceLocation texture) {
            return this.all(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }

        public Builder sides(ResourceLocation texture) {
            return this.sides(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }

        public Builder front(Block texture) {
            return this.front(TextureMapping.getBlockTexture(texture));
        }

        public Builder back(Block texture) {
            return this.back(TextureMapping.getBlockTexture(texture));
        }

        public Builder left(Block texture) {
            return this.left(TextureMapping.getBlockTexture(texture));
        }

        public Builder right(Block texture) {
            return this.right(TextureMapping.getBlockTexture(texture));
        }

        public Builder top(Block texture) {
            return this.top(TextureMapping.getBlockTexture(texture));
        }

        public Builder bottom(Block texture) {
            return this.bottom(TextureMapping.getBlockTexture(texture));
        }

        public Builder particle(Block texture) {
            return this.particle(TextureMapping.getBlockTexture(texture));
        }

        public Builder topOverride(Block texture) {
            return this.topOverride(TextureMapping.getBlockTexture(texture));
        }

        public Builder all(Block texture) {
            return this.all(TextureMapping.getBlockTexture(texture));
        }

        public Builder sides(Block texture) {
            return this.sides(TextureMapping.getBlockTexture(texture));
        }

        public Builder front(Material material) {
            this.front = material;
            return this;
        }

        public Builder back(Material material) {
            this.back = material;
            return this;
        }

        public Builder left(Material material) {
            this.left = material;
            return this;
        }

        public Builder right(Material material) {
            this.right = material;
            return this;
        }

        public Builder top(Material material) {
            this.top = material;
            return this;
        }

        public Builder bottom(Material material) {
            this.bottom = material;
            return this;
        }

        public Builder particle(Material material) {
            this.particle = material;
            return this;
        }

        public Builder topOverride(Material material) {
            this.topOverride = material;
            return this;
        }

        public Builder all(Material material) {
            this.front = material;
            this.back = material;
            this.left = material;
            this.right = material;
            this.top = material;
            this.bottom = material;
            this.particle = material;
            return this;
        }

        public Builder sides(Material material) {
            this.front = material;
            this.back = material;
            this.left = material;
            this.right = material;
            return this;
        }

        public TextureProvider build() {
            return new TextureProvider(this.front, this.back, this.left, this.right, this.top, this.bottom, this.particle, this.topOverride);
        }
    }

    public record BoundTextureProvider(@Nullable TextureAtlasSprite front, @Nullable TextureAtlasSprite back,
                                       @Nullable TextureAtlasSprite left, @Nullable TextureAtlasSprite right,
                                       @Nullable TextureAtlasSprite top, @Nullable TextureAtlasSprite bottom,
                                       @Nullable TextureAtlasSprite particle, @Nullable TextureAtlasSprite topOverride) {
        @Nullable
        public TextureAtlasSprite getSprite(@NotNull BlockFace face) {
            return switch (face) {
                case FRONT -> this.front;
                case BACK -> this.back;
                case LEFT -> this.left;
                case RIGHT -> this.right;
                case TOP -> this.top;
                case BOTTOM -> this.bottom;
            };
        }

        @Nullable
        public TextureAtlasSprite getParticle() {
            return this.particle;
        }

        @Nullable
        public TextureAtlasSprite getItemOverride(@NotNull BlockFace face) {
            return switch (face) {
                case FRONT -> null;
                case BACK -> null;
                case LEFT -> null;
                case RIGHT -> null;
                case TOP -> this.topOverride;
                case BOTTOM -> null;
            };
        }
    }
}
