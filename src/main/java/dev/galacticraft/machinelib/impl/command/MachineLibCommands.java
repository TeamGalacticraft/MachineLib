/*
 * Copyright (c) 2021-2024 Team Galacticraft
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

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.galacticraft.machinelib.api.wire.WireSegment;
import dev.galacticraft.machinelib.impl.attachment.MachineLibAttachmentTypes;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class MachineLibCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("machinelib")
                    .then(LiteralArgumentBuilder.<CommandSourceStack>literal("wire")
                            .then(LiteralArgumentBuilder.<CommandSourceStack>literal("rebuild").executes(ctx -> {
                                ctx.getSource().getLevel().getAttachedOrCreate(MachineLibAttachmentTypes.WIRE_NETWORK_MANAGER).rebuild(ctx);
                                return 1;
                            }))
                            .then(LiteralArgumentBuilder.<CommandSourceStack>literal("purge").executes(ctx -> {
                                ctx.getSource().getLevel().getAttachedOrCreate(MachineLibAttachmentTypes.WIRE_NETWORK_MANAGER).purge();
                                ctx.getSource().sendSystemMessage(Component.literal("network purged"));
                                return 1;
                            }))
                            .then(LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                                    .executes(ctx -> {
                                        ctx.getSource().getLevel().getAttachedOrCreate(MachineLibAttachmentTypes.WIRE_NETWORK_MANAGER).printInfo(ctx);
                                        return 1;
                                    })
                                    .then(RequiredArgumentBuilder.<CommandSourceStack, Coordinates>argument("pos", BlockPosArgument.blockPos())
                                            .executes(ctx -> {
                                                WireSegment segment = ctx.getSource().getLevel().getAttachedOrCreate(MachineLibAttachmentTypes.WIRE_NETWORK_MANAGER).getSegment(BlockPosArgument.getBlockPos(ctx, "pos"));
                                                ctx.getSource().sendSystemMessage(Component.literal(Objects.toString(segment)));
                                                return 1;
                                            })
                                    )
                            )
                    )
            );
        });
    }
}
