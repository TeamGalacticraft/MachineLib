package dev.galacticraft.machinelib.impl.multiblock;

import dev.galacticraft.machinelib.api.multiblock.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;
import java.util.UUID;

/**
 * Default component context backed by a formed multiblock machine.
 */
public final class SimpleMultiblockComponentContext implements MultiblockComponentContext {

    private final FormedMultiblockMachine machine;

    /**
     * Creates a component context.
     *
     * @param machine formed machine
     */
    public SimpleMultiblockComponentContext(final FormedMultiblockMachine machine) {
        this.machine = machine;
    }

    @Override
    public ServerLevel level() {
        return this.machine.level();
    }

    @Override
    public BlockPos origin() {
        return this.machine.origin();
    }

    @Override
    public UUID instanceId() {
        return this.machine.instanceId();
    }

    @Override
    public MultiblockOrientation orientation() {
        return this.machine.orientation();
    }

    @Override
    public MultiblockDefinition definition() {
        return this.machine.definition();
    }

    @Override
    public Set<BlockPos> partPositions() {
        return this.machine.partPositions();
    }

    @Override
    public <T extends MultiblockComponent> T component(final Class<T> type) {
        return this.machine.component(type);
    }

}