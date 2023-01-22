/*
 * Copyright (c) 2021-2023 Team Galacticraft
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

package dev.galacticraft.machinelib.api.block;

import com.mojang.authlib.GameProfile;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.AccessLevel;
import dev.galacticraft.machinelib.api.machine.RedstoneActivation;
import dev.galacticraft.machinelib.api.machine.SecuritySettings;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.storage.slot.SlotGroup;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.block.entity.MachineBlockEntityTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The base block for all machines.
 *
 * @see MachineBlockEntity
 */
public abstract class MachineBlock<T extends MachineBlockEntity> extends BaseEntityBlock {
    /**
     * This property represents whether the machine is active.
     * It is used for world rendering purposes.
     */
    public static final BooleanProperty ACTIVE = Constant.Property.ACTIVE;

    /**
     * Creates a new machine block.
     *
     * @param settings The settings for the block.
     */
    protected MachineBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING, ACTIVE);
    }

    @Override
    public abstract T newBlockEntity(BlockPos pos, BlockState state);

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide && placer instanceof Player player) {
            SecuritySettings security = ((MachineBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).getSecurity();
            if (security.getOwner() == null) security.setOwner(/*((MinecraftServerTeamsGetter) world.getServer()).getSpaceRaceTeams(), */player); //todo: teams
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public final void appendHoverText(ItemStack stack, BlockGetter view, List<Component> tooltip, @NotNull TooltipFlag context) {
        Component text = machineDescription(stack, view, context.isAdvanced());
        if (text != null) {
            if (Screen.hasShiftDown()) {
                char[] line = text.getContents() instanceof TranslatableContents content ? I18n.get(content.getKey()).toCharArray() : text.getString().toCharArray();
                int len = 0;
                final int maxLength = 175;
                StringBuilder builder = new StringBuilder();
                for (char c : line) {
                    len += Minecraft.getInstance().font.width(String.valueOf(c));
                    if (c == ' ' && len >= maxLength) {
                        len = 0;
                        tooltip.add(Component.literal(builder.toString()).setStyle(text.getStyle()));
                        builder = new StringBuilder();
                        continue;
                    }
                    builder.append(c);
                }
                tooltip.add(Component.literal(builder.toString()).setStyle(text.getStyle()));
            } else {
                tooltip.add(Component.translatable(Constant.TranslationKey.PRESS_SHIFT).setStyle(Constant.Text.DARK_GRAY_STYLE));
            }
        }

        if (stack != null && stack.getTag() != null && stack.getTag().contains(Constant.Nbt.BLOCK_ENTITY_TAG)) {
            CompoundTag nbt = stack.getTag().getCompound(Constant.Nbt.BLOCK_ENTITY_TAG);
            tooltip.add(Component.empty());
            if (nbt.contains(Constant.Nbt.ENERGY, Tag.TAG_INT))
                tooltip.add(Component.translatable(Constant.TranslationKey.CURRENT_ENERGY, Component.literal(String.valueOf(nbt.getInt(Constant.Nbt.ENERGY))).setStyle(Constant.Text.BLUE_STYLE)).setStyle(Constant.Text.GOLD_STYLE));
            if (nbt.contains(Constant.Nbt.SECURITY, Tag.TAG_COMPOUND)) {
                CompoundTag security = nbt.getCompound(Constant.Nbt.SECURITY);
                if (security.contains(Constant.Nbt.OWNER, Tag.TAG_COMPOUND)) {
                    GameProfile profile = NbtUtils.readGameProfile(security.getCompound(Constant.Nbt.OWNER));
                    if (profile != null) {
                        MutableComponent text1 = Component.translatable(Constant.TranslationKey.OWNER, Component.literal(profile.getName()).setStyle(Constant.Text.LIGHT_PURPLE_STYLE)).setStyle(Constant.Text.GRAY_STYLE);
                        if (Screen.hasControlDown()) {
                            text1.append(Component.literal(" (" + profile.getId().toString() + ")").setStyle(Constant.Text.AQUA_STYLE));
                        }
                        tooltip.add(text1);
                    } else {
                        tooltip.add(Component.translatable(Constant.TranslationKey.OWNER, Component.translatable(Constant.TranslationKey.UNKNOWN).setStyle(Constant.Text.LIGHT_PURPLE_STYLE)).setStyle(Constant.Text.GRAY_STYLE));
                    }
                    tooltip.add(Component.translatable(Constant.TranslationKey.ACCESS_LEVEL, AccessLevel.fromString(security.getString(Constant.Nbt.ACCESS_LEVEL)).getName()).setStyle(Constant.Text.GREEN_STYLE));
                }
            }

            if (nbt.contains(Constant.Nbt.REDSTONE_ACTIVATION, Tag.TAG_BYTE)) {
                tooltip.add(Component.translatable(Constant.TranslationKey.REDSTONE_ACTIVATION, RedstoneActivation.readTag((ByteTag) Objects.requireNonNull(nbt.get(Constant.Nbt.REDSTONE_ACTIVATION))).getName()).setStyle(Constant.Text.DARK_RED_STYLE));
            }
        }
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public final @NotNull InteractionResult use(BlockState state, @NotNull Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof MachineBlockEntity machine) {
                SecuritySettings security = machine.getSecurity();
                if (security.getOwner() == null)
                    security.setOwner(/*((MinecraftServerTeamsGetter) world.getServer()).getSpaceRaceTeams(), */player); //todo: teams
                if (security.isOwner(player)) {
                    MenuProvider factory = state.getMenuProvider(world, pos);

                    if (factory != null) {
                        player.openMenu(factory);
                        security.sendPacket(pos, (ServerPlayer) player);
                        machine.getRedstoneActivation().sendPacket(pos, (ServerPlayer) player);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(world, pos, state, player);
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof MachineBlockEntity machine) {
            MachineItemStorage inv = machine.itemStorage();
            List<ItemEntity> entities = new ArrayList<>();
            for (SlotGroup<Item, ItemStack, ItemResourceSlot> group : inv) {
                for (ItemResourceSlot slot : group) {
                    if (!slot.isEmpty()) {
                        entities.add(new ItemEntity(world, pos.getX(), pos.getY() + 1, pos.getZ(), slot.copyStack()));
                    }
                }
            }
            for (ItemEntity itemEntity : entities) {
                world.addFreshEntity(itemEntity);
            }
        }
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootContext.@NotNull Builder builder) {
        BlockEntity entity = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (entity instanceof MachineBlockEntity machine) {
            if (machine.areDropsDisabled()) return Collections.emptyList();
        }
        return super.getDrops(state, builder);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(BlockGetter view, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(view, pos, state);

        BlockEntity blockEntity = view.getBlockEntity(pos);
        if (blockEntity != null) { // todo: limit to IO config
            stack.getOrCreateTag().put(Constant.Nbt.BLOCK_ENTITY_TAG, blockEntity.saveWithoutMetadata());
        }

        return stack;
    }

    @NotNull
    @Override
    public <B extends BlockEntity> BlockEntityTicker<B> getTicker(Level world, BlockState state, BlockEntityType<B> type) {
        return MachineBlockEntityTicker.getInstance();
    }

    /**
     * Returns this machine's description for the tooltip when left shift is pressed.
     *
     * @param stack    The item stack (the contained item is this block).
     * @param view     The world.
     * @param advanced Whether advanced tooltips are enabled.
     * @return This machine's description.
     */
    public abstract @Nullable Component machineDescription(ItemStack stack, BlockGetter view, boolean advanced);
}
