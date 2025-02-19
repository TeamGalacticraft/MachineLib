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

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.google.common.graph.Traverser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.galacticraft.machinelib.impl.MachineLib;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class WireNetworkManager {
    public static final Codec<WireNetworkManager> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<WireNetworkManager, T>> decode(DynamicOps<T> dynamicOps, T t) {
            CompoundTag tag = (CompoundTag) dynamicOps.convertMap(NbtOps.INSTANCE, t);
            List<WireSegment> segments = new ArrayList<>();
            for (Tag node : tag.getList("nodes", Tag.TAG_COMPOUND)) {
                segments.add(WireSegment.CODEC.decode(NbtOps.INSTANCE, node).getOrThrow().getFirst());
            }
            List<NetworkConnection> edges = new ArrayList<>();
            for (Tag node : tag.getList("edges", Tag.TAG_COMPOUND)) {
                edges.add(NetworkConnection.CODEC.decode(NbtOps.INSTANCE, node).getOrThrow().getFirst());
            }

            return DataResult.success(new Pair<>(new WireNetworkManager(
                    segments, tag.getLongArray("wires"), tag.getIntArray("wire_mappings"), edges
            ), t));
        }

        @Override
        public <T> DataResult<T> encode(WireNetworkManager manager, DynamicOps<T> dynamicOps, T t) {
            // get some sort of order
            List<WireSegment> wireSegments = new ArrayList<>(manager.networks.nodes());
            List<Map.Entry<BlockPos, WireSegment>> entries = new ArrayList<>(manager.wires.entrySet());

            return dynamicOps.mapBuilder()
                    .add("wires", dynamicOps.createLongList(entries.stream().mapToLong(e -> e.getKey().asLong())))
                    .add("wire_mappings", dynamicOps.createIntList(entries.stream().mapToInt(e -> wireSegments.indexOf(e.getValue())))) //fixme slow
                    .add("nodes", dynamicOps.createList(wireSegments.stream().map(s -> WireSegment.CODEC.encodeStart(dynamicOps, s).getOrThrow())))
                    .add("edges", dynamicOps.createList(manager.networks.edges().stream().map(e -> NetworkConnection.CODEC.encodeStart(dynamicOps, e).getOrThrow())))
                    .build(t);
        }
    };

    private final Map<BlockPos, WireSegment> wires = new HashMap<>();

    private final MutableNetwork<WireSegment, NetworkConnection> networks = NetworkBuilder.undirected().allowsParallelEdges(true).build();
    private final Set<WireSegment> locked = new HashSet<>();

    private WireNetworkManager(List<WireSegment> segments, long[] wires, int[] wireMappings, List<NetworkConnection> edges) {
        for (WireSegment segment : segments) {
            this.networks.addNode(segment);
        }
        for (int i = 0; i < wireMappings.length; i++) {
            this.wires.put(BlockPos.of(wires[i]), segments.get(wireMappings[i]));
        }
        for (NetworkConnection edge : edges) {
            this.networks.addEdge(this.wires.get(edge.first), this.wires.get(edge.second), edge);
        }
    }

    public WireNetworkManager() {
    }

    public @Nullable WireSegment getSegment(BlockPos pos) {
        return this.wires.get(pos);
    }

    private void wireRemoved(Level level, BlockPos pos) {
        WireSegment segment = this.wires.remove(pos);
        if (segment == null) return;

        List<BlockPos> adjacent = new ArrayList<>();
        for (Direction value : Direction.values()) {
            BlockPos relative = pos.relative(value);
            if (this.getSegment(relative) == segment) {
                adjacent.add(relative);
            }
        }

        Set<BlockPos> connected = new HashSet<>();
        Map<BlockPos, EnumSet<Direction>> connectedRefs = new HashMap<>();

        Set<NetworkConnection> networkConnections = new HashSet<>(this.networks.incidentEdges(segment));
        this.networks.removeNode(segment);

        while (!adjacent.isEmpty()) {
            connected.clear();
            connectedRefs.clear();
            traverse(connected, connectedRefs, level, adjacent.removeLast(), null);
            WireSegment newSegment = new WireSegment(segment.x, segment.z, (ServerLevel) level, connectedRefs);

            adjacent.removeAll(connected);

            for (BlockPos blockPos : connected) {
                this.wires.put(blockPos, newSegment);
            }

            this.networks.addNode(newSegment);
            for (NetworkConnection edge : networkConnections) {
                if (connected.contains(edge.first)) {
                    this.networks.addEdge(newSegment, this.wires.get(edge.second), edge);
                } else if (connected.contains(edge.second)) {
                    this.networks.addEdge(newSegment, this.wires.get(edge.first), edge);
                }
            }
        }
    }

    public long accept(WireSegment source, Level level, long amount, TransactionContext context) {
        if (this.locked.contains(source)) return 0;
        level.getProfiler().push("wire_network");
        level.getProfiler().push("traversal");
        ImmutableSet<WireSegment> network = ImmutableSet.copyOf(Traverser.forGraph(this.networks).breadthFirst(source));
        level.getProfiler().pop();
        try {
            this.locked.addAll(network);

            List<EnergyRequest> requests = new ArrayList<>(32);
            long requested = 0;

            try (Transaction transaction = Transaction.openNested(context)) {
                for (WireSegment segment : network) {
                    requested += segment.tryAccept(requests, level, amount, transaction);
                }
                transaction.abort();
            }

            if (requested == 0) return 0;

            double multiplier = (double) amount / requested;
            long distributed = 0;
            try (Transaction transaction = Transaction.openNested(context)) {
                for (EnergyRequest request : requests) {
                    distributed += request.storage.insert((long) (request.amount * multiplier), transaction);
                }

                if (distributed <= amount) transaction.commit();
                else return 0;
            }

            return amount - distributed;
        } finally {
            this.locked.removeAll(network);
            level.getProfiler().pop();
        }
    }

    public static void traverse(Set<BlockPos> visited, Map<BlockPos, EnumSet<Direction>> visitedEndpoints, Level level, BlockPos start, @Nullable BlockPos until) {
        List<BlockPos> queue = new ArrayList<>();
        queue.add(start);
        visited.add(start);

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        while (!queue.isEmpty()) {
            BlockPos target = queue.removeLast();
            BlockState state = level.getBlockState(target);

            // enqueue all adjacent wires
            for (Direction direction : Direction.values()) {
                mutable.setWithOffset(target, direction);
                if (!visited.contains(mutable) && start.getX() >> 4 == mutable.getX() >> 4 && start.getZ() >> 4 == mutable.getZ() >> 4) {
                    if (isPhysicallyConnected(state, direction, level.getBlockState(mutable))) {
                        BlockPos immutable = mutable.immutable();
                        visited.add(immutable);
                        if (mutable.equals(until)) return;
                        queue.addLast(immutable);
                    } else if (isEndpointConnected(state, direction) && !(level.getBlockState(mutable).getBlock() instanceof WireBlock)) {
                        visitedEndpoints.computeIfAbsent(target.immutable(), k -> EnumSet.noneOf(Direction.class)).add(direction.getOpposite());
                    }
                }
            }
        }
    }

    // direction to updated neighbor
    public void updateWire(Level level, BlockPos pos, BlockState state, Direction direction, BlockPos adjPos, BlockState adjState) {
        WireSegment segment = this.getSegment(pos);
        if (segment == null) {
            return;
//            throw new AssertionError();
        }

        WireSegment adjSegment = this.getSegment(adjPos);
        if (adjSegment != null) { // check for wire
            if (sameChunk(pos.getX() >> 4, pos.getZ() >> 4, adjPos)) { // internal - mutate network segment
                if (isPhysicallyConnected(state, direction, adjState)) {
                    if (adjSegment == segment) return; // connection already exists - no change.
                    mergeSegments(adjSegment, segment); // newly created connection
                } else {
                    if (!(adjState.getBlock() instanceof WireBlock)) {
                        this.wireRemoved(level, adjPos); // wire removed - can fast track
                    } else if (adjSegment == segment) {
                        this.splitSegment(pos, direction, level, segment); // connection deleted
                    } //otherwise, no previous connection - no change.
                }
            } else { // external - mutate segment graph
                if (isPhysicallyConnected(state, direction, adjState)) {
                    this.networks.addEdge(segment, adjSegment, new NetworkConnection(pos, adjPos));
                } else {
                    this.networks.removeEdge(new NetworkConnection(pos, adjPos));
                }
            }
        } else if (!(adjState.getBlock() instanceof WireBlock)) { // check for endpoint (internal/external endpoints handled by segment)
            if (isEndpointConnected(state, direction) && !(level.getBlockState(adjPos).getBlock() instanceof WireBlock)) {
                segment.addEndpoint((ServerLevel) level, adjPos, direction.getOpposite());
            } else {
                segment.removeEndpoint(adjPos, direction.getOpposite());
            }
        }
    }

    public void newWire(Level level, BlockPos pos, BlockState state) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        // network and segment to apply to the wire
        WireSegment segment = null;
//        for (Direction direction : Direction.values()) {
//            if (state.getValue(WireBlock.CONNECTIONS[direction.get3DDataValue()])) {
//                WireSegment adj = this.getSegment(mutablePos.setWithOffset(pos, direction));
//                if (adj != null) {
//                    if (this.verifyValidConnection(level, pos, direction)) {
//                        if (sameChunk(pos.getX() >> 4, pos.getZ() >> 4, mutablePos)) {
//                            if (segment == null) {
//                                segment = adj;
//                            } else if (segment != adj) { // merge segments
//                                mergeSegments(adj, segment);
//                            }
//                        }
//                    }
//                }
//            }
//        }
        if (segment == null) {
            segment = new WireSegment(pos.getX() >> 4, pos.getZ() >> 4);
            this.networks.addNode(segment);
            this.wires.put(pos, segment);
        }

//        for (Direction direction : Direction.values()) {
//            if (state.getValue(WireBlock.CONNECTIONS[direction.get3DDataValue()])) {
//                WireSegment adj = this.getSegment(mutablePos.setWithOffset(pos, direction));
//                if (adj != null) {
//                    if (this.verifyValidConnection(level, pos, direction)) {
//                        if (!sameChunk(pos.getX() >> 4, pos.getZ() >> 4, mutablePos)) {
//                            this.networks.addEdge(segment, adj, new NetworkConnection(pos, mutablePos.immutable()));
//                        }
//                    }
//                }
//            }
//        }
    }

    private void mergeSegments(WireSegment adj, WireSegment segment) {
        segment.external.putAll(adj.external);
        segment.storageRefs.putAll(adj.storageRefs);
        adj.external.clear();
        adj.storageRefs.clear();

        this.wires.replaceAll((p, s) -> s == adj ? segment : s);

        Set<NetworkConnection> successors = new HashSet<>(this.networks.incidentEdges(adj));
        this.networks.removeNode(adj);
        for (NetworkConnection successor : successors) {
            this.networks.addEdge(this.wires.get(successor.first), this.wires.get(successor.second), successor);
        }
    }

    private void splitSegment(BlockPos disconnected, Direction disconnectDir, Level level, WireSegment segment) {
        Set<BlockPos> connected = new HashSet<>();
        Map<BlockPos, EnumSet<Direction>> connectedRefs = new HashMap<>();
        BlockPos disconnectPeer = disconnected.relative(disconnectDir);
        traverse(connected, connectedRefs, level, disconnected, disconnectPeer);
        if (connected.contains(disconnectPeer)) return; // no change.

        Set<NetworkConnection> networkConnections = new HashSet<>(this.networks.incidentEdges(segment));
        this.networks.removeNode(segment);

        {
            WireSegment segment1 = new WireSegment(segment.x, segment.z, (ServerLevel) level, connectedRefs);
            this.networks.addNode(segment1);
            for (NetworkConnection edge : networkConnections) {
                if (connected.contains(edge.first)) {
                    this.networks.addEdge(segment1, this.wires.get(edge.second), edge);
                } else if (connected.contains(edge.second)) {
                    this.networks.addEdge(segment1, this.wires.get(edge.first), edge);
                }
            }
        }

        {
            connected.clear();
            connectedRefs.clear();
            traverse(connected, connectedRefs, level, disconnectPeer, null);
            WireSegment segment2 = new WireSegment(segment.x, segment.z, (ServerLevel) level, connectedRefs);
            this.networks.addNode(segment2);
            for (NetworkConnection edge : networkConnections) {
                if (connected.contains(edge.first)) {
                    this.networks.addEdge(segment2, this.wires.get(edge.second), edge);
                } else if (connected.contains(edge.second)) {
                    this.networks.addEdge(segment2, this.wires.get(edge.first), edge);
                }
            }
        }
    }

//    private boolean checkValid(Level level, BlockPos pos, Direction direction) {
//        BlockPos relative = pos.relative(direction);
//        BlockState state = level.getBlockState(relative);
//        if (state.getBlock() instanceof WireBlock wb) {
//            return state.getValue(WireBlock.CONNECTIONS[direction.getOpposite().get3DDataValue()]);
//        }
//        MachineLib.LOGGER.warn("Removed {} ghost wires", traverseValidate(level, relative));
//        this.traverseValidate(level, relative);
//
//        return false;
//    }

    private boolean verifyValidConnection(Level level, BlockPos pos, Direction direction) {
        BlockPos relative = pos.relative(direction);
        BlockState state = level.getBlockState(relative);
        if (state.getOptionalValue(WireBlock.CONNECTIONS[direction.getOpposite().get3DDataValue()]).orElse(false)) {
            return true;
        }
        MachineLib.LOGGER.warn("Invalid connection at {}", relative);

        return false;
    }

//    private int traverseValidate(Level level, BlockPos pos) {
//        int invalid = 0;
//        List<BlockPos> queue = new ArrayList<>();
//        Set<BlockPos> visited = new HashSet<>();
//        queue.add(pos);
//        visited.add(pos);
//
//        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
//        while (!queue.isEmpty()) {
//            BlockPos target = queue.removeLast();
//            if (!(level.getBlockState(target).getBlock() instanceof WireBlock)) {
//                // remove ghost wire
//                this.removeWire(target);
//                invalid++;
//
//                // enqueue all adjacent wires
//                for (Direction direction : Direction.values()) {
//                    mutable.setWithOffset(target, direction);
//                    if (!visited.contains(mutable) && this.getSegment(mutable) != null) {
//                        queue.addLast(mutable.immutable());
//                        visited.add(mutable.immutable());
//                    }
//                }
//            } else {
//                // the position is valid. don't check again
//                visited.add(target);
//            }
//        }
//        return invalid;
//    }

    protected static boolean isPhysicallyConnected(BlockState state, Direction direction, BlockState adjState) {
        return state.getValue(WireBlock.CONNECTIONS[direction.get3DDataValue()]) && adjState.getOptionalValue(WireBlock.CONNECTIONS[direction.getOpposite().get3DDataValue()]).orElse(false);
    }

    protected static boolean isEndpointConnected(BlockState state, Direction direction) {
        return state.getValue(WireBlock.CONNECTIONS[direction.get3DDataValue()]);
    }

    protected boolean sameChunk(int x, int z, BlockPos pos) {
        return pos.getX() >> 4 == x && pos.getZ() >> 4 == z;
    }

    private record NetworkConnection(BlockPos first, BlockPos second) {
        private static final Codec<NetworkConnection> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("a").forGetter(NetworkConnection::first),
                BlockPos.CODEC.fieldOf("b").forGetter(NetworkConnection::second)
        ).apply(i, NetworkConnection::new));

        private NetworkConnection(BlockPos first, BlockPos second) {
            if (Math.abs(first.getX() - second.getX() + first.getY() - second.getY() + first.getZ() - second.getZ()) != 1)
                throw new UnsupportedOperationException();

            if (first.getX() < second.getX() || first.getY() < second.getY() || first.getZ() < second.getZ()) {
                this.first = first;
                this.second = second;
            } else {
                this.second = first;
                this.first = second;
            }
        }
    }

    @Override
    public String toString() {
        return "WireNetworkManager{" +
                "\nwires=" + wires +
                ",\n networks=" + networks +
                "\n}";
    }

    public record EnergyRequest(EnergyStorage storage, long amount) {}
}
