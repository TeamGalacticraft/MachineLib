package dev.galacticraft.machinelib.api.multiblock.components;

import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.MachineStatus;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import net.minecraft.nbt.CompoundTag;

/**
 * Persistent machine state component for a formed multiblock machine.
 *
 * <p>This stores the basic runtime state needed by MachineLib-style machines:
 * powered state, active state, and current status. The status is runtime-only by
 * default because many statuses are derived from current ticking conditions.</p>
 */
public final class MultiblockStateComponent implements MultiblockComponent {

    private static final String POWERED = "Powered";
    private static final String ACTIVE = "Active";

    private final PersistentMachineState state =
            new PersistentMachineState();

    private boolean active;
    private MultiblockComponentContext context;

    /**
     * Gets the MachineLib-compatible machine state object.
     *
     * @return machine state
     */
    public MachineState state() {
        return this.state;
    }

    /**
     * Gets the current machine status.
     *
     * @return current status, or {@code null}
     */
    public MachineStatus status() {
        return this.state.getStatus();
    }

    /**
     * Sets the current machine status.
     *
     * @param status new status
     */
    public void setStatus(final MachineStatus status) {
        this.state.setStatus(status);
    }

    /**
     * Checks whether this multiblock is currently powered.
     *
     * @return {@code true} if powered
     */
    public boolean isPowered() {
        return this.state.isPowered();
    }

    /**
     * Sets whether this multiblock is currently powered.
     *
     * @param powered new powered state
     */
    public void setPowered(final boolean powered) {
        this.state.setPowered(powered);
    }

    /**
     * Checks whether this multiblock is currently active.
     *
     * @return {@code true} if active
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Sets whether this multiblock is currently active.
     *
     * @param active new active state
     */
    public void setActive(final boolean active) {
        if (this.active == active) {
            return;
        }

        this.active = active;
        this.setChanged();
    }

    /**
     * Checks whether this machine is disabled by redstone.
     *
     * @param redstone redstone component
     * @return {@code true} if disabled
     */
    public boolean isDisabled(final MultiblockRedstoneComponent redstone) {
        return redstone != null && redstone.isDisabled(this.isPowered());
    }

    /**
     * Loads persisted state.
     *
     * @param context component context
     * @param tag saved component tag
     */
    @Override
    public void load(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        this.context = context;

        this.state.setPowered(tag.getBoolean(POWERED));
        this.active = tag.getBoolean(ACTIVE);
    }

    /**
     * Saves persisted state.
     *
     * @param context component context
     * @param tag component tag to write into
     */
    @Override
    public void save(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        tag.putBoolean(
                POWERED,
                this.state.isPowered()
        );

        tag.putBoolean(
                ACTIVE,
                this.active
        );
    }

    /**
     * Stores the active component context.
     *
     * @param context component context
     */
    @Override
    public void onFormed(final MultiblockComponentContext context) {
        this.context = context;
    }

    /**
     * Machine state implementation that marks the multiblock as changed when
     * persistent state changes.
     */
    private final class PersistentMachineState extends MachineState {

        /**
         * Updates the runtime status.
         *
         * <p>Status is intentionally not persisted here because it is normally
         * derived from current tick logic.</p>
         *
         * @param status new status
         */
        @Override
        public void setStatus(final MachineStatus status) {
            if (this.status != status) {
                this.status = status;
            }
        }

        /**
         * Updates the powered state.
         *
         * @param powered new powered state
         */
        @Override
        public void setPowered(final boolean powered) {
            if (this.powered != powered) {
                this.powered = powered;
                MultiblockStateComponent.this.setChanged();
            }
        }

    }

    private void setChanged() {
        if (this.context != null) {
            this.context.setChanged();
        }
    }

}