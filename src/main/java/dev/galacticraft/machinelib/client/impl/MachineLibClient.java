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

import dev.galacticraft.machinelib.client.api.model.MachineModelRegistry;
import dev.galacticraft.machinelib.client.impl.model.MachineBakedModel;
import dev.galacticraft.machinelib.client.impl.model.MachineModelLoader;
import dev.galacticraft.machinelib.client.impl.network.MachineLibS2CPackets;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import org.jetbrains.annotations.ApiStatus;

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

        MachineLibS2CPackets.register();
    }
}
