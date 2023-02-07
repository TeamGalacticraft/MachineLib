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

import com.google.gson.JsonObject;
import dev.galacticraft.machinelib.client.api.model.MachineModelRegistry;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

@ApiStatus.Internal
public final class MachineUnbakedModel implements UnbakedModel {
    private static boolean rendererWarn = false;
    private final MachineModelRegistry.SpriteProviderFactory factory;
    private final JsonObject spriteInfo;

    public MachineUnbakedModel(MachineModelRegistry.SpriteProviderFactory factory, JsonObject spriteInfo) {
        this.factory = factory;
        this.spriteInfo = spriteInfo;

        if (!RendererAccess.INSTANCE.hasRenderer() && !rendererWarn) {
            rendererWarn = true;
            MachineLib.LOGGER.error("Failed to find a fabric renderer api backend! Machines might not render.");
        }
    }

    @Override
    public @NotNull Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {

    }

    @Override
    public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation) {
        return new MachineBakedModel(this.factory, this.spriteInfo, function);
    }
}
