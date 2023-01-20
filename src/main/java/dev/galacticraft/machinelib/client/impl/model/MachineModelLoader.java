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

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.galacticraft.machinelib.client.api.model.MachineModelRegistry;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

@ApiStatus.Internal
public final class MachineModelLoader {
    @Contract(pure = true)
    public static @NotNull ModelResourceProvider applyModel(ResourceManager resourceManager) {
        return (resourceId, context) -> {
            if (resourceId.getNamespace().equals("minecraft") && resourceId.getPath().contains("builtin"))
                if (!resourceId.getPath().contains("block"))
                    return null;
            return getModel(resourceManager, new ResourceLocation(resourceId.getNamespace(), resourceId.getPath() + ".json"));
        };
    }

    @Contract(pure = true)
    public static @NotNull ModelVariantProvider applyModelVariant(ResourceManager resourceManager) {
        return (modelId, context) -> {
            getModel(resourceManager, new ResourceLocation(modelId.getNamespace(), "models/block/" + modelId.getPath() + ".json"));
            if (modelId.getVariant().equals("inventory") && MachineBakedModel.IDENTIFIERS.getOrDefault(modelId.getNamespace(), Collections.emptySet()).contains(modelId.getPath())) {
                return MachineUnbakedModel.INSTANCE;
            }
            return null;
        };
    }

    private static @Nullable UnbakedModel getModel(@NotNull ResourceManager resourceManager, ResourceLocation resourceId) {
        try {
            Resource resource = resourceManager.getResource(resourceId).orElse(null);
            if (resource == null)
                return null;
            JsonElement element = Streams.parse(new JsonReader(new InputStreamReader(resource.open(), Charsets.UTF_8)));
            JsonObject json = element.getAsJsonObject();
            if (!json.has("machine"))
                return null;
            MachineModelRegistry.SpriteProvider provider = MachineModelRegistry.getSpriteProvider(new ResourceLocation(GsonHelper.getAsString(json, "sprite_provider"))).get();
            if (json.has("sprite_info"))
                provider.fromJson(json.getAsJsonObject("sprite_info"), Set.copyOf(MachineBakedModel.TEXTURE_DEPENDENCIES));
            String blockID = GsonHelper.getAsString(json, "machine");
            MachineBakedModel.register(BuiltInRegistries.BLOCK.get(new ResourceLocation(blockID)), provider);
            return MachineUnbakedModel.INSTANCE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
