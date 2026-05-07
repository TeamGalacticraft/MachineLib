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

package dev.galacticraft.machinelib.impl.network;

import dev.galacticraft.machinelib.impl.network.c2s.AccessLevelPayload;
import dev.galacticraft.machinelib.impl.network.c2s.RedstoneModePayload;
import dev.galacticraft.machinelib.impl.network.c2s.SideConfigurationClickPayload;
import dev.galacticraft.machinelib.impl.network.c2s.TankInteractionPayload;
import dev.galacticraft.machinelib.impl.network.s2c.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class MachineLibPackets {
    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(AccessLevelPayload.TYPE, AccessLevelPayload::apply);
        ServerPlayNetworking.registerGlobalReceiver(RedstoneModePayload.TYPE, RedstoneModePayload::apply);
        ServerPlayNetworking.registerGlobalReceiver(SideConfigurationClickPayload.TYPE, SideConfigurationClickPayload::apply);
        ServerPlayNetworking.registerGlobalReceiver(TankInteractionPayload.TYPE, TankInteractionPayload::apply);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(BaseMachineUpdatePayload.TYPE, BaseMachineUpdatePayload::apply);
        ClientPlayNetworking.registerGlobalReceiver(SideConfigurationUpdatePayload.TYPE, SideConfigurationUpdatePayload::apply);
        ClientPlayNetworking.registerGlobalReceiver(MenuSyncPayload.TYPE, MenuSyncPayload::apply);
        ClientPlayNetworking.registerGlobalReceiver(MultiblockSyncAddPayload.TYPE, MultiblockSyncAddPayload::apply);
        ClientPlayNetworking.registerGlobalReceiver(MultiblockSyncRemovePayload.TYPE, MultiblockSyncRemovePayload::apply);
    }

    public static void registerChannels() {
        // c2s
        PayloadTypeRegistry.playC2S().register(AccessLevelPayload.TYPE, AccessLevelPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RedstoneModePayload.TYPE, RedstoneModePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SideConfigurationClickPayload.TYPE, SideConfigurationClickPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TankInteractionPayload.TYPE, TankInteractionPayload.CODEC);

        // s2c
        PayloadTypeRegistry.playS2C().register(BaseMachineUpdatePayload.TYPE, BaseMachineUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SideConfigurationUpdatePayload.TYPE, SideConfigurationUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MenuSyncPayload.TYPE, MenuSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MultiblockSyncAddPayload.TYPE, MultiblockSyncAddPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MultiblockSyncRemovePayload.TYPE, MultiblockSyncRemovePayload.CODEC);
    }
}
