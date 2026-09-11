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
import com.mojang.serialization.JsonOps;
import dev.galacticraft.machinelib.client.api.model.MachineTextureBase;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MachineModelDataLoader {
    private final Map<ResourceLocation, JsonObject> machines = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, MachineTextureBase> bases = new ConcurrentHashMap<>();

    public void register(ResourceLocation id, JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has(MachineModelLoadingPlugin.MARKER)) {
                String type = obj.get(MachineModelLoadingPlugin.MARKER).getAsString();
                if (type.equals(MachineModelLoadingPlugin.BASE_TYPE)){
                    this.bases.put(id, MachineTextureBase.CODEC.decode(JsonOps.INSTANCE, obj).getOrThrow().getFirst());
                } else {
                    assert type.equals(MachineModelLoadingPlugin.MACHINE_TYPE);
                    this.machines.put(id, obj);
                }
            }
        }
    }

    public JsonObject getMachine(ResourceLocation id) {
        return this.machines.get(id);
    }

    public MachineTextureBase getBase(ResourceLocation id) {
        return this.bases.get(id);
    }
}
