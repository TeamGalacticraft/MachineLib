package dev.galacticraft.machinelib.api.multiblock;

import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.menu.ConfiguredMenu;
import dev.galacticraft.machinelib.api.menu.MenuData;
import dev.galacticraft.machinelib.api.menu.SynchronizedMenu;
import dev.galacticraft.machinelib.api.menu.Tank;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockIOConfigComponent;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockRedstoneComponent;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockSecurityComponent;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockStateComponent;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.impl.menu.MenuDataClient;
import dev.galacticraft.machinelib.impl.menu.MenuDataImpl;
import dev.galacticraft.machinelib.impl.multiblock.FormedMultiblockMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base configured menu for formed multiblock machines.
 *
 * <p>This is the multiblock equivalent of {@link ConfiguredMenu}. Unlike
 * {@link SynchronizedMenu}, this menu is not backed by a block entity. Instead,
 * it binds to a formed multiblock instance and reads its standard MachineLib
 * components.</p>
 */
public abstract class MultiblockConfiguredMenu extends AbstractContainerMenu {

    public final Inventory playerInventory;
    public final Player player;
    public final UUID instanceId;
    public final ResourceLocation definitionId;
    public final BlockPos origin;
    public final BlockPos clickedPos;
    public final MultiblockOrientation orientation;

    public final IOConfig configuration;
    public final SecuritySettings security;
    public final MachineState state;

    public final List<Tank> tanks = new ArrayList<>();

    public RedstoneMode redstoneMode;

    protected int internalSlots;

    private final MenuData data;
    private final FormedMultiblockMachine machine;

    /**
     * Creates a server-side configured multiblock menu.
     *
     * @param type menu type
     * @param syncId menu sync id
     * @param player player opening the menu
     * @param machine formed multiblock machine
     * @param clickedPos clicked part position
     */
    protected MultiblockConfiguredMenu(
            final MenuType<? extends MultiblockConfiguredMenu> type,
            final int syncId,
            final ServerPlayer player,
            final FormedMultiblockMachine machine,
            final BlockPos clickedPos
    ) {
        super(type, syncId);

        this.player = player;
        this.playerInventory = player.getInventory();
        this.machine = machine;

        this.instanceId = machine.instanceId();
        this.definitionId = machine.definition().id();
        this.origin = machine.origin();
        this.clickedPos = clickedPos.immutable();
        this.orientation = machine.orientation();

        final MultiblockIOConfigComponent io =
                machine.component(MultiblockIOConfigComponent.class);

        final MultiblockSecurityComponent security =
                machine.component(MultiblockSecurityComponent.class);

        final MultiblockStateComponent state =
                machine.component(MultiblockStateComponent.class);

        final MultiblockRedstoneComponent redstone =
                machine.component(MultiblockRedstoneComponent.class);

        if (io == null) {
            throw new IllegalStateException("Multiblock menu requires MultiblockIOConfigComponent");
        }

        if (security == null) {
            throw new IllegalStateException("Multiblock menu requires MultiblockSecurityComponent");
        }

        if (state == null) {
            throw new IllegalStateException("Multiblock menu requires MultiblockStateComponent");
        }

        if (redstone == null) {
            throw new IllegalStateException("Multiblock menu requires MultiblockRedstoneComponent");
        }

        this.configuration = io.configuration();
        this.security = security.security();
        this.state = state.state();
        this.redstoneMode = redstone.mode();

        this.data = new MenuDataImpl(player, syncId);
        this.registerData(this.data);
    }

    /**
     * Creates a client-side configured multiblock menu.
     *
     * <p>The client does not own the real formed machine. It creates local
     * synchronized copies of the standard configured components and receives
     * their values through menu data sync.</p>
     *
     * @param type menu type
     * @param syncId menu sync id
     * @param inventory player inventory
     * @param openingData multiblock opening data
     */
    protected MultiblockConfiguredMenu(
            final MenuType<? extends MultiblockConfiguredMenu> type,
            final int syncId,
            final Inventory inventory,
            final MultiblockMenuOpeningData openingData
    ) {
        super(type, syncId);

        this.player = inventory.player;
        this.playerInventory = inventory;
        this.machine = null;

        this.instanceId = openingData.instanceId();
        this.definitionId = openingData.definitionId();
        this.origin = openingData.origin();
        this.clickedPos = openingData.clickedPos();
        this.orientation = openingData.orientation();

        this.configuration = new IOConfig(this.createClientFaces());
        this.security = new ClientSecuritySettings();
        this.state = new MachineState();
        this.redstoneMode = RedstoneMode.IGNORE;

        this.data = new MenuDataClient(syncId);
        this.registerData(this.data);
    }

    /**
     * Registers synchronized menu data.
     *
     * @param data menu data
     */
    public void registerData(final MenuData data) {
        data.register(this.configuration);
        data.register(this.security);
        data.register(this.state);

        data.registerEnum(
                RedstoneMode.values(),
                () -> this.redstoneMode,
                mode -> this.redstoneMode = mode
        );
    }

    /**
     * Gets the formed machine backing this menu.
     *
     * <p>This only exists on the logical server. It is {@code null} on the
     * client.</p>
     *
     * @return formed machine, or {@code null} on client
     */
    public FormedMultiblockMachine machine() {
        return this.machine;
    }

    /**
     * Gets this menu's synchronization data object.
     *
     * @return menu data
     */
    public MenuData getData() {
        return this.data;
    }

    /**
     * Adds player inventory slots in the normal MachineLib layout.
     *
     * @param inventory player inventory
     * @param x start x
     * @param y start y
     */
    protected void addPlayerInventorySlots(
            final Inventory inventory,
            final int x,
            final int y
    ) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(
                        inventory,
                        column + row * 9 + 9,
                        x + column * 18,
                        y + row * 18
                ));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(
                    inventory,
                    column,
                    x + column * 18,
                    y + 58
            ));
        }
    }

    /**
     * Checks whether a face is locked from configuration changes.
     *
     * @param face logical face
     * @return {@code true} if the face is locked
     */
    public boolean isFaceLocked(final BlockFace face) {
        return false;
    }

    /**
     * Cycles the I/O configuration of a logical multiblock face.
     *
     * @param face face to cycle
     * @param reverse whether to cycle backward
     * @param reset whether to reset to none
     */
    public void cycleFaceConfig(
            final BlockFace face,
            final boolean reverse,
            final boolean reset
    ) {
        final IOFace option = this.configuration.get(face);

        final short bits = this.calculateIoBitmask();

        if (bits != 0b1_000_000_000_000 && !reset && !this.isFaceLocked(face)) {
            ResourceType type = option.getType();
            ResourceFlow flow = option.getFlow();

            final int index = switch (type) {
                case NONE -> 12;
                case ENERGY, ITEM, FLUID, ANY -> (type.ordinal() - 1) * 3 + flow.ordinal();
            };

            int i = index + (reverse ? -1 : 1);

            while (i != index) {
                if (i == -1) {
                    i = 12;
                } else if (i == 13) {
                    i = 0;
                }

                if ((bits >>> i & 0b1) != 0) {
                    break;
                }

                i += reverse ? -1 : 1;
            }

            if (i == 12) {
                option.setOption(
                        ResourceType.NONE,
                        ResourceFlow.BOTH
                );
            } else {
                final byte flowIndex = (byte) (i % 3);
                final byte typeIndex = (byte) ((i - flowIndex) / 3 + 1);

                option.setOption(
                        ResourceType.getFromOrdinal(typeIndex),
                        ResourceFlow.getFromOrdinal(flowIndex)
                );
            }
        } else {
            option.setOption(
                    ResourceType.NONE,
                    ResourceFlow.BOTH
            );
        }

        if (this.machine != null) {
            this.machine.setComponentsChanged();
        }
    }

    /**
     * Adds a fluid tank display to this menu.
     *
     * @param tank tank display
     */
    public void addTank(final Tank tank) {
        tank.setId(this.tanks.size());
        this.tanks.add(tank);
    }

    /**
     * Calculates the valid I/O bitmask for this menu.
     *
     * @return I/O option bitmask
     */
    protected abstract short calculateIoBitmask();

    /**
     * Checks whether the menu is still valid.
     *
     * @param player player
     * @return {@code true} if the player can keep this menu open
     */
    @Override
    public boolean stillValid(final Player player) {
        if (!this.security.hasAccess(player)) {
            return false;
        }

        if (this.machine == null) {
            return true;
        }

        if (!this.machine.isFormed()) {
            return false;
        }

        if (!this.machine.contains(this.clickedPos)) {
            return false;
        }

        return player.distanceToSqr(
                this.clickedPos.getX() + 0.5D,
                this.clickedPos.getY() + 0.5D,
                this.clickedPos.getZ() + 0.5D
        ) <= 64.0D;
    }

    /**
     * Sends all synchronized data to the remote client.
     */
    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        this.data.synchronizeFull();
    }

    /**
     * Broadcasts changed synchronized data.
     */
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        this.data.synchronize();
    }

    private IOFace[] createClientFaces() {
        final IOFace[] faces = new IOFace[BlockFace.values().length];

        for (int i = 0; i < faces.length; i++) {
            faces[i] = new IOFace(
                    ResourceType.NONE,
                    ResourceFlow.BOTH
            );
        }

        return faces;
    }

    /**
     * Client-side security settings placeholder.
     *
     * <p>The real values are synchronized through menu data after opening.</p>
     */
    private static final class ClientSecuritySettings extends SecuritySettings {

    }

}