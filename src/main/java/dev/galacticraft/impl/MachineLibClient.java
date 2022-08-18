/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.impl;

import dev.galacticraft.api.client.model.MachineModelRegistry;
import dev.galacticraft.api.gas.GasFluid;
import dev.galacticraft.impl.client.model.MachineBakedModel;
import dev.galacticraft.impl.client.model.MachineModelLoader;
import dev.galacticraft.impl.client.network.MachineLibS2CPackets;
import dev.galacticraft.impl.client.resource.MachineLibResourceReloadListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MachineLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(MachineLibResourceReloadListener.INSTANCE);

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(MachineModelLoader::applyModel);

        ModelLoadingRegistry.INSTANCE.registerVariantProvider(MachineModelLoader::applyModelVariant);

        // Builtin Sprite Providers
        MachineModelRegistry.register(new ResourceLocation(MLConstant.MOD_ID, "default"), () -> MachineModelRegistry.SpriteProvider.DEFAULT);
        MachineModelRegistry.register(new ResourceLocation(MLConstant.MOD_ID, "front_face"), MachineBakedModel.FrontFaceSpriteProvider::new);
        MachineModelRegistry.register(new ResourceLocation(MLConstant.MOD_ID, "single"), MachineBakedModel.SingleSpriteProvider::new);
        MachineModelRegistry.register(new ResourceLocation(MLConstant.MOD_ID, "z_axis"), MachineBakedModel.ZAxisSpriteProvider::new);

        for (GasFluid gasFluid : GasFluid.GAS_FLUIDS) {
            FluidVariantRendering.register(gasFluid, new FluidVariantRenderHandler() {
                @Override
                public void appendTooltip(FluidVariant fluidVariant, List<Component> tooltip, TooltipFlag tooltipContext) {
                    tooltip.add(Component.translatable(MLConstant.TranslationKey.GAS_MARKER));
                    if (tooltipContext.isAdvanced()) tooltip.add(Component.nullToEmpty(gasFluid.getSymbol()));
                }
            });
            FluidRenderHandlerRegistry.INSTANCE.register(gasFluid, new SimpleFluidRenderHandler(gasFluid.getTexture(), gasFluid.getTexture(), null, gasFluid.getTint()));
        }

        MachineLibS2CPackets.register();
    }
}
