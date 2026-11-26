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

package dev.galacticraft.machinelib.api.machine.security;

import dev.galacticraft.machinelib.impl.MachineLib;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Registry for team systems exposed to MachineLib.
 *
 * <p>A mod may register its team implementation during initialization and a
 * modpack may then select it using MachineLib's {@code teamSystem} config
 * option.</p>
 */
public final class TeamSystems {
    /**
     * The identifier of MachineLib's built-in vanilla team implementation.
     */
    public static final String MINECRAFT = "minecraft";

    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_.-]+");
    private static final Map<String, TeamSystem> SYSTEMS = new LinkedHashMap<>();

    private TeamSystems() {}

    /**
     * Registers a team system.
     *
     * <p>It is recommended that mods use their mod id as the team system id.</p>
     *
     * @param id     the unique team system id
     * @param system the team system implementation
     * @throws IllegalArgumentException if the id is invalid or already registered
     */
    public static void register(@NotNull String id, @NotNull TeamSystem system) {
        if (!VALID_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid team system id: " + id);
        }

        if (SYSTEMS.putIfAbsent(id, system) != null) {
            throw new IllegalArgumentException("Team system already registered: " + id);
        }
    }

    /**
     * {@return whether a team system with the supplied id is registered}
     *
     * @param id the team system id
     */
    @Contract(pure = true)
    public static boolean isRegistered(@NotNull String id) {
        return SYSTEMS.containsKey(id);
    }

    /**
     * Returns a registered team system.
     *
     * @param id the team system id
     * @return the registered implementation, or {@code null} if none exists
     */
    @Contract(pure = true)
    public static @Nullable TeamSystem get(@NotNull String id) {
        return SYSTEMS.get(id);
    }

    /**
     * {@return an immutable ordered set containing all registered team system ids}
     */
    @Contract(pure = true)
    public static @NotNull Set<String> getRegisteredIds() {
        return Collections.unmodifiableSet(SYSTEMS.keySet());
    }

    /**
     * {@return an immutable ordered view of all registered team systems}
     */
    @Contract(pure = true)
    public static @NotNull Map<String, TeamSystem> getRegistered() {
        return Collections.unmodifiableMap(SYSTEMS);
    }

    /**
     * Determines whether the supplied player is a teammate of the owner using
     * the currently configured team system.
     *
     * <p>If the configured implementation is unavailable, the built-in
     * {@value #MINECRAFT} implementation is used. If that implementation is also
     * unavailable, access is denied.</p>
     *
     * @param owner  the machine owner
     * @param player the player attempting access
     * @return whether the configured team system considers them teammates
     */
    public static boolean areTeammates(
            @NotNull UUID owner,
            @NotNull Player player
    ) {
        TeamSystem system = getConfigured();

        return system != null && system.areTeammates(owner, player);
    }

    /**
     * Returns the currently configured team system, falling back to vanilla.
     *
     * @return the active team system, or {@code null} if no usable implementation
     *         is registered
     */
    public static @Nullable TeamSystem getConfigured() {
        String configured = MachineLib.CONFIG.teamSystem();
        TeamSystem system = SYSTEMS.get(configured);

        if (system != null) {
            return system;
        }

        return SYSTEMS.get(MINECRAFT);
    }
}