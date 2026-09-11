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

package dev.galacticraft.machinelib.impl.config;

import dev.galacticraft.machinelib.api.config.Config;
import dev.galacticraft.machinelib.test.MinecraftTest;
import net.fabricmc.loader.api.FabricLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MachineLibConfigTest implements MinecraftTest {
    private static final File FILE = FabricLoader.getInstance().getGameDir().resolve(".machinelib_test_config.json").toFile();

    private Config config;

    @BeforeEach
    public void setup() {
        FILE.delete();
        this.config = Config.loadFrom(FILE);
    }

    @AfterEach
    public void cleanup() {
        FILE.delete();
    }

    @Test
    public void saveOnOpen() {
        assertTrue(FILE.exists());
    }

    @Test
    public void load() {
        boolean enabled = !this.config.enableColoredVanillaFluidNames();
        this.config.setEnableColoredVanillaFluidNames(enabled);
        this.config.save();

        Config newConfig = Config.loadFrom(FILE);
        assertEquals(enabled, newConfig.enableColoredVanillaFluidNames());
    }

    @Test
    public void reload() {
        boolean enabled = !this.config.enableColoredVanillaFluidNames();
        this.config.setEnableColoredVanillaFluidNames(enabled);
        this.config.reload();

        assertNotEquals(enabled, this.config.enableColoredVanillaFluidNames());
    }
}
