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

package dev.galacticraft.machinelib.impl.machine.security;

import com.mojang.authlib.GameProfile;
import dev.galacticraft.machinelib.api.machine.security.TeamSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Team system backed by vanilla Minecraft scoreboard teams ({@code /team}).
 */
@ApiStatus.Internal
public final class VanillaTeamSystem implements TeamSystem {
    public static final VanillaTeamSystem INSTANCE = new VanillaTeamSystem();

    private VanillaTeamSystem() {}

    @Override
    public boolean areTeammates(
            @NotNull UUID owner,
            @NotNull Player player
    ) {
        MinecraftServer server = player.getServer();

        if (server == null) {
            return false;
        }

        GameProfileCache cache = server.getProfileCache();

        if (cache == null) {
            return false;
        }

        Optional<GameProfile> ownerProfile = cache.get(owner);

        if (ownerProfile.isEmpty()) {
            return false;
        }

        PlayerTeam ownerTeam = server.getScoreboard().getPlayersTeam(
                ownerProfile.get().getName()
        );

        return ownerTeam != null && ownerTeam == player.getTeam();
    }
}