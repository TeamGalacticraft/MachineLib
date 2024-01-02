/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

import com.google.gson.JsonObject;
import dev.galacticraft.machinelib.api.block.MachineBlock;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.MachineIOFace;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.api.model.MachineModelRegistry;
import dev.galacticraft.machinelib.client.api.render.MachineRenderData;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public final class MachineBakedModel implements FabricBakedModel, BakedModel {
    public static final Material MACHINE = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine"));
    public static final Material MACHINE_SIDE = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_side"));
    
    public static final Material MACHINE_ENERGY_IN = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_power_input"));
    public static final Material MACHINE_ENERGY_OUT = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_power_output"));
    public static final Material MACHINE_ENERGY_BOTH = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_power_both"));

    public static final Material MACHINE_FLUID_IN = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_fluid_input"));
    public static final Material MACHINE_FLUID_OUT = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_fluid_output"));
    public static final Material MACHINE_FLUID_BOTH = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_fluid_both"));

    public static final Material MACHINE_ITEM_IN = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_item_input"));
    public static final Material MACHINE_ITEM_OUT = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_item_output"));
    public static final Material MACHINE_ITEM_BOTH = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_item_both"));

    public static final Material MACHINE_GAS_IN = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_gas_input"));
    public static final Material MACHINE_GAS_OUT = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_gas_output"));
    public static final Material MACHINE_GAS_BOTH = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_gas_both"));

    public static final Material MACHINE_ANY_IN = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_any_input"));
    public static final Material MACHINE_ANY_OUT = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_any_output"));
    public static final Material MACHINE_ANY_BOTH = new Material(TextureAtlas.LOCATION_BLOCKS, Constant.id("block/machine_any_both"));

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

    private final MachineModelRegistry.SpriteProvider provider;
    private final Function<Material, TextureAtlasSprite> atlasFunction;
    private final TextureAtlasSprite particle;

    public MachineBakedModel(MachineModelRegistry.SpriteProviderFactory factory, JsonObject spriteInfo, Function<Material, TextureAtlasSprite> function) {
        this.provider = factory.create(spriteInfo, function);
        this.atlasFunction = function;
        this.particle = function.apply(MACHINE);
    }

    private boolean transform(MachineRenderData renderData, @NotNull BlockState state, @NotNull MutableQuadView quad) {
        BlockFace face = BlockFace.toFace(state.getValue(BlockStateProperties.HORIZONTAL_FACING), quad.nominalFace());
        MachineIOFace machineFace = renderData == null ? MachineIOFace.blank() : renderData.getIOConfig().get(face);
        assert face != null;
        quad.spriteBake(getSprite(face,
                        renderData,
                        machineFace.getType(), machineFace.getFlow()),
                MutableQuadView.BAKE_LOCK_UV);
        quad.color(-1, -1, -1, -1);
        return true;
    }

    private boolean transformItem(MachineIOConfig config, @NotNull MutableQuadView quad) {
        BlockFace face = BlockFace.toFace(Direction.NORTH, quad.nominalFace());
        MachineIOFace machineIOFace = config.get(face);
        assert face != null;
        quad.spriteBake(getSprite(face,
                        config,
                        machineIOFace.getType(), machineIOFace.getFlow()),
                MutableQuadView.BAKE_LOCK_UV);
        quad.color(-1, -1, -1, -1);
        return true;
    }

    public TextureAtlasSprite getSprite(@NotNull BlockFace face, @Nullable MachineRenderData renderData, @NotNull ResourceType type, @NotNull ResourceFlow flow) {
        if (type == ResourceType.NONE) return this.provider.getSpritesForState(renderData, face);

        switch (flow) {
            case INPUT -> {
                switch (type) {
                    case ENERGY -> {
                        return this.atlasFunction.apply(MACHINE_ENERGY_IN); //todo: cache these
                    }
                    case ITEM -> {
                        return this.atlasFunction.apply(MACHINE_ITEM_IN);
                    }
                    case FLUID -> {
                        return this.atlasFunction.apply(MACHINE_FLUID_IN);
                    }
                    case ANY -> {
                        return this.atlasFunction.apply(MACHINE_ANY_IN);
                    }
                }
            }
            case OUTPUT -> {
                switch (type) {
                    case ENERGY -> {
                        return this.atlasFunction.apply(MACHINE_ENERGY_OUT);
                    }
                    case ITEM -> {
                        return this.atlasFunction.apply(MACHINE_ITEM_OUT);
                    }
                    case FLUID -> {
                        return this.atlasFunction.apply(MACHINE_FLUID_OUT);
                    }
                    case ANY -> {
                        return this.atlasFunction.apply(MACHINE_ANY_OUT);
                    }
                }
            }
            case BOTH -> {
                switch (type) {
                    case ENERGY -> {
                        return this.atlasFunction.apply(MACHINE_ENERGY_BOTH);
                    }
                    case ITEM -> {
                        return this.atlasFunction.apply(MACHINE_ITEM_BOTH);
                    }
                    case FLUID -> {
                        return this.atlasFunction.apply(MACHINE_FLUID_BOTH);
                    }
                    case ANY -> {
                        return this.atlasFunction.apply(MACHINE_ANY_BOTH);
                    }
                }
            }
        }
        
        return this.provider.getSpritesForState(renderData, face);
    }

    public MachineModelRegistry.SpriteProvider getProvider() {
        return provider;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        // TODO: block entity can be null when loading the world, I don't think that's suppose to happen
        if (blockView instanceof RenderAttachedBlockView renderAttachedBlockView && renderAttachedBlockView.getBlockEntityRenderAttachment(pos) instanceof MachineRenderData renderData) {
            context.pushTransform(quad -> transform(renderData, state, quad));
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
        CompoundTag tag = stack.getTag();
        MachineIOConfig config = MachineIOConfig.create();
        if (tag != null && tag.contains(Constant.Nbt.BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag beTag = tag.getCompound(Constant.Nbt.BLOCK_ENTITY_TAG);
            if (beTag.contains(Constant.Nbt.CONFIGURATION, Tag.TAG_COMPOUND)) {
                CompoundTag confTag = beTag.getCompound(Constant.Nbt.CONFIGURATION);
                if (confTag.contains(Constant.Nbt.CONFIGURATION, Tag.TAG_COMPOUND)) {
                    config.readTag(confTag.getCompound(Constant.Nbt.CONFIGURATION));
                }
            }
        }

        context.pushTransform(quad -> transformItem(config, quad));
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
        return particle;
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
        private final TextureAtlasSprite sprite;
        private final TextureAtlasSprite machineSide;
        private final TextureAtlasSprite machine;

        public FrontFaceSpriteProvider(@NotNull JsonObject json, @NotNull Function<Material, TextureAtlasSprite> atlas) {
            this.sprite = atlas.apply(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(GsonHelper.getAsString(json, "sprite"))));
            this.machineSide = atlas.apply(MACHINE_SIDE);
            this.machine = atlas.apply(MACHINE);
        }

        @Override
        public @NotNull TextureAtlasSprite getSpritesForState(@Nullable MachineRenderData renderData, @NotNull BlockFace face) {
            if (face == BlockFace.FRONT) return this.sprite;
            if (face.side()) return this.machineSide;
            return this.machine;
        }
    }

    public static class SingleSpriteProvider implements MachineModelRegistry.SpriteProvider {
        private final TextureAtlasSprite sprite;

        public SingleSpriteProvider(@NotNull JsonObject json, @NotNull Function<Material, TextureAtlasSprite> atlas) {
            this.sprite = atlas.apply(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(GsonHelper.getAsString(json, "sprite"))));
        }

        @Override
        public @NotNull TextureAtlasSprite getSpritesForState(@Nullable MachineRenderData renderData, @NotNull BlockFace face) {
            return this.sprite;
        }
    }

    public static class ZAxisSpriteProvider implements MachineModelRegistry.SpriteProvider {
        private final boolean sided;
        private final TextureAtlasSprite front;
        private final TextureAtlasSprite back;
        private final TextureAtlasSprite machineSide;
        private final TextureAtlasSprite machine;

        public ZAxisSpriteProvider(@NotNull JsonObject json, @NotNull Function<Material, TextureAtlasSprite> atlas) {
            if (json.has("sprite")) {
                this.front = this.back = atlas.apply(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(GsonHelper.getAsString(json, "sprite"))));
            } else {
                this.front = atlas.apply(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(GsonHelper.getAsString(json, "front"))));
                this.back = atlas.apply(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(GsonHelper.getAsString(json, "back"))));
            }
            this.sided = GsonHelper.getAsBoolean(json, "sided");
            this.machineSide = atlas.apply(MACHINE_SIDE);
            this.machine = atlas.apply(MACHINE);
        }

        @Override
        public @NotNull TextureAtlasSprite getSpritesForState(@Nullable MachineRenderData renderData, @NotNull BlockFace face) {
            if (face == BlockFace.FRONT) return this.front;
            if (face == BlockFace.BACK) return this.back;
            if (this.sided && face.side()) return this.machineSide;
            return this.machine;
        }
    }
}