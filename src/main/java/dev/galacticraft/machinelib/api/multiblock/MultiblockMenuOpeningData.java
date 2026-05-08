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

package dev.galacticraft.machinelib.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Data sent from server to client when opening a multiblock menu.
 *
 * @param instanceId formed multiblock instance id
 * @param definitionId multiblock definition id
 * @param origin formed multiblock origin
 * @param clickedPos clicked part position
 * @param orientation formed orientation
 */
public record MultiblockMenuOpeningData(
        UUID instanceId,
        ResourceLocation definitionId,
        BlockPos origin,
        BlockPos clickedPos,
        MultiblockOrientation orientation
) {
    private static final StreamCodec<RegistryFriendlyByteBuf, MultiblockOrientation> ORIENTATION_CODEC =
            StreamCodec.of(
                    (buffer, orientation) -> ByteBufCodecs.VAR_INT.encode(buffer, orientation.ordinal()),
                    buffer -> MultiblockOrientation.values()[ByteBufCodecs.VAR_INT.decode(buffer)]
            );

    /**
     * Stream codec for multiblock menu opening data.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, MultiblockMenuOpeningData> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    MultiblockMenuOpeningData::instanceId,
                    ResourceLocation.STREAM_CODEC,
                    MultiblockMenuOpeningData::definitionId,
                    BlockPos.STREAM_CODEC,
                    MultiblockMenuOpeningData::origin,
                    BlockPos.STREAM_CODEC,
                    MultiblockMenuOpeningData::clickedPos,
                    ORIENTATION_CODEC,
                    MultiblockMenuOpeningData::orientation,
                    MultiblockMenuOpeningData::new
            );

    private static final class UUIDUtil {

        private static final StreamCodec<RegistryFriendlyByteBuf, UUID> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_LONG,
                        UUID::getMostSignificantBits,
                        ByteBufCodecs.VAR_LONG,
                        UUID::getLeastSignificantBits,
                        UUID::new
                );

        private UUIDUtil() {

        }

    }
}