/*
 * Copyright (c) 2021-2024 Team Galacticraft
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
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("ui.machinelib.config.enable_colored_vanilla_fluid_names"), MachineLib.CONFIG.enableColoredVanillaFluidNames())
                .setSaveConsumer(MachineLib.CONFIG::setEnableColoredVanillaFluidNames)
                .setDefaultValue(Config.DEFAULT.enableColoredVanillaFluidNames())
                .build()
        );
        general.addEntry(entryBuilder.startEnumSelector(Component.translatable("ui.machinelib.config.fluid_display_mode"), Config.FluidDisplayMode.class, MachineLib.CONFIG.fluidDisplayMode())
                .setSaveConsumer(MachineLib.CONFIG::setFluidDisplayMode)
                .setDefaultValue(Config.DEFAULT.fluidDisplayMode())
                .setEnumNameProvider(v -> ((Config.FluidDisplayMode)v).getName())
                .build()
        );

//        ConfigCategory debug = builder.getOrCreateCategory(Component.translatable("ui.machinelib.config.category.debug"));
        return builder.build();
    }
}
