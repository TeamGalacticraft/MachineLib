/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.machinelib.impl.platform;

import dev.galacticraft.machinelib.api.component.ComponentTypes;
import dev.galacticraft.machinelib.api.util.FluidUnits;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.platform.services.PlatformHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;

public final class Services {
    public static final PlatformHelper PLATFORM = service(PlatformHelper.class);
    public static final ComponentTypes COMPONENT_TYPES = service(ComponentTypes.class);
    public static final FluidUnits FLUID_UNITS = service(FluidUnits.class);

    public static <T> @NotNull T service(Class<T> clazz) {
        final T service = ServiceLoader.load(clazz).findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constant.LOGGER.debug("Loaded {} for service {}", service.getClass().getName(), clazz);
        return service;
    }
}
