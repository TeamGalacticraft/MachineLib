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

package dev.galacticraft.machinelib.api.config;

import com.google.gson.annotations.SerializedName;
import dev.galacticraft.machinelib.impl.config.MachineLibConfig;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;

public interface Config {
    Config DEFAULT = new MachineLibConfig(null);

    static Config loadFrom(File file) {
        return new MachineLibConfig(file);
    }

    boolean enableColoredVanillaFluidNames();
    void setEnableColoredVanillaFluidNames(boolean enabled);

    FluidDisplayMode fluidDisplayMode();
    void setFluidDisplayMode(FluidDisplayMode value);

    void copyFrom(Config config);

    @ApiStatus.Internal
    void reload();

    void save();

    enum FluidDisplayMode {
        @SerializedName("millibucket")
        MILLIBUCKET(Component.translatable("ui.machinelib.config.fluid_display_mode.millibucket")),
        @SerializedName("raw")
        RAW(Component.translatable("ui.machinelib.config.fluid_display_mode.raw")),;

        private final Component name;

        FluidDisplayMode(Component name) {
            this.name = name;
        }

        public Component getName() {
            return name;
        }
    }
}
