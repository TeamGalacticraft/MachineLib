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

package dev.galacticraft.machinelib.api.wire;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;

public class WireSegment {
    public static final Codec<WireSegment> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("x").forGetter(s -> s.x),
            Codec.INT.fieldOf("z").forGetter(s -> s.z),
            BlockPos.CODEC.listOf().fieldOf("ext").forGetter(s -> ImmutableList.copyOf(s.external.keySet())),
            StorageRef.CODEC.listOf().fieldOf("refs").forGetter(s -> s.storageRefs.entrySet().stream().map(e -> new StorageRef(e.getKey(), e.getValue())).toList())
    ).apply(i, WireSegment::new));

    final int x;
    final int z;

    // assertion: only one direction is possible as these are all outside chunk borders
    final Map<BlockPos, BlockApiCache<EnergyStorage, Direction>> external; // storage pos -> cache
    final Map<BlockPos, EnumSet<Direction>> storageRefs; // storage pos -> direction OUT OF storage

    public WireSegment(int x, int z) {
        this.x = x;
        this.z = z;
        this.external = new HashMap<>();
        this.storageRefs = new HashMap<>();
    }

    public WireSegment(int x, int z, ServerLevel level, Map<BlockPos, EnumSet<Direction>> refs) {
        this.x = x;
        this.z = z;
        this.storageRefs = new HashMap<>(refs);
        this.external = new HashMap<>();
        for (Iterator<Map.Entry<BlockPos, EnumSet<Direction>>> iterator = refs.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<BlockPos, EnumSet<Direction>> entry = iterator.next();
            if (isExternal(entry.getKey())) {
                external.put(entry.getKey(), BlockApiCache.create(EnergyStorage.SIDED, level, entry.getKey()));
                iterator.remove();
            }
        }
    }

    public WireSegment(int x, int z, List<BlockPos> ext, List<StorageRef> storageRefs) {
        this.x = x;
        this.z = z;
        this.external = new HashMap<>();
        this.storageRefs = new HashMap<>();

        for (BlockPos blockPos : ext) {
            this.external.put(blockPos, null);
        }
        for (StorageRef storageRef : storageRefs) {
            this.storageRefs.put(storageRef.pos, storageRef.dirs);
        }
    }

    public long tryAccept(List<WireNetworkManager.EnergyRequest> requests, Level level, long amount, TransactionContext transaction) {
        ChunkAccess chunk = level.getChunk(this.x, this.z, ChunkStatus.FULL, false);
        if (chunk == null) return 0;

        long requested = 0;
        for (Map.Entry<BlockPos, EnumSet<Direction>> entry : storageRefs.entrySet()) {
            for (Direction direction : entry.getValue()) {
                EnergyStorage energyStorage = EnergyStorage.SIDED.find(level, entry.getKey(), chunk.getBlockState(entry.getKey()), chunk.getBlockEntity(entry.getKey()), direction);
                if (energyStorage != null) {
                    long inserted = energyStorage.insert(amount, transaction);
                    if (inserted > 0) {
                        requests.add(new WireNetworkManager.EnergyRequest(energyStorage, inserted));
                        requested += inserted;
                    }
                }
            }
        }

        if (this.external.containsKey(null)) {
            this.external.replaceAll((p, k) -> k != null ? k : BlockApiCache.create(EnergyStorage.SIDED, (ServerLevel) level, p));
        }

        for (Map.Entry<BlockPos, BlockApiCache<EnergyStorage, Direction>> entry : external.entrySet()) {
            if (level.isLoaded(entry.getKey())) {
                Direction direction = Direction.fromDelta(this.x - entry.getKey().getX() >> 4, 0, this.z - entry.getKey().getZ() >> 4);
                EnergyStorage energyStorage = EnergyStorage.SIDED.find(level, entry.getKey(), chunk.getBlockState(entry.getKey()), chunk.getBlockEntity(entry.getKey()), direction);
                if (energyStorage != null) {
                    long inserted = energyStorage.insert(amount, transaction);
                    if (inserted > 0) {
                        requests.add(new WireNetworkManager.EnergyRequest(energyStorage, inserted));
                        requested += inserted;
                    }
                }
            }
        }

        return requested;
    }

    private boolean isExternal(BlockPos pos) {
        return pos.getX() >> 4 != this.x
            || pos.getZ() >> 4 != this.z;
    }

    @Override
    public String toString() {
        return "WireSegment@" + System.identityHashCode(this) + "{" +
                "x=" + x +
                ", z=" + z +
                ", external=" + external +
                ", storageRefs=" + storageRefs +
                '}';
    }

    public @Nullable EnergyStorage storage(WireNetworkManager manager, Level level, @Nullable Direction context) {
        if (context == null) return null;
        return new EnergyStorage() {
            @Override
            public long insert(long maxAmount, TransactionContext transaction) {
                return manager.accept(WireSegment.this, level, maxAmount, transaction);
            }

            @Override
            public long extract(long maxAmount, TransactionContext transaction) {
                return 0;
            }

            @Override
            public long getAmount() {
                return 0;
            }

            @Override
            public long getCapacity() {
                return 0;
            }
        };
    }

    public void addEndpoint(ServerLevel level, BlockPos pos, Direction direction) {
        if (isExternal(pos)) {
            this.external.put(pos, BlockApiCache.create(EnergyStorage.SIDED, level, pos));
        } else {
            this.storageRefs.computeIfAbsent(pos, k -> EnumSet.noneOf(Direction.class)).add(direction);
        }
    }

    public void removeEndpoint(BlockPos pos, Direction direction) {
        this.external.remove(pos); // if the position is outside, there's only one way to get to it

        EnumSet<Direction> directions = this.storageRefs.get(pos);
        if (directions != null) {
            if (directions.size() > 1) {
                directions.remove(direction);
            } else {
                this.storageRefs.remove(pos);
            }
        }
    }

    record StorageRef(BlockPos pos, EnumSet<Direction> dirs) {
        private static final Codec<StorageRef> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(StorageRef::pos),
                Codec.BYTE.fieldOf("dirs").forGetter(r -> encodeDirs(r.dirs))
        ).apply(i, StorageRef::new));

        StorageRef(BlockPos pos, byte dirs) {
            this(pos, decodeDirs(dirs));
        }

        private static byte encodeDirs(EnumSet<Direction> value) {
            byte out = 0;
            for (Direction direction : value) {
                out |= (byte) (1 << direction.get3DDataValue());
            }
            return out;
        }

        private static EnumSet<Direction> decodeDirs(byte out) {
            EnumSet<Direction> value = EnumSet.noneOf(Direction.class);
            for (int i = 0; i < 6; i++) {
                if ((out & (1 << i)) != 0) {
                    value.add(Direction.from3DDataValue(i));
                }
            }
            return value;
        }
    }
}
