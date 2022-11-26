package dev.galacticraft.machinelib.forge.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Registry.class)
public interface RegistryAccessor {

    @Invoker
    static <T, R extends WritableRegistry<T>> DefaultedRegistry<T> callRegisterDefaulted(ResourceKey<? extends Registry<T>> arg, String string, Lifecycle lifecycle, Registry.RegistryBootstrap<T> arg2) {
        throw new UnsupportedOperationException();
    }
}
