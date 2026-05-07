package dev.galacticraft.machinelib.api.multiblock;

import dev.galacticraft.machinelib.api.menu.MachineMenu;
import dev.galacticraft.machinelib.api.menu.MenuData;
import dev.galacticraft.machinelib.api.menu.Tank;
import dev.galacticraft.machinelib.api.multiblock.components.MultiblockStorageComponent;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.storage.MachineFluidStorage;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.StorageSpec;
import dev.galacticraft.machinelib.api.storage.slot.FluidResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.ResourceSlot;
import dev.galacticraft.machinelib.api.transfer.TransferType;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import dev.galacticraft.machinelib.impl.compat.vanilla.StorageSlot;
import dev.galacticraft.machinelib.impl.multiblock.FormedMultiblockMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Base MachineLib-style menu for formed multiblock machines.
 *
 * <p>This extends {@link MultiblockConfiguredMenu} with item, fluid, and energy
 * storage from {@link MultiblockStorageComponent}. It is the multiblock
 * equivalent of {@link MachineMenu}.</p>
 */
public class MultiblockMachineMenu extends MultiblockConfiguredMenu {

    public final MachineItemStorage itemStorage;
    public final MachineFluidStorage fluidStorage;
    public final MachineEnergyStorage energyStorage;

    /**
     * Creates a server-side multiblock machine menu.
     *
     * @param type menu type
     * @param syncId menu sync id
     * @param player player opening the menu
     * @param machine formed machine
     * @param clickedPos clicked part position
     */
    public MultiblockMachineMenu(
            final MenuType<? extends MultiblockMachineMenu> type,
            final int syncId,
            final ServerPlayer player,
            final FormedMultiblockMachine machine,
            final BlockPos clickedPos
    ) {
        super(
                type,
                syncId,
                player,
                machine,
                clickedPos
        );

        final MultiblockStorageComponent storage =
                machine.component(MultiblockStorageComponent.class);

        if (storage == null) {
            throw new IllegalStateException("Multiblock machine menu requires MultiblockStorageComponent");
        }

        this.itemStorage = storage.itemStorage();
        this.fluidStorage = storage.fluidStorage();
        this.energyStorage = storage.energyStorage();

        this.generateSlots(this.itemStorage);
        this.generateTanks(this.fluidStorage);
        this.addPlayerInventorySlots(
                player.getInventory(),
                0,
                0
        );

        this.initializeDataSync();
    }

    /**
     * Creates a client-side multiblock machine menu and optionally adds player
     * inventory slots.
     *
     * @param type menu type
     * @param syncId menu sync id
     * @param inventory player inventory
     * @param openingData multiblock opening data
     * @param spec client storage specification
     * @param invX player inventory x
     * @param invY player inventory y
     */
    public MultiblockMachineMenu(
            final MenuType<? extends MultiblockMachineMenu> type,
            final int syncId,
            final Inventory inventory,
            final MultiblockMenuOpeningData openingData,
            final StorageSpec spec,
            final int invX,
            final int invY
    ) {
        super(
                type,
                syncId,
                inventory,
                openingData
        );

        this.itemStorage = spec.createItemStorage();
        this.fluidStorage = spec.createFluidStorage();
        this.energyStorage = spec.createEnergyStorage();

        this.generateSlots(this.itemStorage);
        this.generateTanks(this.fluidStorage);

        if (invX >= 0 && invY >= 0) {
            this.addPlayerInventorySlots(
                    inventory,
                    invX,
                    invY
            );
        }

        this.initializeDataSync();
    }

    /**
     * Creates a client-side multiblock machine menu.
     *
     * @param type menu type
     * @param syncId menu sync id
     * @param inventory player inventory
     * @param openingData multiblock opening data
     * @param spec client storage specification
     */
    public MultiblockMachineMenu(
            final MenuType<? extends MultiblockMachineMenu> type,
            final int syncId,
            final Inventory inventory,
            final MultiblockMenuOpeningData openingData,
            final StorageSpec spec
    ) {
        this(
                type,
                syncId,
                inventory,
                openingData,
                spec,
                -1,
                -1
        );
    }

    /**
     * Registers synchronized storage data.
     *
     * @param data menu data
     */
    @Override
    public void registerData(final MenuData data) {
        super.registerData(data);

        data.register(this.itemStorage);
        data.register(this.fluidStorage);
        data.register(this.energyStorage);
    }

    /**
     * Generates visible item slots for the multiblock item storage.
     *
     * @param itemStorage item storage
     */
    protected void generateSlots(final MachineItemStorage itemStorage) {
        final ItemResourceSlot[] slots = itemStorage.getSlots();

        for (int i = 0; i < slots.length; i++) {
            final ItemResourceSlot slot = slots[i];

            if (slot.getDisplay() != null) {
                this.internalSlots++;

                this.addSlot(new StorageSlot(
                        itemStorage,
                        slot,
                        slot.getDisplay(),
                        i,
                        this.player
                ));
            }
        }
    }

    /**
     * Generates visible fluid tank displays for the multiblock fluid storage.
     *
     * @param fluidStorage fluid storage
     */
    protected void generateTanks(final MachineFluidStorage fluidStorage) {
        final FluidResourceSlot[] tanks = fluidStorage.getSlots();

        for (int i = 0; i < tanks.length; i++) {
            final FluidResourceSlot slot = tanks[i];

            if (slot.getDisplay() != null) {
                this.addTank(Tank.create(
                        slot,
                        slot.getDisplay(),
                        slot.transferMode(),
                        i
                ));
            }
        }
    }

    /**
     * Handles shift-click movement between multiblock slots and player inventory.
     *
     * @param player player moving items
     * @param index clicked slot index
     * @return moved stack
     */
    @Override
    public ItemStack quickMoveStack(
            final Player player,
            final int index
    ) {
        ItemStack stack = ItemStack.EMPTY;
        final Slot slotFrom = this.slots.get(index);

        if (slotFrom.hasItem()) {
            final ItemStack stackFrom = slotFrom.getItem();
            stack = stackFrom.copy();

            if (index < this.internalSlots && slotFrom instanceof StorageSlot storageSlot) {
                final ItemResourceSlot itemSlot = storageSlot.getWrapped();

                if (!this.quickMoveIntoPlayerInventory(itemSlot)) {
                    return ItemStack.EMPTY;
                }

                stack = storageSlot.getItem();

                if (itemSlot.transferMode() == TransferType.OUTPUT) {
                    storageSlot.onQuickCraft(
                            stack,
                            stackFrom
                    );
                }

                this.markStorageChanged();
                return stack;
            }

            final long originalCount = stack.getCount();
            long available = originalCount;

            for (int i = 0; i < this.internalSlots; i++) {
                final ItemResourceSlot slot = ((StorageSlot) this.slots.get(i)).getWrapped();

                if (slot.transferMode().playerInsertion()
                        && slot.contains(stack.getItem(), stack.getComponentsPatch())) {
                    available -= slot.insert(
                            stack.getItem(),
                            stack.getComponentsPatch(),
                            available
                    );

                    if (available == 0) {
                        slotFrom.setByPlayer(ItemStack.EMPTY);
                        this.markStorageChanged();
                        return ItemStack.EMPTY;
                    }
                }
            }

            for (int i = 0; i < this.internalSlots; i++) {
                final StorageSlot slot = (StorageSlot) this.slots.get(i);

                if (slot.mayPlace(stack)) {
                    available -= slot.getWrapped().insert(
                            stack.getItem(),
                            stack.getComponentsPatch(),
                            available
                    );

                    if (available == 0) {
                        slotFrom.setByPlayer(ItemStack.EMPTY);
                        this.markStorageChanged();
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (available == originalCount) {
                final int size = this.slots.size();

                if (index >= this.internalSlots && index < size - 9) {
                    if (!this.moveItemStackTo(
                            stackFrom,
                            size - 9,
                            size,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= size - 9 && index < size) {
                    if (!this.moveItemStackTo(
                            stackFrom,
                            this.internalSlots,
                            size - 9,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                stackFrom.setCount((int) available);
                this.markStorageChanged();
            }

            if (stackFrom.isEmpty()) {
                slotFrom.set(ItemStack.EMPTY);
            } else {
                slotFrom.setChanged();
            }

            if (stackFrom.getCount() == stack.getCount() || available != originalCount) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    /**
     * Sends mutated storage-slot stacks before normal menu sync.
     */
    @Override
    public void sendAllDataToRemote() {
        for (int i = 0; i < this.internalSlots; i++) {
            if (this.slots.get(i) instanceof StorageSlot storageSlot) {
                storageSlot.handleStackMutation();
            }
        }

        super.sendAllDataToRemote();
    }

    /**
     * Calculates valid I/O options for this multiblock storage.
     *
     * @return I/O bitmask
     */
    @Override
    protected short calculateIoBitmask() {
        short bits = 1 << 12;

        if (this.energyStorage.getCapacity() > 0) {
            bits |= 0b000_000_000_111;
        }

        if (this.itemStorage.size() > 0) {
            bits |= 0b000_000_111_000;
        }

        if (this.fluidStorage.size() > 0) {
            bits |= 0b000_111_000_000;
        }

        if (this.energyStorage.getCapacity() > 0
                || this.itemStorage.size() > 0
                || this.fluidStorage.size() > 0) {
            bits |= 0b111_000_000_000;
        }

        return bits;
    }

    /**
     * Prevents moving stacks into internal slots using vanilla slot movement.
     *
     * @param stack moved stack
     * @param startIndex start index
     * @param endIndex end index
     * @param reverse reverse search
     * @return {@code true} if moved
     */
    @Override
    protected boolean moveItemStackTo(
            final ItemStack stack,
            final int startIndex,
            final int endIndex,
            final boolean reverse
    ) {
        if (startIndex < this.internalSlots) {
            throw new UnsupportedOperationException(
                    "invalid startIndex of " + startIndex + ", must be at least " + this.internalSlots
            );
        }

        return super.moveItemStackTo(
                stack,
                startIndex,
                endIndex,
                reverse
        );
    }

    private boolean quickMoveIntoPlayerInventory(final ResourceSlot<Item> fromSlot) {
        if (fromSlot.isEmpty()) {
            return false;
        }

        final boolean reverse = fromSlot.transferMode() == TransferType.OUTPUT;
        ItemStack stack = ItemStackUtil.create(fromSlot);

        final int total = stack.getCount();
        final int size = this.slots.size();
        final int start = reverse ? size - 1 : this.internalSlots;

        int i = start;

        while (i >= this.internalSlots && i < size) {
            final Slot slot = this.slots.get(i);

            if (ItemStack.isSameItemSameComponents(stack, slot.getItem())) {
                stack = slot.safeInsert(stack);

                if (stack.isEmpty()) {
                    final long extracted = fromSlot.extract(total);
                    assert extracted == total;

                    this.markStorageChanged();
                    return true;
                }
            }

            i += reverse ? -1 : 1;
        }

        i = start;

        while (i >= this.internalSlots && i < size) {
            final Slot slot = this.slots.get(i);

            stack = slot.safeInsert(stack);

            if (stack.isEmpty()) {
                final long extracted = fromSlot.extract(total);
                assert extracted == total;

                this.markStorageChanged();
                return true;
            }

            i += reverse ? -1 : 1;
        }

        final long extracted = fromSlot.extract(total - stack.getCount());
        assert extracted == total - stack.getCount();

        if (extracted > 0) {
            this.markStorageChanged();
        }

        return false;
    }

    private void markStorageChanged() {
        final FormedMultiblockMachine machine = this.machine();

        if (machine == null) {
            return;
        }

        final MultiblockStorageComponent storage =
                machine.component(MultiblockStorageComponent.class);

        if (storage != null) {
            storage.setChanged();
        }
    }

}