/*
 * Copyright (c) 2019-2022 Team Galacticraft
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

package dev.galacticraft.test;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.spongepowered.asm.launch.MixinBootstrap;

public class BootstrapMinecraft implements BeforeAllCallback {
    private static boolean initialized = false;

    @Override
    public synchronized void beforeAll(ExtensionContext context) {
        if (!BootstrapMinecraft.initialized) {
            BootstrapMinecraft.initialized = true;
            try {
                SharedConstants.createGameVersion();
                MixinBootstrap.init();
                Knot.launch(new String[] {"nogui"}, EnvType.SERVER);
                System.out.println("Memory: " + Runtime.getRuntime().maxMemory() / 1000000.0 + "MB");
                System.setProperty(SystemProperties.DEVELOPMENT, "true");
                Bootstrap.initialize();
            } catch (IllegalArgumentException e) {
                System.out.println("print");
                e.printStackTrace();
                System.out.println("end print");
            }
        }
    }
}
