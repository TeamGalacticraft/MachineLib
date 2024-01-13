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

package dev.galacticraft.machinelib.impl.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import dev.galacticraft.machinelib.api.config.Config;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MachineLibConfig implements Config {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public MachineLibConfig() {}

    @Expose
    public boolean enableColouredVanillaFluidNames = true;
    @Expose
    public long bucketBreakpoint = FluidConstants.BUCKET * 100;

    @Override
    public boolean enableColouredVanillaFluidNames() {
        return this.enableColouredVanillaFluidNames;
    }

    @Override
    public long bucketBreakpoint() {
        return this.bucketBreakpoint;
    }

    @Override
    public void copyFrom(Config config) {
        this.enableColouredVanillaFluidNames = config.enableColouredVanillaFluidNames();
        this.bucketBreakpoint = config.bucketBreakpoint();
    }

    @Override
    public void loadFromFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            MachineLibConfig config = GSON.fromJson(reader, MachineLibConfig.class);
            this.copyFrom(config);
        } catch (IOException e) {
            MachineLib.LOGGER.error("Failed to read config file!", e);
        }
    }

    @Override
    public void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            MachineLib.LOGGER.error("Failed to save config file!", e);
        }
    }
}
