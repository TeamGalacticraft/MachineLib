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

package dev.galacticraft.machinelib.impl.command;

import dev.galacticraft.machinelib.api.machine.security.TeamSystems;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class MachineLibCommands {
    private MachineLibCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        dispatcher.register(
                                Commands.literal("machinelib")
                                        .then(
                                                Commands.literal("exposedTeamSystems")
                                                        .executes(context -> {
                                                            String configured =
                                                                    MachineLib.CONFIG.teamSystem();

                                                            context.getSource().sendSuccess(
                                                                    () -> Component.literal(
                                                                            "Registered MachineLib team systems:"
                                                                    ),
                                                                    false
                                                            );

                                                            for (String id :
                                                                    TeamSystems.getRegisteredIds()) {
                                                                boolean active =
                                                                        id.equals(configured)
                                                                                || !TeamSystems.isRegistered(configured)
                                                                                && id.equals(TeamSystems.MINECRAFT);

                                                                Component message =
                                                                        Component.literal(
                                                                                " - " + id
                                                                        );

                                                                if (active) {
                                                                    message = message.copy()
                                                                            .append(
                                                                                    Component.literal(
                                                                                            " [ACTIVE]"
                                                                                    ).withStyle(
                                                                                            ChatFormatting.GREEN
                                                                                    )
                                                                            );
                                                                }

                                                                Component finalMessage = message;

                                                                context.getSource().sendSuccess(
                                                                        () -> finalMessage,
                                                                        false
                                                                );
                                                            }

                                                            if (!TeamSystems.isRegistered(configured)) {
                                                                context.getSource().sendSuccess(
                                                                        () -> Component.literal(
                                                                                "Configured team system '"
                                                                                        + configured
                                                                                        + "' is not registered; using '"
                                                                                        + TeamSystems.MINECRAFT
                                                                                        + "'."
                                                                        ).withStyle(
                                                                                ChatFormatting.YELLOW
                                                                        ),
                                                                        false
                                                                );
                                                            }

                                                            return TeamSystems
                                                                    .getRegisteredIds()
                                                                    .size();
                                                        })
                                        )
                        )
        );
    }
}