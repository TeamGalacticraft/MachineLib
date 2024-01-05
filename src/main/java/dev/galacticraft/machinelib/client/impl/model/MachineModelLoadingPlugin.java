package dev.galacticraft.machinelib.client.impl.model;

import com.google.gson.JsonObject;
import dev.galacticraft.machinelib.client.api.model.MachineModelRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MachineModelLoadingPlugin implements PreparableModelLoadingPlugin<Map<ResourceLocation, JsonObject>>, ModelResolver {
    public static final MachineModelLoadingPlugin INSTANCE = new MachineModelLoadingPlugin();
    private Map<ResourceLocation, JsonObject> data = null;
    private final Map<ResourceLocation, UnbakedModel> pendingItemModels = new HashMap<>();

    @Override
    public @Nullable UnbakedModel resolveModel(ModelResolver.Context context) {
        assert this.data != null;
        JsonObject json = this.data.remove(context.id());
        if (json != null) {
            MachineUnbakedModel model = new MachineUnbakedModel(MachineModelRegistry.getProviderFactory(new ResourceLocation(GsonHelper.getAsString(json, MachineModelRegistry.MARKER))),
                    json.getAsJsonObject("sprites"));
            this.pendingItemModels.put(new ResourceLocation(context.id().getNamespace(), context.id().getPath().replace("machine/", "item/")), model);
            context.getOrLoadModel(new ModelResourceLocation(context.id().getNamespace(), context.id().getPath().replace("machine/", ""), "inventory"));
            return model;
        }
        return this.pendingItemModels.remove(context.id());
    }

    @Override
    public void onInitializeModelLoader(Map<ResourceLocation, JsonObject> data, ModelLoadingPlugin.Context pluginContext) {
        this.data = data;
        this.pendingItemModels.clear();
        pluginContext.resolveModel().register(this);
    }
}
