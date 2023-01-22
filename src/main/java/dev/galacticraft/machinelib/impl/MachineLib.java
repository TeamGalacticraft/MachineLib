/*
 * Copyright (c) 2021-2023 Team Galacticraft
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
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupType;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroupTypes;
import dev.galacticraft.machinelib.impl.network.MachineLibC2SPackets;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiStatus.Internal
public final class MachineLib implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Constant.MOD_NAME);
    public static final MappedRegistry<SlotGroupType> SLOT_GROUP_TYPE_REGISTRY = FabricRegistryBuilder.createSimple(SlotGroupType.class, new ResourceLocation(Constant.MOD_ID, "slot_group"))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();
    public static final DefaultedRegistry<MachineStatus> MACHINE_STATUS_REGISTRY = FabricRegistryBuilder.createDefaulted(MachineStatus.class, new ResourceLocation(Constant.MOD_ID, "machine_status"), new ResourceLocation(Constant.MOD_ID, "invalid"))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    @Override
    public void onInitialize() {
        Gases.init();
        MachineLibC2SPackets.register();
        MachineStatuses.initialize();
        SlotGroupTypes.initialize();
    }
}
