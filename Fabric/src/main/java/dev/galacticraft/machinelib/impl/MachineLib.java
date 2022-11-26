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

package dev.galacticraft.machinelib.impl;

import dev.galacticraft.machinelib.api.gas.Gases;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.impl.network.MachineLibC2SPackets;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.ApiStatus;

import static dev.galacticraft.machinelib.api.gas.Gases.*;

@ApiStatus.Internal
public final class MachineLib implements ModInitializer {
    @Override
    public void onInitialize() {
        Gases.init();
        MachineLibC2SPackets.register();
        MachineStatuses.init();


        Registry.register(Registry.FLUID, HYDROGEN_ID, HYDROGEN);
        Registry.register(Registry.FLUID, NITROGEN_ID, NITROGEN);
        Registry.register(Registry.FLUID, OXYGEN_ID, OXYGEN);
        Registry.register(Registry.FLUID, CARBON_DIOXIDE_ID, CARBON_DIOXIDE);
        Registry.register(Registry.FLUID, WATER_VAPOR_ID, WATER_VAPOR);
        Registry.register(Registry.FLUID, METHANE_ID, METHANE);
        Registry.register(Registry.FLUID, HELIUM_ID, HELIUM);
        Registry.register(Registry.FLUID, ARGON_ID, ARGON);
        Registry.register(Registry.FLUID, NEON_ID, NEON);
        Registry.register(Registry.FLUID, KRYPTON_ID, KRYPTON);
        Registry.register(Registry.FLUID, NITROUS_OXIDE_ID, NITROUS_OXIDE);
        Registry.register(Registry.FLUID, CARBON_MONOXIDE_ID, CARBON_MONOXIDE);
        Registry.register(Registry.FLUID, XENON_ID, XENON);
        Registry.register(Registry.FLUID, OZONE_ID, OZONE);
        Registry.register(Registry.FLUID, NITROUS_DIOXIDE_ID, NITROUS_DIOXIDE);
        Registry.register(Registry.FLUID, IODINE_ID, IODINE);
    }
}
