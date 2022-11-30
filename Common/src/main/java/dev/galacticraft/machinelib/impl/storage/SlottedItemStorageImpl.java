package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.storage.slot.ItemSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.api.storage.slot.display.ItemSlotDisplay;
import dev.galacticraft.machinelib.impl.storage.slot.InternalItemSlot;
import dev.galacticraft.machinelib.impl.storage.slot.ItemSlotImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class SlottedItemStorageImpl implements InternalSlottedItemStorage {
    private final int size;
    private final InternalItemSlot[] slots;
    private final SlotGroup[] groups;
    private final ResourceFilter<Item>[] filters;
    private final ItemSlotDisplay[] displays;
    private final boolean[] playerInsertion;
    private long modCount = 0;

    public SlottedItemStorageImpl(int size, SlotGroup[] groups, int[] capacity, ResourceFilter<Item>[] filters, ItemSlotDisplay[] displays, boolean[] playerInsertion) {
        assert filters.length == size;
        this.size = size;
        this.slots = new InternalItemSlot[size];
        this.groups = groups;
        this.filters = filters;
        this.displays = displays;
        this.playerInsertion = playerInsertion;

        for (int i = 0; i < this.slots.length; i++) {
            this.slots[i] = new ItemSlotImpl(this, capacity[i]);
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public InternalItemSlot getSlot(int slot) {
        return this.slots[slot];
    }

    @Override
    public SlotGroup getGroup(int slot) {
        return this.groups[slot];
    }

    @Override
    public ResourceFilter<Item> getFilter(int slot) {
        return this.filters[slot];
    }

    @Override
    public boolean canPlayerInsert(int slot) {
        return this.playerInsertion[slot];
    }

    @Override
    public boolean canExternalInsert(int slot) {
        return this.playerInsertion[slot] && this.groups[slot].isAutomatable();
    }

    @Override
    public boolean canExternalExtract(int slot) {
        return !this.playerInsertion[slot] && this.groups[slot].isAutomatable();
    }

    @Override
    public int insert(Item item, int amount) {
        int initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.filters[i].matches(item, null)) {
                    amount -= this.insert(i, item, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public int insert(Item item, CompoundTag tag, int amount) {
        int initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.filters[i].matches(item, tag)) {
                    amount -= this.insert(i, item, tag, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public int insertMerge(Item item, int amount) {
        int initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.getItem(i) == item) {
                    amount -= this.insert(i, item, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return this.insert(item, amount);
    }

    @Override
    public int insertMerge(Item item, CompoundTag tag, int amount) {
        int initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                ItemStack stack = this.getStack(i);
                if (stack.getItem() == item && Objects.equals(stack.getTag(), tag)) {
                    amount -= this.insert(i, item, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return this.insert(item, tag, amount);
    }

    @Override
    public int simulateInsert(Item item, int amount) {
        int initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.filters[i].matches(item, null)) {
                    amount -= this.simulateInsert(i, item, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public int simulateInsert(Item item, CompoundTag tag, int amount) {
        int initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.filters[i].matches(item, tag)) {
                    amount -= this.simulateInsert(i, item, tag, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public int simulateInsertMerge(Item item, int amount) {
        int initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.getItem(i) == item) {
                    amount -= this.simulateInsert(i, item, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                if (this.getItem(i) != item && this.filters[i].matches(item, null)) {
                    amount -= this.simulateInsert(i, item, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public int simulateInsertMerge(Item item, CompoundTag tag, int amount) {
        int initial = 0;
        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                ItemStack stack = this.getStack(i);
                if (stack.getItem() == item && Objects.equals(stack.getTag(), tag)) {
                    amount -= this.simulateInsert(i, item, amount);
                    if (amount == 0) return initial;
                }
            }
        }

        for (int i = 0; i < this.size; i++) {
            if (!this.playerInsertion[i]) {
                ItemStack stack = this.getStack(i);
                if ((stack.getItem() != item || !Objects.equals(stack.getTag(), tag)) && this.filters[i].matches(item, tag)) {
                    amount -= this.simulateInsert(i, item, amount);
                    if (amount == 0) return initial;
                }
            }
        }
        return initial - amount;
    }

    @Override
    public boolean extract(Item item) {
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                if (this.extract(i, item)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean extractExact(Item item, int amount) {
        int initial = amount;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.simulateExtract(i, item, amount);
                if (amount == 0) break;
            }
        }
        if (amount != 0) return false;

        amount = initial;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.extract(i, item, amount);
                if (amount == 0) return true;
            }
        }
        return false;
    }

    @Override
    public int extract(Item item, int amount) {
        int initial = amount;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.extract(i, item, amount);
                if (amount == 0) return initial;
            }
        }
        return initial - amount;
    }

    @Override
    public int extract(Item item, CompoundTag tag, int amount) {
        int initial = amount;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.extract(i, item, tag, amount);
                if (amount == 0) return initial;
            }
        }
        return initial - amount;
    }

    @Override
    public boolean simulateExtract(Item item) {
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                if (this.simulateExtract(i, item)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean simulateExtractExact(Item item, int amount) {
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.simulateExtract(i, item, amount);
                if (amount == 0) return true;
            }
        }
        return false;
    }

    @Override
    public int simulateExtract(Item item, int amount) {
        int initial = amount;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.simulateExtract(i, item, amount);
                if (amount == 0) return initial;
            }
        }
        return initial - amount;
    }

    @Override
    public int simulateExtract(Item item, CompoundTag tag, int amount) {
        int initial = amount;
        for (int i = 0; i < this.size; i++) {
            if (this.playerInsertion[i]) {
                amount -= this.simulateExtract(i, item, tag, amount);
                if (amount == 0) return initial;
            }
        }
        return initial - amount;
    }

    @Override
    public boolean isEmpty() {
        for (ItemSlot slot : this.slots) {
            if (!slot.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public long getModCount() {
        return this.modCount;
    }

    @Override
    public void markDirty() {
        this.modCount++;
    }

    @Override
    public void setModCount(long value) {
        this.modCount = value;
    }
}
