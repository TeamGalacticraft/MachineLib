package dev.galacticraft.machinelib.api.component;

import dev.galacticraft.machinelib.api.storage.slot.ItemSlot;
import dev.galacticraft.machinelib.api.storage.slot.MachineItemSlot;
import dev.galacticraft.machinelib.impl.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ItemContexts {
    static ItemContext create(ItemSlot slot) {
        if (slot instanceof MachineItemSlot) {
            return create((MachineItemSlot)slot);
        }
        return Services.ITEM_CONTEXTS._create(slot);
    }

    static ItemContext create(MachineItemSlot slot) {
        return Services.ITEM_CONTEXTS._create(slot);
    }

    static ItemContext createImmutable(ItemStack stack) {
        return Services.ITEM_CONTEXTS._createImmutable(stack);
    }

    static ItemContext createImmutable(Item item, int amount) {
        return Services.ITEM_CONTEXTS._createImmutable(item, amount);
    }

    static ItemContext createImmutable(Item item, CompoundTag tag, int amount) {
        return Services.ITEM_CONTEXTS._createImmutable(item, tag, amount);
    }

    static ItemContext playerHand(Player player, InteractionHand hand) {
        return Services.ITEM_CONTEXTS._playerHand(player, hand);
    }
    static ItemContext playerCursor(Player player, AbstractContainerMenu menu) {
        return Services.ITEM_CONTEXTS._playerCursor(player, menu);
    }
    static ItemContext playerInteract(Player player, InteractionHand hand) {
        return Services.ITEM_CONTEXTS._playerInteract(player, hand);
    }

    static <C> C toPlatform(ItemContext context) {
        return Services.ITEM_CONTEXTS._toPlatform(context);
    }

    ItemContext _create(ItemSlot slot);
    ItemContext _create(MachineItemSlot slot);
    ItemContext _createImmutable(ItemStack stack);
    ItemContext _createImmutable(Item item, int amount);
    ItemContext _createImmutable(Item item, CompoundTag tag, int amount);
    ItemContext _playerHand(Player player, InteractionHand hand);
    ItemContext _playerCursor(Player player, AbstractContainerMenu menu);
    ItemContext _playerInteract(Player player, InteractionHand hand);
    <C> C _toPlatform(ItemContext context);
}
