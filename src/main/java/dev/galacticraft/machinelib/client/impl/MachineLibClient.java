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

package dev.galacticraft.machinelib.client.impl;

import dev.galacticraft.machinelib.api.gas.GasFluid;
import dev.galacticraft.machinelib.client.api.model.MachineModelRegistry;
import dev.galacticraft.machinelib.client.impl.model.MachineBakedModel;
import dev.galacticraft.machinelib.client.impl.model.MachineModelLoader;
import dev.galacticraft.machinelib.client.impl.network.MachineLibS2CPackets;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public final class MachineLibClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(MachineModelLoader::applyModel);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(MachineModelLoader::applyModelVariant);

        // Builtin Sprite Providers
        MachineModelRegistry.register(Constant.id("default"), MachineModelRegistry.SpriteProviderFactory.DEFAULT);
        MachineModelRegistry.register(Constant.id("front_face"), MachineBakedModel.FrontFaceSpriteProvider::new);
        MachineModelRegistry.register(Constant.id("single"), MachineBakedModel.SingleSpriteProvider::new);
        MachineModelRegistry.register(Constant.id("z_axis"), MachineBakedModel.ZAxisSpriteProvider::new);

        for (GasFluid gasFluid : GasFluid.GAS_FLUIDS) {
            FluidVariantRendering.register(gasFluid, new FluidVariantRenderHandler() {
                @Override
                public void appendTooltip(FluidVariant fluidVariant, List<Component> tooltip, TooltipFlag tooltipContext) {
                    tooltip.add(Component.translatable(Constant.TranslationKey.GAS_MARKER));
                    if (tooltipContext.isAdvanced()) tooltip.add(Component.nullToEmpty(gasFluid.getSymbol()));
                }
            });
            FluidRenderHandlerRegistry.INSTANCE.register(gasFluid, new SimpleFluidRenderHandler(gasFluid.getTexture(), gasFluid.getTexture(), null, gasFluid.getTint()));
        }

        MachineLibS2CPackets.register();
    }
}
