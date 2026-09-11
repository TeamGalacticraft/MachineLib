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

package dev.galacticraft.machinelib.client.impl.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.galacticraft.machinelib.client.api.model.MachineTextureBase;
import dev.galacticraft.machinelib.client.api.model.TextureProvider;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MachineModelLoadingPlugin implements PreparableModelLoadingPlugin<MachineModelDataLoader>, ModelResolver {
    public static final String MARKER = "machinelib:type";
    public static final String BASE_TYPE = "base";
    public static final String MACHINE_TYPE = "machine";
    public static final String DEFAULT_MACHINE_BASE = "machine/base";

    public static final MachineModelLoadingPlugin INSTANCE = new MachineModelLoadingPlugin();
    private final Map<ResourceLocation, UnbakedModel> pendingItemModels = new HashMap<>();
    private MachineModelDataLoader data = null;

    @Override
    public @Nullable UnbakedModel resolveModel(ModelResolver.Context context) {
        assert this.data != null;
        JsonObject json = this.data.getMachine(context.id());
        if (json != null) {
            DataResult<? extends Pair<TextureProvider, JsonElement>> sprites = TextureProvider.CODEC.decode(JsonOps.INSTANCE, json.get("data"));
            JsonElement baseId = json.get("base");
            ResourceLocation base = baseId == null ? context.id().withPath(DEFAULT_MACHINE_BASE) : ResourceLocation.parse(baseId.getAsString());
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(context.id().getNamespace(), context.id().getPath().replace("machine/", "item/"));
            MachineUnbakedModel model = new MachineUnbakedModel(sprites.getOrThrow().getFirst(), base);
            this.pendingItemModels.put(location, model);
            if (json.has("item_override")) {
                JsonElement itemOverride = json.get("item_override");
                UnbakedModel unbaked = context.getOrLoadModel(ResourceLocation.parse(itemOverride.getAsString()));
                this.pendingItemModels.put(location, unbaked);
            }
            return model;
        }

        MachineTextureBase base = this.data.getBase(context.id());
        if (base != null) return base;

        return this.pendingItemModels.remove(context.id());
    }

    @Override
    public void onInitializeModelLoader(MachineModelDataLoader data, ModelLoadingPlugin.Context pluginContext) {
        this.data = data;
        this.pendingItemModels.clear();
        pluginContext.resolveModel().register(this);
    }
}
