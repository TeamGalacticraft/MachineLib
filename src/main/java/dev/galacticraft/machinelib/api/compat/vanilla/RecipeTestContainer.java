package dev.galacticraft.machinelib.api.compat.vanilla;

import dev.galacticraft.machinelib.api.misc.Modifiable;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class RecipeTestContainer implements Container, Modifiable {
    private final ItemResourceSlot[] slots;
    private final long[] modifications;
    private long modCount = 0;

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull RecipeTestContainer create(ItemResourceSlot @NotNull ... slots) {
        assert slots.length > 0;
        return new RecipeTestContainer(slots);
    }

    private RecipeTestContainer(ItemResourceSlot[] slots) {
        this.slots = slots;
        this.modifications = new long[this.slots.length];
    }

    @Override
    public int getContainerSize() {
        return this.slots.length;
    }

    @Override
    public boolean isEmpty() {
        for (ItemResourceSlot slot : this.slots) {
            if (!slot.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        return ItemStackUtil.copy(this.slots[i]);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {

    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public void clearContent() {}

    @Override
    public long getModifications() {
        for (int i = 0; i < this.slots.length; i++) {
            if (this.modifications[i] != (this.modifications[i] = this.slots[i].getModifications())) {
                this.modCount++;
            }
        }
        return this.modCount;
    }
}
