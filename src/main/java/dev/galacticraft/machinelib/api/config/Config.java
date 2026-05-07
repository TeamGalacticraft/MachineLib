/*
 * Copyright (c) 2021-2025 Team Galacticraft
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
import dev.galacticraft.machinelib.api.multiblock.MultiblockValidationMode;
import dev.galacticraft.machinelib.impl.config.MachineLibConfig;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;

/**
 * MachineLib configuration/settings.
 */
public interface Config {
    /**
     * The default config. Do not modify this.
     */
    Config DEFAULT = new MachineLibConfig(null);

    /**
     * Loads a config from the provided file.
     *
     * @param file the file to load the config from
     * @return the loaded config
     */
    static Config loadFrom(File file) {
        return new MachineLibConfig(file);
    }

    /**
     * {@return whether vanilla fluid names should be colored}
     */
    boolean enableColoredVanillaFluidNames();

    /**
     * Sets whether vanilla fluid names should be colored.
     *
     * @param enabled whether vanilla fluid names should be colored
     */
    void setEnableColoredVanillaFluidNames(boolean enabled);

    /**
     * {@return the unit that fluids should be displayed in}
     */
    FluidUnits fluidUnits();

    /**
     * Sets the unit that fluids should be displayed in.
     *
     * @param units the unit that fluids should be displayed in
     */
    void getFluidUnits(FluidUnits units);

    /**
     * Copies the state of the provided config into this config.
     *
     * @param config the config to copy from
     */
    void copyFrom(Config config);

    /**
     * Reloads the config from the file.
     */
    @ApiStatus.Internal
    void reload();

    /**
     * Saves the config to the file.
     */
    void save();

    /**
     * The unit to display fluids in.
     */
    enum FluidUnits {
        /**
         * Display fluids in millibuckets.
         * 1000mB = 1 bucket.
         */
        @SerializedName("millibucket")
        MILLIBUCKET(Component.translatable("ui.machinelib.config.fluid_display_mode.millibucket")),

        /**
         * Display fluids in 81000ths of a bucket.
         *
         * @see net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants#BUCKET
         */
        @SerializedName("raw")
        RAW(Component.translatable("ui.machinelib.config.fluid_display_mode.raw")),
        ;

        /**
         * The name of this display mode.
         */
        private final Component name;

        /**
         * Creates a new display mode.
         *
         * @param name the name of this display mode
         */
        FluidUnits(Component name) {
            this.name = name;
        }

        /**
         * {@return the name of this display mode}
         */
        public Component getName() {
            return name;
        }
    }

    /**
     * {@return the multiblock validation mode}
     */
    MultiblockValidationMode multiblockValidationMode();

    /**
     * Sets the multiblock validation mode.
     *
     * @param mode the validation mode
     */
    void setMultiblockValidationMode(MultiblockValidationMode mode);

    /**
     * {@return the multiblock validation budget per tick}
     */
    int multiblockValidationBudgetPerTick();

    /**
     * Sets the multiblock validation budget per tick.
     *
     * @param budget the validation budget
     */
    void setMultiblockValidationBudgetPerTick(int budget);
}
