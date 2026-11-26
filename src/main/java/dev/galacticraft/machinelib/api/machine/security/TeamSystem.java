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

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Provides team membership information for MachineLib security checks.
 *
 * <p>Implementations are registered through {@link TeamSystems}. MachineLib
 * selects one registered implementation using its {@code teamSystem} config
 * option.</p>
 */
@FunctionalInterface
public interface TeamSystem {
    /**
     * Determines whether the supplied player is considered a teammate of the
     * machine owner.
     *
     * <p>The owner check itself is performed by MachineLib before this method is
     * called. Implementations therefore only need to determine team membership.</p>
     *
     * @param owner  the UUID of the machine owner
     * @param player the player attempting to access the machine
     * @return whether the player is a teammate of the owner
     */
    boolean areTeammates(@NotNull UUID owner, @NotNull Player player);
}