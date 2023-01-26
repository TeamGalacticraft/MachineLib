/*
 * Copyright (c) 2021-2023 Team Galacticraft
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

package dev.galacticraft.machinelib.client.impl.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.block.face.BlockFace;
import dev.galacticraft.machinelib.api.block.face.MachineIOFace;
import dev.galacticraft.machinelib.api.machine.MachineConfiguration;
import dev.galacticraft.machinelib.api.machine.MachineIOConfig;
import dev.galacticraft.machinelib.api.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.api.storage.io.ResourceType;
import dev.galacticraft.machinelib.client.api.model.MachineModelRegistry;
import dev.galacticraft.machinelib.client.impl.util.CachingSpriteAtlas;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public final class MachineBakedModel implements FabricBakedModel, BakedModel {
    public static final MachineBakedModel INSTANCE = new MachineBakedModel();

    public static final ResourceLocation MACHINE_ENERGY_IN = Constant.id("block/machine_power_input");
    public static final ResourceLocation MACHINE_ENERGY_OUT = Constant.id("block/machine_power_output");
    public static final ResourceLocation MACHINE_ENERGY_BOTH = Constant.id("block/machine_power_both");

    public static final ResourceLocation MACHINE_FLUID_IN = Constant.id("block/machine_fluid_input");
    public static final ResourceLocation MACHINE_FLUID_OUT = Constant.id("block/machine_fluid_output");
    public static final ResourceLocation MACHINE_FLUID_BOTH = Constant.id("block/machine_fluid_both");

    public static final ResourceLocation MACHINE_ITEM_IN = Constant.id("block/machine_item_input");
    public static final ResourceLocation MACHINE_ITEM_OUT = Constant.id("block/machine_item_output");
    public static final ResourceLocation MACHINE_ITEM_BOTH = Constant.id("block/machine_item_both");

    public static final ResourceLocation MACHINE_GAS_IN = Constant.id("block/machine_gas_input");
    public static final ResourceLocation MACHINE_GAS_OUT = Constant.id("block/machine_gas_output");
    public static final ResourceLocation MACHINE_GAS_BOTH = Constant.id("block/machine_gas_both");

    public static final ResourceLocation MACHINE_ANY_IN = Constant.id("block/machine_any_input");
    public static final ResourceLocation MACHINE_ANY_OUT = Constant.id("block/machine_any_output");
    public static final ResourceLocation MACHINE_ANY_BOTH = Constant.id("block/machine_any_both");

    @ApiStatus.Internal
    public static final CachingSpriteAtlas CACHING_SPRITE_ATLAS = new CachingSpriteAtlas(null);
    @ApiStatus.Internal
    public static final Map<Block, MachineModelRegistry.SpriteProvider> SPRITE_PROVIDERS = new IdentityHashMap<>();
    @ApiStatus.Internal
    public static final Map<String, Set<String>> IDENTIFIERS = new HashMap<>();
    public static final List<ResourceLocation> TEXTURE_DEPENDENCIES = new ArrayList<>();
    private static final ItemTransforms ITEM_TRANSFORMATION = new ItemTransforms(
            new ItemTransform(new Vector3f(75, 45, 0), new Vector3f(0, 0.25f, 0), new Vector3f(0.375f, 0.375f, 0.375f)),
            new ItemTransform(new Vector3f(75, 45, 0), new Vector3f(0, 0.25f, 0), new Vector3f(0.375f, 0.375f, 0.375f)),
            new ItemTransform(new Vector3f(0, 225, 0), new Vector3f(0, 0, 0), new Vector3f(0.40f, 0.40f, 0.40f)),
            new ItemTransform(new Vector3f(0, 45, 0), new Vector3f(0, 0, 0), new Vector3f(0.40f, 0.40f, 0.40f)),
            ItemTransform.NO_TRANSFORM,
            new ItemTransform(new Vector3f(30, 225, 0), new Vector3f(0, 0, 0), new Vector3f(0.625f, 0.625f, 0.625f)),
            new ItemTransform(new Vector3f(0, 0, 0), new Vector3f(0, 0.2f, 0), new Vector3f(0.25f, 0.25f, 0.25f)),
            new ItemTransform(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0.5f, 0.5f, 0.5f))
    );

    private static final MachineConfiguration CONFIGURATION = MachineConfiguration.create();

    static {
        TEXTURE_DEPENDENCIES.add(MachineModelRegistry.MACHINE);
        TEXTURE_DEPENDENCIES.add(MachineModelRegistry.MACHINE_SIDE);

        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_ENERGY_IN);
        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_ENERGY_OUT);
        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_ENERGY_BOTH);

        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_FLUID_IN);
        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_FLUID_OUT);
        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_FLUID_BOTH);

        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_ITEM_IN);
        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_ITEM_OUT);
        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_ITEM_BOTH);

        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_GAS_IN);
        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_GAS_OUT);
        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_GAS_BOTH);

        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_ANY_IN);
        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_ANY_OUT);
        TEXTURE_DEPENDENCIES.add(MachineBakedModel.MACHINE_ANY_BOTH);
    }

    private MachineBakedModel() {
    }

    public static void register(@NotNull Block block, @NotNull MachineModelRegistry.SpriteProvider provider) {
        Preconditions.checkNotNull(block);
        Preconditions.checkNotNull(provider);

        SPRITE_PROVIDERS.put(block, provider);
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        IDENTIFIERS.computeIfAbsent(id.getNamespace(), s -> new HashSet<>());
        IDENTIFIERS.get(id.getNamespace()).add(id.getPath());
    }

    @ApiStatus.Internal
    public static void setSpriteAtlas(Function<ResourceLocation, TextureAtlasSprite> function) {
        CACHING_SPRITE_ATLAS.setAtlas(function);
    }

    public static boolean transform(@NotNull MachineIOConfig ioConfig, @Nullable MachineBlockEntity machine, @NotNull BlockState state, @NotNull MutableQuadView quad) {
        BlockFace face = BlockFace.toFace(state.getValue(BlockStateProperties.HORIZONTAL_FACING), quad.nominalFace());
        MachineIOFace machineFace = ioConfig.get(face);
        quad.spriteBake(0,
                getSprite(face,
                        machine,
                        null,
                        SPRITE_PROVIDERS.getOrDefault(state.getBlock(), MachineModelRegistry.SpriteProvider.DEFAULT),
                        machineFace.getType(), machineFace.getFlow()),
                MutableQuadView.BAKE_LOCK_UV);
        quad.spriteColor(0, -1, -1, -1, -1);
        return true;
    }

    public static boolean transformItem(@NotNull ItemStack stack, @NotNull MutableQuadView quad) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(Constant.Nbt.BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
            CONFIGURATION.readTag(tag.getCompound(Constant.Nbt.BLOCK_ENTITY_TAG).getCompound(Constant.Nbt.CONFIGURATION));
            MachineIOFace machineFace = CONFIGURATION.getIOConfiguration().get(BlockFace.toFace(Direction.NORTH, quad.nominalFace()));
            quad.spriteBake(0,
                    getSprite(BlockFace.toFace(Direction.NORTH, quad.nominalFace()),
                            null,
                            stack,
                            SPRITE_PROVIDERS.getOrDefault(((BlockItem) stack.getItem()).getBlock(), MachineModelRegistry.SpriteProvider.DEFAULT),
                            machineFace.getType(), machineFace.getFlow()),
                    MutableQuadView.BAKE_LOCK_UV);
        } else {
            quad.spriteBake(0, SPRITE_PROVIDERS.getOrDefault(((BlockItem) stack.getItem()).getBlock(), MachineModelRegistry.SpriteProvider.DEFAULT)
                    .getSpritesForState(null, stack, BlockFace.toFace(Direction.NORTH, quad.nominalFace()), CACHING_SPRITE_ATLAS), MutableQuadView.BAKE_LOCK_UV);
        }
        quad.spriteColor(0, -1, -1, -1, -1);
        return true;
    }

    public static TextureAtlasSprite getSprite(@NotNull BlockFace face, @Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull MachineModelRegistry.SpriteProvider provider, @NotNull ResourceType type, @NotNull ResourceFlow flow) {
        switch (flow) {
            case INPUT -> {
                if (type == ResourceType.ENERGY) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ENERGY_IN);
                } else if (type == ResourceType.ITEM) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ITEM_IN);
                } else if (type == ResourceType.FLUID) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_FLUID_IN);
                } else if (type == ResourceType.ANY) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ANY_IN);
                }
            }
            case OUTPUT -> {
                if (type == ResourceType.ENERGY) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ENERGY_OUT);
                } else if (type == ResourceType.ITEM) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ITEM_OUT);
                } else if (type == ResourceType.FLUID) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_FLUID_OUT);
                } else if (type == ResourceType.ANY) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ANY_OUT);
                }
            }
            case BOTH -> {
                if (type == ResourceType.ENERGY) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ENERGY_BOTH);
                } else if (type == ResourceType.ITEM) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ITEM_BOTH);
                } else if (type == ResourceType.FLUID) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_FLUID_BOTH);
                } else if (type == ResourceType.ANY) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ANY_BOTH);
                }
            }
        }
        return provider.getSpritesForState(machine, stack, face, CACHING_SPRITE_ATLAS);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        // TODO: block entity can be null when loading the world, I don't think that's suppose to happen
        if (blockView instanceof RenderAttachedBlockView renderAttachedBlockView && renderAttachedBlockView.getBlockEntityRenderAttachment(pos) instanceof MachineIOConfig ioConfig) {
            context.pushTransform(quad -> transform(ioConfig, (MachineBlockEntity) blockView.getBlockEntity(pos), state, quad));
            for (Direction direction : Constant.Cache.DIRECTIONS) {
                context.getEmitter().square(direction, 0, 0, 1, 1, 0).emit();
            }
            context.popTransform();
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        assert stack.getItem() instanceof BlockItem;
        assert ((BlockItem) stack.getItem()).getBlock() instanceof MachineBlock;
        context.pushTransform(quad -> transformItem(stack, quad));
        for (Direction direction : Constant.Cache.DIRECTIONS) {
            context.getEmitter().square(direction, 0, 0, 1, 1, 0).emit();
        }
        context.popTransform();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return CACHING_SPRITE_ATLAS.apply(MachineModelRegistry.MACHINE);
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return ITEM_TRANSFORMATION;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public static class FrontFaceSpriteProvider implements MachineModelRegistry.SpriteProvider {
        private ResourceLocation sprite;

        public FrontFaceSpriteProvider(ResourceLocation sprite) {
            this.sprite = sprite;
        }

        public FrontFaceSpriteProvider() {
            this.sprite = MachineModelRegistry.MACHINE;
        }

        @Override
        public @NotNull TextureAtlasSprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<ResourceLocation, TextureAtlasSprite> atlas) {
            if (face == BlockFace.FRONT) return atlas.apply(sprite);
            if (face.side()) return atlas.apply(MachineModelRegistry.MACHINE_SIDE);
            return atlas.apply(MachineModelRegistry.MACHINE);
        }

        @Override
        public void fromJson(JsonObject jsonObject, Set<ResourceLocation> textureDependencies) {
            this.sprite = new ResourceLocation(GsonHelper.getAsString(jsonObject, "sprite"));
            textureDependencies.add(sprite);
        }
    }

    public static class SingleSpriteProvider implements MachineModelRegistry.SpriteProvider {
        private ResourceLocation sprite;

        @Override
        public @NotNull TextureAtlasSprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<ResourceLocation, TextureAtlasSprite> atlas) {
            return atlas.apply(sprite);
        }

        @Override
        public void fromJson(JsonObject jsonObject, Set<ResourceLocation> textureDependencies) {
            this.sprite = new ResourceLocation(GsonHelper.getAsString(jsonObject, "sprite"));
            textureDependencies.add(sprite);
        }
    }

    public static class ZAxisSpriteProvider implements MachineModelRegistry.SpriteProvider {
        private ResourceLocation front;
        private ResourceLocation back;
        private boolean sided;

        @Override
        public @NotNull TextureAtlasSprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<ResourceLocation, TextureAtlasSprite> atlas) {
            if (face == BlockFace.FRONT) return atlas.apply(this.front);
            if (face == BlockFace.BACK) return atlas.apply(this.back);
            if (this.sided && face.side()) return atlas.apply(MachineModelRegistry.MACHINE_SIDE);
            return atlas.apply(MachineModelRegistry.MACHINE);
        }

        @Override
        public void fromJson(JsonObject jsonObject, Set<ResourceLocation> textureDependencies) {
            if (jsonObject.has("sprite")) {
                ResourceLocation sprite = new ResourceLocation(GsonHelper.getAsString(jsonObject, "sprite"));
                this.front = sprite;
                this.back = sprite;
            } else {
                this.front = new ResourceLocation(GsonHelper.getAsString(jsonObject, "front"));
                this.back = new ResourceLocation(GsonHelper.getAsString(jsonObject, "back"));
            }
            this.sided = GsonHelper.getAsBoolean(jsonObject, "sided");
            textureDependencies.add(front);
            textureDependencies.add(back);
        }
    }
}