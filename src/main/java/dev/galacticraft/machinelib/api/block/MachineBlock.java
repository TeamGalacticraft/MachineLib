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

package dev.galacticraft.machinelib.api.block;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.MapCodec;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.machine.configuration.AccessLevel;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneMode;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import dev.galacticraft.machinelib.client.api.util.DisplayUtil;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.block.entity.MachineBlockEntityTicker;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The base block for all machines.
 */
public abstract class MachineBlock extends BaseBlock {
    /**
     * Represents a boolean property for specifying the active state of the machine.
     *
     * @see MachineBlockEntity#isActive() for the definition of 'active'
     */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    /**
     * Tooltip prompt text. Shown instead of the long-form description when shift is not pressed.
     */
    protected static final Component PRESS_SHIFT = Component.translatable(Constant.TranslationKey.PRESS_SHIFT).setStyle(Constant.Text.DARK_GRAY_STYLE);

    /**
     * Creates a new machine block.
     *
     * @param settings The settings for the block.
     */
    public MachineBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.getStateDefinition().any().setValue(ACTIVE, false));
    }

    /**
     * Updates the active state of a machine block in the specified level at a given position.
     *
     * @param level The level in which the machine block exists.
     * @param pos The position of the machine block.
     * @param state The current state of the machine block.
     * @param b The new value for the active state.
     */
    public static void updateActiveState(Level level, BlockPos pos, BlockState state, boolean b) {
        level.setBlock(pos, state.setValue(ACTIVE, b), 2);
    }

    /**
     * Determines whether a machine block is active or not based on its state.
     *
     * @param state The state of the machine block.
     * @return {@code true} if the machine block is active, {@code false} otherwise.
     */
    public static boolean isActive(@NotNull BlockState state) {
        return state.getValue(ACTIVE);
    }

    protected static void appendBlockEntityTooltip(ItemStack stack, List<Component> tooltip) {
        if (stack != null) {
            CustomData data = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
            if (!data.isEmpty()) {
                CompoundTag nbt = data.getUnsafe();
                tooltip.add(Component.empty());
                if (nbt.contains(Constant.Nbt.ENERGY, Tag.TAG_INT)) {
                    tooltip.add(Component.translatable(Constant.TranslationKey.CURRENT_ENERGY, Component.literal(String.valueOf(nbt.getInt(Constant.Nbt.ENERGY))).setStyle(Constant.Text.BLUE_STYLE)).setStyle(Constant.Text.GOLD_STYLE));
                }

                if (nbt.contains(Constant.Nbt.SECURITY, Tag.TAG_COMPOUND)) {
                    CompoundTag security = nbt.getCompound(Constant.Nbt.SECURITY);
                    if (security.hasUUID(Constant.Nbt.OWNER)) {
                        UUID ownerUuid = security.getUUID(Constant.Nbt.OWNER);
                        Optional<GameProfile> profile = SkullBlockEntity.fetchGameProfile(ownerUuid).getNow(Optional.empty());
                        MutableComponent name = profile.isPresent() ? Component.literal(profile.get().getName()) : Component.translatable(Constant.TranslationKey.UNKNOWN);
                        MutableComponent owner = Component.translatable(Constant.TranslationKey.OWNER_TOOLTIP, name.setStyle(Constant.Text.LIGHT_PURPLE_STYLE)).setStyle(Constant.Text.GRAY_STYLE);
                        if (Screen.hasControlDown()) {
                            owner.append(Component.literal(" (" + ownerUuid.toString() + ")").setStyle(Constant.Text.AQUA_STYLE));
                        }
                        tooltip.add(owner);
                        tooltip.add(Component.translatable(Constant.TranslationKey.ACCESS_LEVEL, AccessLevel.fromString(security.getString(Constant.Nbt.ACCESS_LEVEL)).getName()).setStyle(Constant.Text.GREEN_STYLE));
                    }
                }

                if (nbt.contains(Constant.Nbt.REDSTONE_MODE, Tag.TAG_BYTE)) {
                    tooltip.add(Component.translatable(Constant.TranslationKey.REDSTONE_MODE, RedstoneMode.readTag(Objects.requireNonNull(nbt.get(Constant.Nbt.REDSTONE_MODE))).getName()).setStyle(Constant.Text.DARK_RED_STYLE));
                }
            }
        }
    }

    protected abstract @NotNull MapCodec<? extends BaseEntityBlock> codec();

    @Override
    public abstract @Nullable MachineBlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, level, pos, block, fromPos, notify);
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
                machine.getState().setPowered(level.hasNeighborSignal(pos));
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, placer, itemStack);
        if (!level.isClientSide && placer instanceof ServerPlayer player) {
            if (level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
                machine.getSecurity().tryUpdate(player.getUUID());
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState blockState2, boolean bl) {
        super.onPlace(state, level, pos, blockState2, bl);
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
                machine.getState().setPowered(level.hasNeighborSignal(pos));
            }
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.addAll(DisplayUtil.wrapText(Component.translatable(this.getDescriptionId() + ".description").withStyle(Constant.Text.GRAY_STYLE), 150));
        } else {
            tooltip.add(PRESS_SHIFT);
        }

        appendBlockEntityTooltip(stack, tooltip);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof MachineBlockEntity machine) {
                SecuritySettings security = machine.getSecurity();

                security.tryUpdate(player.getUUID());
                if (security.hasAccess(player)) {
                    return super.useWithoutItem(state, level, pos, player, hit);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof MachineBlockEntity machine) {
            if (!machine.areDropsDisabled()) {
                MachineItemStorage inv = machine.itemStorage();
                List<ItemEntity> entities = new ArrayList<>();
                for (ItemResourceSlot slot : inv.getSlots()) {
                    if (!slot.isEmpty()) {
                        entities.add(new ItemEntity(level, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, ItemStackUtil.create(slot)));
                        slot.set(null, DataComponentPatch.EMPTY, 0);
                    }
                }
                for (ItemEntity itemEntity : entities) {
                    level.addFreshEntity(itemEntity);
                }
            }
        }

        return state;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.@NotNull Builder builder) {
        return builder.getParameter(LootContextParams.BLOCK_ENTITY) instanceof MachineBlockEntity machine
                && machine.areDropsDisabled() ? Collections.emptyList()
                : super.getDrops(state, builder);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(LevelReader reader, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(reader, pos, state);

        if (Screen.hasAltDown()) {
            // todo: add a separate item to copy/paste config data that also works in survival instead of this
            BlockEntity blockEntity = reader.getBlockEntity(pos);
            if (blockEntity instanceof MachineBlockEntity machine) {
                CompoundTag config = new CompoundTag();
                config.put(Constant.Nbt.CONFIGURATION, machine.getIOConfig().createTag());
                config.put(Constant.Nbt.SECURITY, machine.getSecurity().createTag());
                config.put(Constant.Nbt.REDSTONE_MODE, machine.getRedstoneMode().createTag());
                BlockItem.setBlockEntityData(stack, blockEntity.getType(), config);
            }
        }

        return stack;
    }

    @Nullable
    @Override
    public <B extends BlockEntity> BlockEntityTicker<B> getTicker(Level level, BlockState state, BlockEntityType<B> type) {
        return !level.isClientSide ? MachineBlockEntityTicker.getInstance() : null;
    }
}
