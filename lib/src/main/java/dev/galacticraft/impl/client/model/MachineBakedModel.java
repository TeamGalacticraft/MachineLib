/*
 * Copyright (c) 2021-${year} ${company}
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

package dev.galacticraft.impl.client.model;

import dev.galacticraft.api.block.ConfiguredMachineFace;
import dev.galacticraft.api.block.MachineBlock;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.block.util.BlockFace;
import dev.galacticraft.api.client.model.MachineModelRegistry;
import dev.galacticraft.api.machine.MachineConfiguration;
import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.api.machine.storage.io.ResourceType;
import dev.galacticraft.impl.client.util.CachingSpriteAtlas;
import dev.galacticraft.impl.machine.Constant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class MachineBakedModel implements FabricBakedModel, BakedModel {
    public static final MachineBakedModel INSTANCE = new MachineBakedModel();

    public static final Identifier MACHINE_ENERGY_IN = new Identifier(Constant.MOD_ID, "block/machine_power_input");
    public static final Identifier MACHINE_ENERGY_OUT = new Identifier(Constant.MOD_ID, "block/machine_power_output");
    public static final Identifier MACHINE_ENERGY_IO = new Identifier(Constant.MOD_ID, "block/machine_power_io");

    public static final Identifier MACHINE_FLUID_IN = new Identifier(Constant.MOD_ID, "block/machine_fluid_input");
    public static final Identifier MACHINE_FLUID_OUT = new Identifier(Constant.MOD_ID, "block/machine_fluid_output");
    public static final Identifier MACHINE_FLUID_IO = new Identifier(Constant.MOD_ID, "block/machine_fluid_io");

    public static final Identifier MACHINE_ITEM_IN = new Identifier(Constant.MOD_ID, "block/machine_item_input");
    public static final Identifier MACHINE_ITEM_OUT = new Identifier(Constant.MOD_ID, "block/machine_item_output");
    public static final Identifier MACHINE_ITEM_IO = new Identifier(Constant.MOD_ID, "block/machine_item_io");

    public static final Identifier MACHINE_GAS_IN = new Identifier(Constant.MOD_ID, "block/machine_gas_input");
    public static final Identifier MACHINE_GAS_OUT = new Identifier(Constant.MOD_ID, "block/machine_gas_output");
    public static final Identifier MACHINE_GAS_IO = new Identifier(Constant.MOD_ID, "block/machine_gas_io");

    public static final Identifier MACHINE_ANY_IN = new Identifier(Constant.MOD_ID, "block/machine_any_input");
    public static final Identifier MACHINE_ANY_OUT = new Identifier(Constant.MOD_ID, "block/machine_any_output");
    public static final Identifier MACHINE_ANY_IO = new Identifier(Constant.MOD_ID, "block/machine_any_io");

    private static final ModelTransformation ITEM_TRANSFORMATION = new ModelTransformation(
            new Transformation(new Vec3f(75, 45, 0), new Vec3f(0, 0.25f, 0), new Vec3f(0.375f, 0.375f, 0.375f)),
            new Transformation(new Vec3f(75, 45, 0), new Vec3f(0, 0.25f, 0), new Vec3f(0.375f, 0.375f, 0.375f)),
            new Transformation(new Vec3f(0, 225, 0), new Vec3f(0, 0, 0), new Vec3f(0.40f, 0.40f, 0.40f)),
            new Transformation(new Vec3f(0, 45, 0), new Vec3f(0, 0, 0), new Vec3f(0.40f, 0.40f, 0.40f)),
            Transformation.IDENTITY,
            new Transformation(new Vec3f(30, 225, 0), new Vec3f(0, 0, 0), new Vec3f(0.625f, 0.625f, 0.625f)),
            new Transformation(new Vec3f(0, 0, 0), new Vec3f(0, 0.2f, 0), new Vec3f(0.25f, 0.25f, 0.25f)),
            new Transformation(new Vec3f(0, 0, 0), new Vec3f(0, 0, 0), new Vec3f(0.5f, 0.5f, 0.5f))
    );

    @ApiStatus.Internal
    public static final CachingSpriteAtlas CACHING_SPRITE_ATLAS = new CachingSpriteAtlas(null);
    @ApiStatus.Internal
    public static final Map<Block, MachineModelRegistry.SpriteProvider> SPRITE_PROVIDERS = new IdentityHashMap<>();
    @ApiStatus.Internal
    public static final Map<String, Set<String>> IDENTIFIERS = new HashMap<>();
    public static final List<Identifier> TEXTURE_DEPENDENCIES = new LinkedList<>();
    private static final MachineConfiguration CONFIGURATION = MachineConfiguration.create();

    protected MachineBakedModel() {}

    public static void register(Block block, MachineModelRegistry.SpriteProvider provider) {
        SPRITE_PROVIDERS.put(block, provider);
        Identifier id = Registry.BLOCK.getId(block);
        IDENTIFIERS.putIfAbsent(id.getNamespace(), new HashSet<>());
        IDENTIFIERS.get(id.getNamespace()).add(id.getPath());
    }

    @ApiStatus.Internal
    public static void setSpriteAtlas(Function<Identifier, Sprite> function) {
        CACHING_SPRITE_ATLAS.setAtlas(function);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        MachineBlockEntity machine = ((MachineBlockEntity) blockView.getBlockEntity(pos));
        assert machine != null;
        context.pushTransform(quad -> transform(machine, state, quad));
        for (Direction direction : Direction.values()) {
            context.getEmitter().square(direction, 0, 0, 1, 1, 0).emit();
        }
        context.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        assert stack.getItem() instanceof BlockItem;
        assert ((BlockItem) stack.getItem()).getBlock() instanceof MachineBlock;
        context.pushTransform(quad -> transformItem(stack, quad));
        for (Direction direction : Direction.values()) {
            context.getEmitter().square(direction, 0, 0, 1, 1, 0).emit();
        }
        context.popTransform();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean hasDepth() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getParticleSprite() {
        return CACHING_SPRITE_ATLAS.apply(MachineModelRegistry.MACHINE);
    }

    @Override
    public ModelTransformation getTransformation() {
        return ITEM_TRANSFORMATION;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    public static boolean transform(MachineBlockEntity machine, BlockState state, MutableQuadView quad) {
        BlockFace face = BlockFace.toFace(state.get(Properties.HORIZONTAL_FACING), quad.nominalFace());
        ConfiguredMachineFace machineFace = machine.getIOConfig().get(face);
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

    public static boolean transformItem(ItemStack stack, MutableQuadView quad) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && tag.contains(Constant.Nbt.BLOCK_ENTITY_TAG, NbtElement.COMPOUND_TYPE)) {
            CONFIGURATION.readNbt(tag.getCompound(Constant.Nbt.BLOCK_ENTITY_TAG));
            ConfiguredMachineFace machineFace = CONFIGURATION.getIOConfiguration().get(BlockFace.toFace(Direction.NORTH, quad.nominalFace()));
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

    public static Sprite getSprite(@NotNull BlockFace face, @Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull MachineModelRegistry.SpriteProvider provider, @NotNull ResourceType<?, ?> type, @NotNull ResourceFlow flow) {
        switch (flow) {
            case INPUT -> {
                if (type == ResourceType.ENERGY) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ENERGY_IN);
                } else if (type == ResourceType.ITEM) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ITEM_IN);
                } else if (type == ResourceType.FLUID) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_FLUID_IN);
                } else if (type == ResourceType.GAS) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_GAS_IN);
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
                } else if (type == ResourceType.GAS) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_GAS_OUT);
                } else if (type == ResourceType.ANY) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ANY_OUT);
                }
            }
            case BOTH -> {
                if (type == ResourceType.ENERGY) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ENERGY_IO);
                } else if (type == ResourceType.ITEM) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ITEM_IO);
                } else if (type == ResourceType.FLUID) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_FLUID_IO);
                } else if (type == ResourceType.GAS) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_GAS_IO);
                } else if (type == ResourceType.ANY) {
                    return CACHING_SPRITE_ATLAS.apply(MACHINE_ANY_IO);
                }
            }
        }
        return provider.getSpritesForState(machine, stack, face, CACHING_SPRITE_ATLAS);
    }

    public record FrontFaceSpriteProvider(Identifier sprite) implements MachineModelRegistry.SpriteProvider {
        public FrontFaceSpriteProvider(Identifier sprite) {
            this.sprite = sprite;
            TEXTURE_DEPENDENCIES.add(sprite);
        }

        @Override
        public @NotNull
        Sprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<Identifier, Sprite> atlas) {
            if (face == BlockFace.FRONT) return atlas.apply(sprite);
            if (face.horizontal()) return atlas.apply(MachineModelRegistry.MACHINE_SIDE);
            return atlas.apply(MachineModelRegistry.MACHINE);
        }
    }

    public record SingleSpriteProvider(Identifier sprite) implements MachineModelRegistry.SpriteProvider {
        public SingleSpriteProvider(Identifier sprite) {
            this.sprite = sprite;
            TEXTURE_DEPENDENCIES.add(sprite);
        }

        @Override
        public @NotNull
        Sprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<Identifier, Sprite> atlas) {
            return atlas.apply(sprite);
        }
    }

    public static class ZAxisSpriteProvider implements MachineModelRegistry.SpriteProvider {
        private final Identifier front;
        private final Identifier back;
        private final boolean sided;

        public ZAxisSpriteProvider(Identifier sprite, boolean sided) {
            this(sprite, sprite, sided);
        }

        public ZAxisSpriteProvider(Identifier front, Identifier back, boolean sided) {
            this.front = front;
            this.back = back;
            this.sided = sided;
            TEXTURE_DEPENDENCIES.add(front);
            TEXTURE_DEPENDENCIES.add(back);
        }

        @Override
        public @NotNull Sprite getSpritesForState(@Nullable MachineBlockEntity machine, @Nullable ItemStack stack, @NotNull BlockFace face, @NotNull Function<Identifier, Sprite> atlas) {
            if (face == BlockFace.FRONT) return atlas.apply(this.front);
            if (face == BlockFace.BACK) return atlas.apply(this.back);
            if (this.sided && face.horizontal()) return atlas.apply(MachineModelRegistry.MACHINE_SIDE);
            return atlas.apply(MachineModelRegistry.MACHINE);
        }
    }
}