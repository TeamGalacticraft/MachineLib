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

package dev.galacticraft.machinelib.impl.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import dev.galacticraft.machinelib.api.config.Config;
import dev.galacticraft.machinelib.api.multiblock.MultiblockValidationMode;
import dev.galacticraft.machinelib.impl.MachineLib;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MachineLibConfig implements Config {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private final @Nullable File file;

    @Expose
    public boolean enableColoredVanillaFluidNames = true;
    @Expose
    public FluidUnits fluidUnits = FluidUnits.MILLIBUCKET;

    @Expose
    public MultiblockValidationMode multiblockValidationMode = MultiblockValidationMode.END_OF_TICK;

    @Expose
    public int multiblockValidationBudgetPerTick = 32;

    public MachineLibConfig(@Nullable File file) {
        this.file = file;

        if (file != null) {
            if (file.exists()) {
                this.reload();
            } else {
                MachineLib.LOGGER.info("Config file does not exist. Creating it...");
                this.save();
            }
        }
    }

    @Override
    public boolean enableColoredVanillaFluidNames() {
        return this.enableColoredVanillaFluidNames;
    }

    @Override
    public void setEnableColoredVanillaFluidNames(boolean enabled) {
        this.enableColoredVanillaFluidNames = enabled;
    }

    @Override
    public FluidUnits fluidUnits() {
        return this.fluidUnits;
    }

    @Override
    public void getFluidUnits(FluidUnits units) {
        this.fluidUnits = units;
    }

    @Override
    public MultiblockValidationMode multiblockValidationMode() {
        return this.multiblockValidationMode;
    }

    @Override
    public void setMultiblockValidationMode(final MultiblockValidationMode mode) {
        this.multiblockValidationMode = mode;
    }

    @Override
    public int multiblockValidationBudgetPerTick() {
        return this.multiblockValidationBudgetPerTick;
    }

    @Override
    public void setMultiblockValidationBudgetPerTick(final int budget) {
        this.multiblockValidationBudgetPerTick = Math.max(1, budget);
    }

    @Override
    public void copyFrom(Config config) {
        this.enableColoredVanillaFluidNames = config.enableColoredVanillaFluidNames();
        this.fluidUnits = config.fluidUnits();
        this.multiblockValidationMode = config.multiblockValidationMode();
        this.multiblockValidationBudgetPerTick = config.multiblockValidationBudgetPerTick();
    }

    @Override
    public void reload() {
        if (this.file != null) {
            try (FileReader reader = new FileReader(this.file, StandardCharsets.UTF_8)) {
                MachineLibConfig config = GSON.fromJson(reader, MachineLibConfig.class);
                this.copyFrom(config);
            } catch (IOException e) {
                MachineLib.LOGGER.error("Failed to read config file!", e);
            }
        }
    }

    @Override
    public void save() {
        if (this.file != null) {
            if (!this.file.getParentFile().exists()) {
                this.file.getParentFile().mkdirs();
            }

            try (FileWriter writer = new FileWriter(this.file, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            } catch (IOException e) {
                MachineLib.LOGGER.error("Failed to save config file!", e);
            }
        }
    }
}
