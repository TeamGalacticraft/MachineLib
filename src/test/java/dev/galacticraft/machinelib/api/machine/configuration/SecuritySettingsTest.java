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

package dev.galacticraft.machinelib.api.machine.configuration;

import dev.galacticraft.machinelib.test.MinecraftTest;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class SecuritySettingsTest implements MinecraftTest {
    private SecuritySettings settings;

    @BeforeEach
    void setup() {
        this.settings = new SecuritySettings();
    }

    @Test
    void tryUpdate() {
        UUID uuid = UUID.randomUUID();
        this.settings.tryUpdate(uuid);
        Assertions.assertEquals(uuid, this.settings.getOwner());
    }

    @Test
    void isOwner() {
        UUID owner = UUID.randomUUID();
        this.settings.owner = owner;
//        Assertions.assertEquals(owner, this.settings.isOwner());
    }

    @Test
    void hasAccess() {
    }

    @Test
    void getAccessLevel() {
    }

    @Test
    void setAccessLevel() {
    }

    @Test
    void getOwner() {
    }

    @Test
    void createTag() {
    }

    @Test
    void readTag() {
        Assertions.assertDoesNotThrow(() -> settings.readTag(new CompoundTag()));
    }

    @Test
    void writePacket() {
    }

    @Test
    void readPacket() {
    }
}