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

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.galacticraft.machinelib.impl.MachineLib;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
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
                    .add("wire_mappings", dynamicOps.createIntList(entries.stream().mapToInt(e -> wireSegments.indexOf(e.getValue())))) //fixme slow, but only called on world close
                    .add("nodes", dynamicOps.createList(wireSegments.stream().map(s -> WireSegment.CODEC.encodeStart(dynamicOps, s).getOrThrow())))
                    .add("edges", dynamicOps.createList(manager.networks.edges().stream().map(e -> NetworkConnection.CODEC.encodeStart(dynamicOps, e).getOrThrow())))
                    .build(t);
        }
    };

    private static final EnergyStorage CLIENT_DUMMY_STORAGE = new EnergyStorage() {
        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            return 0;
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
            return 1;
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

    public @NotNull EnergyStorage getStorage(Level level, BlockPos pos) {
        return level.isClientSide ? CLIENT_DUMMY_STORAGE : new WireEnergyStorage(pos, (ServerLevel) level);
    }

    public void removeWire(ServerLevel level, BlockPos pos) {
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
        Set<NetworkConnection> peerSegments = new HashSet<>();
        Map<BlockPos, EnumSet<Direction>> connectedRefs = new HashMap<>();

        this.networks.removeNode(segment);

        while (!adjacent.isEmpty()) {
            connected.clear();
            connectedRefs.clear();
            peerSegments.clear();

            traverseWires(connected, connectedRefs, peerSegments, level, adjacent.removeLast(), null, segment.capacity);
            WireSegment newSegment = new WireSegment(segment.x, segment.z, level, segment.capacity, connectedRefs);

            adjacent.removeAll(connected);

            for (BlockPos blockPos : connected) {
                this.wires.put(blockPos, newSegment);
            }

            for (NetworkConnection peerSegment : peerSegments) {
                this.networks.addEdge(this.wires.get(peerSegment.first), this.wires.get(peerSegment.second), peerSegment);
            }
        }
    }

    public long accept(BlockPos sourcePos, ServerLevel level, long amount, TransactionContext context) {
        WireSegment source = this.wires.get(sourcePos);
        if (source == null) {
            BlockState state = level.getBlockState(sourcePos);
            if (!(state.getBlock() instanceof WireBlock)) return 0;
            source = createFullNetwork(level, sourcePos, state);
        }

        if (this.locked.contains(source)) return 0;
        level.getProfiler().push("wire_network");

        level.getProfiler().push("traversal");
        Object2LongOpenHashMap<WireSegment> network = traverseNetwork(level.getGameTime(), source);
        level.getProfiler().pop();

        try {
            this.locked.addAll(network.keySet());
            record SegmentRequest(WireSegment key, double internalFulfillment, List<EnergyRequest> requests) {}
            List<SegmentRequest> netRequests = new ArrayList<>(network.size());
            long requested = 0;

            level.getProfiler().push("test");
            try (Transaction transaction = Transaction.openNested(context)) {
                for (ObjectIterator<Object2LongMap.Entry<WireSegment>> iterator = network.object2LongEntrySet().fastIterator(); iterator.hasNext(); ) {
                    List<EnergyRequest> requests = new ArrayList<>();
                    Object2LongMap.Entry<WireSegment> entry = iterator.next();
                    WireSegment segment = entry.getKey();
                    if (segment.usage >= network.getLong(segment)) continue;
                    long request = segment.tryAccept(requests, level, amount, transaction);
                    long possible = Math.min(request, network.getLong(segment) - segment.usage);
                    if (request > 0 && possible > 0) {
                        netRequests.add(new SegmentRequest(segment, (double) possible / (double) request, requests));
                        requested += possible;
                    }
                }
                transaction.abort();
            }
            level.getProfiler().pop();

            if (requested <= 0) return 0;

            double globalFulfillment = amount > requested ? 1 : (double) amount / requested;
            long distributed = 0;
            level.getProfiler().push("distribution");
            System.out.println(sourcePos.toShortString());
            try (Transaction transaction = Transaction.openNested(context)) {
                for (SegmentRequest segment : netRequests) {
                    for (EnergyRequest request : segment.requests) {
                        long inserted = request.storage.insert((long) (request.amount * globalFulfillment * segment.internalFulfillment), transaction);
                        distributed += inserted;
                        System.out.println("dist: " + inserted);
                        if ((segment.key.usage += inserted) >= segment.key.capacity) break;
                    }
                }

                if (distributed <= amount) {
                    transaction.commit();
                } else {
                    MachineLib.LOGGER.error("Distributed too much energy. Aborting!");
                    return 0;
                }
            } finally {
                level.getProfiler().pop();
            }

            return amount - distributed;
        } finally {
            this.locked.removeAll(network.keySet());
            level.getProfiler().pop();
        }
    }

    private Object2LongOpenHashMap<WireSegment> traverseNetwork(long tick, WireSegment source) {
        ArrayList<WireSegment> queue = new ArrayList<>();
        queue.add(source);
        Object2LongOpenHashMap<WireSegment> map = new Object2LongOpenHashMap<>();
        map.defaultReturnValue(Integer.MIN_VALUE);

        map.put(source, source.capacity);
        while (!queue.isEmpty()) {
            WireSegment segment = queue.removeLast();
            if (segment.tick != tick) {
                System.out.println("---- " + segment.usage + " / " + segment.capacity);
                segment.usage = 0;
                segment.tick = tick;
            }

            long cap = map.getLong(segment);
            for (WireSegment successor : this.networks.successors(segment)) {
                long localCap = Math.min(cap, successor.capacity);
                if (map.getLong(successor) < localCap) { // DNE or worse than the new one
                    map.put(successor, localCap);
                    queue.add(successor); // we can requeue if we get better flow here
                }
            }
        }
        return map;
    }

    private static void traverseWires(Set<BlockPos> visited, Map<BlockPos, EnumSet<Direction>> visitedEndpoints, Set<NetworkConnection> peerSegments, ServerLevel level, BlockPos start, @Nullable BlockPos until, long cap) {
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
                if (!visited.contains(mutable)) {
                    BlockState adjState = level.getBlockState(mutable);
                    if (isPhysicallyConnected(state, direction, adjState)) {
                        if (isMergable(start.getX() >> 4, start.getZ() >> 4, mutable, cap, ((WireBlock) adjState.getBlock()).capacity)) {
                            BlockPos immutable = mutable.immutable();
                            visited.add(immutable);
                            if (mutable.equals(until)) return;
                            queue.addLast(immutable);
                        } else {
                            if (mutable.equals(until)) return;
                            peerSegments.add(new NetworkConnection(target, mutable.immutable()));
                        }
                    } else if (isEndpointConnected(state, direction, adjState)) {
                        visitedEndpoints.computeIfAbsent(mutable.immutable(), k -> EnumSet.noneOf(Direction.class)).add(direction.getOpposite());
                    }
                }
            }
        }
    }

    // direction to updated neighbor
    public void updateWire(ServerLevel level, BlockPos pos, BlockState state, Direction direction, BlockPos adjPos, BlockState adjState) {
        WireSegment segment = this.getSegment(pos);
        if (segment == null) {
            MachineLib.LOGGER.warn("Missing network at [{}], creating one.", pos.toShortString());
            segment = createFullNetwork(level, pos, state);
        }

        WireSegment adjSegment = this.getSegment(adjPos);
        if (adjSegment != null) { // check for wire
            if (isMergable(pos.getX() >> 4, pos.getZ() >> 4, adjPos, segment.capacity, adjSegment.capacity)) { // internal - mutate network segment
                if (isPhysicallyConnected(state, direction, adjState)) {
                    if (adjSegment == segment) return; // connection already exists - no change.
                    mergeSegments(adjSegment, segment); // newly created connection
                } else {
                    if (!(adjState.getBlock() instanceof WireBlock)) {
                        this.removeWire(level, adjPos); // wire removed - can fast track
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
            if (isEndpointConnected(state, direction, adjState)) {
                segment.addEndpoint(level, adjPos, direction.getOpposite());
            } else {
                segment.removeEndpoint(adjPos, direction.getOpposite());
            }
        }
    }

    private WireSegment createFullNetwork(ServerLevel level, BlockPos pos, BlockState state) {
        if (state == null) state = level.getBlockState(pos);

        Set<BlockPos> connected = new HashSet<>();
        Set<NetworkConnection> peerSegments = new HashSet<>();
        Map<BlockPos, EnumSet<Direction>> connectedRefs = new HashMap<>();
        traverseWires(connected, connectedRefs, peerSegments, level, pos, null, ((WireBlock) state.getBlock()).capacity);
        WireSegment segment = new WireSegment(pos.getX() >> 4, pos.getZ() >> 4, level, ((WireBlock) state.getBlock()).capacity, connectedRefs);

        this.networks.addNode(segment);
        for (BlockPos blockPos : connected) {
            WireSegment segment1 = this.wires.put(blockPos, segment);
            if (segment1 != null) this.networks.removeNode(segment1);
        }

        for (NetworkConnection peerSegment : peerSegments) {
            if (this.wires.get(peerSegment.first) == null) {
                createFullNetwork(level, peerSegment.first, null);
                if (!networks.edges().contains(peerSegment)) throw new AssertionError();
            } else if (this.wires.get(peerSegment.second) == null) {
                createFullNetwork(level, peerSegment.second, null);
                if (!networks.edges().contains(peerSegment)) throw new AssertionError();
            } else {
                this.networks.addEdge(this.wires.get(peerSegment.first), this.wires.get(peerSegment.second), peerSegment);
            }
        }
        return segment;
    }

    public void newWire(ServerLevel level, BlockPos pos, BlockState state) {
        // network and segment to apply to the wire
        WireSegment segment = new WireSegment(pos.getX() >> 4, pos.getZ() >> 4, ((WireBlock) state.getBlock()).capacity);
        this.networks.addNode(segment);
        this.wires.put(pos, segment);

        // update endpoint connections. we don't need to update wires, as they will notify properly
        for (Direction direction : Direction.values()) {
            if (state.getValue(WireBlock.CONNECTIONS[direction.get3DDataValue()]) && !(level.getBlockState(pos.relative(direction)).getBlock() instanceof WireBlock)) {
                segment.addEndpoint(level, pos.relative(direction), direction.getOpposite());
            }
        }
    }

    private void mergeSegments(WireSegment adj, WireSegment segment) {
        if (adj.capacity != segment.capacity) throw new AssertionError();
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

    private void splitSegment(BlockPos disconnected, Direction disconnectDir, ServerLevel level, WireSegment segment) {
        Set<BlockPos> connected = new HashSet<>();
        Map<BlockPos, EnumSet<Direction>> connectedRefs = new HashMap<>();
        Set<NetworkConnection> peerSegments = new HashSet<>();
        BlockPos disconnectPeer = disconnected.relative(disconnectDir);

        traverseWires(connected, connectedRefs, peerSegments, level, disconnected, disconnectPeer, segment.capacity);
        if (connected.contains(disconnectPeer)) return; // no change.
        this.networks.removeNode(segment);

        WireSegment segment1 = new WireSegment(segment.x, segment.z, level, segment.capacity, connectedRefs);
        this.networks.addNode(segment1);
        for (NetworkConnection peerSegment : peerSegments) {
            this.networks.addEdge(this.wires.get(peerSegment.first), this.wires.get(peerSegment.second), peerSegment);
        }

        connected.clear();
        connectedRefs.clear();
        peerSegments.clear();

        traverseWires(connected, connectedRefs, peerSegments, level, disconnectPeer, null, segment.capacity);
        WireSegment segment2 = new WireSegment(segment.x, segment.z, level, segment.capacity, connectedRefs);
        this.networks.addNode(segment2);
        for (NetworkConnection peerSegment : peerSegments) {
            this.networks.addEdge(this.wires.get(peerSegment.first), this.wires.get(peerSegment.second), peerSegment);
        }
    }

    protected static boolean isPhysicallyConnected(BlockState state, Direction direction, BlockState adjState) {
        return state.getValue(WireBlock.CONNECTIONS[direction.get3DDataValue()])
                && adjState.getOptionalValue(WireBlock.CONNECTIONS[direction.getOpposite().get3DDataValue()]).orElse(false);
    }

    protected static boolean isEndpointConnected(BlockState state, Direction direction, BlockState adjState) {
        return state.getValue(WireBlock.CONNECTIONS[direction.get3DDataValue()]) && !(adjState.getBlock() instanceof WireBlock);
    }

    protected static boolean isMergable(int x, int z, BlockPos pos, long a, long b) {
        return sameChunk(x, z, pos) && a == b;
    }

    protected static boolean sameChunk(int x, int z, BlockPos pos) {
        return pos.getX() >> 4 == x && pos.getZ() >> 4 == z;
    }

    public void rebuild(CommandContext<CommandSourceStack> ctx) {
        Map<BlockPos, WireSegment> wires = new HashMap<>(this.wires);
        this.wires.clear();
        Set<WireSegment> wireSegments = new HashSet<>(this.networks.nodes());
        Set<NetworkConnection> edges = new HashSet<>(this.networks.edges());
        wireSegments.forEach(this.networks::removeNode);
        ServerLevel level = ctx.getSource().getLevel();

        int nonWires = 0;

        for (BlockPos blockPos : wires.keySet()) {
            WireSegment segment = this.wires.get(blockPos);
            if (segment == null) {
                BlockState blockState = level.getBlockState(blockPos);
                if (blockState.getBlock() instanceof WireBlock) {
                    createFullNetwork(level, blockPos, blockState);
                } else {
                    nonWires++;
                }
            }
        }

        int nodeDiff = this.networks.nodes().size() - wireSegments.size();
        int edgeDiff = this.networks.edges().size() - edges.size();

        HashSet<BlockPos> wireDiff = new HashSet<>(this.wires.keySet());
        wireDiff.removeAll(wires.keySet());
        ctx.getSource().sendSystemMessage(Component.literal("rebuilt networks. N: " + nodeDiff + ", E: " + edgeDiff + ", W+: " + wireDiff.size() + ", W-: " + nonWires));
    }

    public void purge() {
        this.wires.clear();
        new ArrayList<>(this.networks.nodes()).forEach(this.networks::removeNode);
    }

    public void printInfo(CommandContext<CommandSourceStack> ctx) {
        Set<WireSegment> orphaned = new HashSet<>(this.networks.nodes());
        orphaned.removeAll(this.wires.values());
        ctx.getSource().sendSystemMessage(Component.literal("wires: " + this.wires.size() + ", segments: " + this.networks.nodes().size() + ", orphaned: " + orphaned.size()));
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

    private class WireEnergyStorage implements EnergyStorage {
        private final BlockPos pos;
        private final ServerLevel level;

        public WireEnergyStorage(BlockPos pos, ServerLevel level) {
            this.pos = pos;
            this.level = level;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            return WireNetworkManager.this.accept(pos, level, maxAmount, transaction);
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
            WireSegment segment = WireNetworkManager.this.getSegment(pos);
            return segment != null ? segment.capacity : 0;
        }
    }
}
