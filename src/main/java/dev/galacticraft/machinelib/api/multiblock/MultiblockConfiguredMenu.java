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

import dev.galacticraft.machinelib.api.machine.MachineState;
import dev.galacticraft.machinelib.api.machine.configuration.IOConfig;
import dev.galacticraft.machinelib.api.machine.configuration.IOFace;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.menu.ConfiguredMenu;
import dev.galacticraft.machinelib.api.menu.MenuData;
import dev.galacticraft.machinelib.api.menu.SynchronizedMenu;
import dev.galacticraft.machinelib.api.menu.Tank;
import dev.galacticraft.machinelib.api.multiblock.components.*;
import dev.galacticraft.machinelib.api.multiblock.port.*;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import dev.galacticraft.machinelib.api.transfer.ResourceType;
import dev.galacticraft.machinelib.api.util.BlockFace;
import dev.galacticraft.machinelib.client.impl.menu.MenuDataClient;
import dev.galacticraft.machinelib.impl.menu.MenuDataImpl;
import dev.galacticraft.machinelib.impl.multiblock.FormedMultiblockMachine;
import dev.galacticraft.machinelib.impl.multiblock.MachineLibMultiblocks;
import dev.galacticraft.machinelib.impl.network.c2s.MultiblockPortConfigUpdatePayload;
import dev.galacticraft.machinelib.impl.network.s2c.MultiblockPortConfigSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import java.util.*;

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

    private final Map<MultiblockPortFace, ConfiguredMultiblockPort> clientPorts =
            new LinkedHashMap<>();

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
    }

    /**
     * Initializes synchronized menu data after the full menu object has finished
     * assigning all subclass fields.
     *
     * <p>This must not be called from the base constructor, because Java dispatches
     * overridden methods before subclass constructors have initialized their own
     * fields.</p>
     */
    protected final void initializeDataSync() {
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
     * Gets every exposed face from the opened multiblock definition.
     *
     * @return exposed face set
     */
    public Set<MultiblockPortFace> exposedPortFaces() {
        if (this.machine != null) {
            return this.machine.definition().exposedFaces();
        }

        final MultiblockDefinition definition = MachineLibMultiblocks.getDefinition(this.definitionId);

        if (definition == null) {
            return Set.of();
        }

        return definition.exposedFaces();
    }

    /**
     * Gets the currently configured port for a face.
     *
     * @param face port face
     * @return configured port, if present
     */
    public Optional<ConfiguredMultiblockPort> configuredPortAt(final MultiblockPortFace face) {
        if (this.machine == null) {
            return Optional.ofNullable(this.clientPorts.get(face));
        }

        final MultiblockPortComponent ports = this.machine.component(MultiblockPortComponent.class);

        if (ports == null) {
            return Optional.empty();
        }

        return ports.portAt(face);
    }

    /**
     * Gets the allowed port rules for a face.
     *
     * @param face port face
     * @return matching rules
     */
    public List<MultiblockPortRule> portRulesFor(final MultiblockPortFace face) {
        final MultiblockDefinition definition = this.machine == null
                ? MachineLibMultiblocks.getDefinition(this.definitionId)
                : this.machine.definition();

        if (definition == null) {
            return List.of();
        }

        final List<MultiblockPortRule> rules = new ArrayList<>();

        for (final MultiblockPortRule rule : definition.portRules()) {
            if (rule.face().equals(face)) {
                rules.add(rule);
            }
        }

        return rules;
    }

    /**
     * Applies a full configured-port sync to this client menu.
     *
     * @param ports synced configured ports
     */
    public void applyClientPortSync(final List<ConfiguredMultiblockPort> ports) {
        this.clientPorts.clear();

        for (final ConfiguredMultiblockPort port : ports) {
            this.clientPorts.put(
                    port.face(),
                    port
            );
        }
    }

    /**
     * Sends a client-to-server request to set one multiblock port.
     *
     * @param port configured port
     */
    public void sendSetPort(final ConfiguredMultiblockPort port) {
        ClientPlayNetworking.send(MultiblockPortConfigUpdatePayload.set(
                this.instanceId,
                port
        ));
    }

    /**
     * Sends a client-to-server request to remove one configured multiblock port.
     *
     * @param face port face
     */
    public void sendRemovePort(final MultiblockPortFace face) {
        ClientPlayNetworking.send(MultiblockPortConfigUpdatePayload.remove(
                this.instanceId,
                face
        ));
    }

    /**
     * Sends the current server-side port configuration to this menu's player.
     */
    public void syncPortsToClient() {
        if (!(this.player instanceof ServerPlayer serverPlayer) || this.machine == null) {
            return;
        }

        final MultiblockPortComponent ports = this.machine.component(MultiblockPortComponent.class);

        if (ports == null) {
            return;
        }

        ServerPlayNetworking.send(
                serverPlayer,
                new MultiblockPortConfigSyncPayload(
                        this.instanceId,
                        ports.ports()
                )
        );
    }

    /**
     * Cycles the configured port on a multiblock face.
     *
     * <p>Every configurable face always has an implicit {@code none} option. This
     * means cycling forward goes from no port to the first valid option, through
     * every valid option, then back to no port. Cycling backward does the reverse.</p>
     *
     * @param face port face to cycle
     * @param reverse whether to cycle backward
     */
    public void cyclePort(
            final MultiblockPortFace face,
            final boolean reverse
    ) {
        final List<ConfiguredMultiblockPort> options = this.portOptionsFor(face);

        if (options.isEmpty()) {
            return;
        }

        final Optional<ConfiguredMultiblockPort> current = this.configuredPortAt(face);

        if (current.isEmpty()) {
            this.sendSetPort(reverse ? options.get(options.size() - 1) : options.get(0));
            return;
        }

        int index = options.indexOf(current.get());

        if (index < 0) {
            this.sendRemovePort(face);
            return;
        }

        index += reverse ? -1 : 1;

        if (index < 0 || index >= options.size()) {
            this.sendRemovePort(face);
            return;
        }

        this.sendSetPort(options.get(index));
    }

    /**
     * Removes the configured port from one multiblock face.
     *
     * @param face port face to clear
     */
    public void removePort(final MultiblockPortFace face) {
        this.sendRemovePort(face);
    }

    /**
     * Builds every valid configured-port option for a face from its matching rules.
     *
     * @param face port face
     * @return valid configured-port options
     */
    public List<ConfiguredMultiblockPort> portOptionsFor(final MultiblockPortFace face) {
        final List<ConfiguredMultiblockPort> options = new ArrayList<>();

        for (final MultiblockPortRule rule : this.portRulesFor(face)) {
            for (final MultiblockPortType type : rule.types()) {
                for (final MultiblockPortMode mode : rule.modes()) {
                    for (final MultiblockPortTarget target : rule.targets()) {
                        options.add(new ConfiguredMultiblockPort(
                                face,
                                type,
                                mode,
                                target
                        ));
                    }
                }
            }
        }

        return List.copyOf(options);
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
        this.syncPortsToClient();
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