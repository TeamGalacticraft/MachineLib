package dev.galacticraft.machinelib.testmod;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Constant {
    String MOD_ID = "machinelib-test";
    Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    String SIMPLE_MACHINE = "simple_machine";

    @Contract("_ -> new")
    static @NotNull ResourceLocation id(@NotNull String id) {
        return new ResourceLocation(MOD_ID, id);
    }
}
