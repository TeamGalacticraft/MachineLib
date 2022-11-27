//package dev.galacticraft.machinelib.impl.forge.client.model;
//
//import com.google.common.collect.ImmutableList;
//import com.mojang.datafixers.util.Pair;
//import dev.galacticraft.machinelib.client.impl.util.SpriteUtil;
//import net.minecraft.client.renderer.block.model.ItemOverrides;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.resources.model.*;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
//import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
//
//import java.util.Collection;
//import java.util.Set;
//import java.util.function.Function;
//
//public class ForgeMachineUnbakedModel implements IUnbakedGeometry<ForgeMachineUnbakedModel> {
//    @Override
//    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBakery arg, Function<Material, TextureAtlasSprite> function, ModelState arg2, ItemOverrides arg3, ResourceLocation arg4) {
//        return null;
//    }
//
//    @Override
//    public Collection<Material> getMaterials(IGeometryBakingContext iGeometryBakingContext, Function<ResourceLocation, UnbakedModel> function, Set<Pair<String, String>> set) {
//        ImmutableList.Builder<Material> builder = ImmutableList.builderWithExpectedSize(17);
//        builder.add(SpriteUtil.identifier(MachineModelRegistry.MACHINE));
//        builder.add(SpriteUtil.identifier(MachineModelRegistry.MACHINE_SIDE));
//
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_ENERGY_IN));
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_ENERGY_OUT));
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_ENERGY_BOTH));
//
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_FLUID_IN));
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_FLUID_OUT));
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_FLUID_BOTH));
//
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_ITEM_IN));
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_ITEM_OUT));
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_ITEM_BOTH));
//
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_GAS_IN));
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_GAS_OUT));
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_GAS_BOTH));
//
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_ANY_IN));
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_ANY_OUT));
//        builder.add(SpriteUtil.identifier(MachineBakedModel.MACHINE_ANY_BOTH));
//
//        builder.addAll(SpriteUtil.identifiers(MachineBakedModel.TEXTURE_DEPENDENCIES));
//        return builder.build();
//    }
//}
