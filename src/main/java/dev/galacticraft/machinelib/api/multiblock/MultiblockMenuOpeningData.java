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