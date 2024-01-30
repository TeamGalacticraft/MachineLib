package dev.galacticraft.machinelib.client.impl.compat;

import dev.galacticraft.machinelib.api.config.Config;
import dev.galacticraft.machinelib.impl.MachineLib;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigScreen {
    public static Screen factory(Screen screen) {
        final ConfigBuilder builder = ConfigBuilder.create();
        final ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        builder.setParentScreen(screen);
        builder.setSavingRunnable(MachineLib.CONFIG::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("ui.machinelib.config.category.general"));
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("ui.machinelib.config.enable_coloured_vanilla_fluid_names"), MachineLib.CONFIG.enableColouredVanillaFluidNames())
                .setSaveConsumer(MachineLib.CONFIG::setEnableColouredVanillaFluidNames)
                .setDefaultValue(Config.DEFAULT.enableColouredVanillaFluidNames())
                .build()
        );
        general.addEntry(entryBuilder.startLongSlider(Component.translatable("ui.machinelib.config.bucket_breakpoint"), MachineLib.CONFIG.bucketBreakpoint(), FluidConstants.BUCKET, FluidConstants.BUCKET * 2048)
                .setSaveConsumer(MachineLib.CONFIG::setBucketBreakpoint)
                .setDefaultValue(Config.DEFAULT.bucketBreakpoint())
                .setTextGetter(integer -> Component.literal(String.valueOf(integer)))
                .build()
        );

//        ConfigCategory debug = builder.getOrCreateCategory(Component.translatable("ui.machinelib.config.category.debug"));
        return builder.build();
    }
}
