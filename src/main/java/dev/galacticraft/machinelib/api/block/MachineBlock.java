/*
 * Copyright (c) 2021-2024 Team Galacticraft
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
import dev.galacticraft.machinelib.api.machine.configuration.AccessLevel;
import dev.galacticraft.machinelib.api.machine.configuration.RedstoneActivation;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.storage.MachineItemStorage;
import dev.galacticraft.machinelib.api.storage.slot.ItemResourceSlot;
import dev.galacticraft.machinelib.api.util.ItemStackUtil;
import dev.galacticraft.machinelib.client.api.util.DisplayUtil;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.block.entity.MachineBlockEntityTicker;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.storage.loot.LootParams;
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
public class MachineBlock<Machine extends MachineBlockEntity> extends BaseEntityBlock {
    /**
     * Represents a boolean property for specifying the active state of the machine.
     * @see MachineBlockEntity#isActive() for the definition of 'active'
     */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    /**
     * Tooltip prompt text. Shown instead of the long-form description when shift is not pressed.
     *
     * @see #shiftDescription(ItemStack, BlockGetter, TooltipFlag)
     */
    private static final Component PRESS_SHIFT = Component.translatable(Constant.TranslationKey.PRESS_SHIFT).setStyle(Constant.Text.DARK_GRAY_STYLE);

    /**
     * Factory that constructs the relevant machine block entity for this block.
     */
    private final MachineBlockEntityFactory<Machine> factory;

    /**
     * The line-wrapped long description of this machine.
     *
     * @see #shiftDescription(ItemStack, BlockGetter, TooltipFlag)
     */
    private List<Component> description = null;

    /**
     * Creates a new machine block.
     *
     * @param settings The settings for the block.
     * @param factory the machine block entity factory
     */
    public MachineBlock(Properties settings, MachineBlockEntityFactory<Machine> factory) {
        super(settings);
        this.factory = factory;
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING, ACTIVE);
    }

    @Override
    public Machine newBlockEntity(BlockPos pos, BlockState state) {
        return this.factory.create(pos, state);
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
                SecuritySettings security = machine.getSecurity();
                if (!security.hasOwner()) {
                    security.setOwner(player.getUUID(), player.getGameProfile().getName());
                }
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

    /**
     * @see #shiftDescription
     */
    @Override
    public void appendHoverText(ItemStack stack, BlockGetter view, List<Component> tooltip, @NotNull TooltipFlag context) {
        Component text = this.shiftDescription(stack, view, context);
        if (text != null) {
            if (Screen.hasShiftDown()) {
                if (this.description == null) {
                    this.description = DisplayUtil.wrapText(text, 128);
                }
                tooltip.addAll(this.description);
            } else {
                tooltip.add(PRESS_SHIFT);
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
                        MutableComponent owner = Component.translatable(Constant.TranslationKey.OWNER, Component.literal(profile.getName()).setStyle(Constant.Text.LIGHT_PURPLE_STYLE)).setStyle(Constant.Text.GRAY_STYLE);
                        if (Screen.hasControlDown()) {
                            owner.append(Component.literal(" (" + profile.getId().toString() + ")").setStyle(Constant.Text.AQUA_STYLE));
                        }
                        tooltip.add(owner);
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
    public final @NotNull InteractionResult use(BlockState state, @NotNull Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof MachineBlockEntity machine) {
                SecuritySettings security = machine.getSecurity();
                if (!security.hasOwner()) {
                    security.setOwner(player.getUUID(), player.getGameProfile().getName()); //todo: teams
                }
                if (security.hasAccess(player)) {
                    player.openMenu(machine);
                    return InteractionResult.CONSUME;
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
            if (!machine.areDropsDisabled()) {
                MachineItemStorage inv = machine.itemStorage();
                List<ItemEntity> entities = new ArrayList<>();
                for (ItemResourceSlot slot : inv.getSlots()) {
                    if (!slot.isEmpty()) {
                        entities.add(new ItemEntity(world, pos.getX(), pos.getY() + 1, pos.getZ(), ItemStackUtil.create(slot)));
                        slot.set(null, null, 0);
                    }
                }
                for (ItemEntity itemEntity : entities) {
                    world.addFreshEntity(itemEntity);
                }
            }
        }
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.@NotNull Builder builder) {
        return builder.getParameter(LootContextParams.BLOCK_ENTITY) instanceof MachineBlockEntity machine
                && machine.areDropsDisabled() ? Collections.emptyList()
                : super.getDrops(state, builder);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(BlockGetter view, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(view, pos, state);

        BlockEntity blockEntity = view.getBlockEntity(pos);
        if (blockEntity instanceof MachineBlockEntity machine) {
            CompoundTag config = new CompoundTag();
            config.put(Constant.Nbt.CONFIGURATION, machine.getConfiguration().createTag());
            stack.getOrCreateTag().put(Constant.Nbt.BLOCK_ENTITY_TAG, config);
        }

        return stack;
    }

    @Nullable
    @Override
    public <B extends BlockEntity> BlockEntityTicker<B> getTicker(Level world, BlockState state, BlockEntityType<B> type) {
        return !world.isClientSide ? MachineBlockEntityTicker.getInstance() : null;
    }

    /**
     * Returns this machine's description for the tooltip when left shift is pressed.
     *
     * @param stack The item stack (the contained item is this block).
     * @param view The world.
     * @param context Extra tooltip information.
     * @return This machine's description.
     */
    public @Nullable Component shiftDescription(ItemStack stack, BlockGetter view, TooltipFlag context) {
        return Component.translatable(this.getDescriptionId() + ".description");
    }

    /**
     * Factory for creating {@link MachineBlockEntity}s.
     *
     * @param <Machine> the type of machine this factory creates
     */
    @FunctionalInterface
    public interface MachineBlockEntityFactory<Machine extends MachineBlockEntity> {
        @Nullable Machine create(@NotNull BlockPos pos, @NotNull BlockState state);
    }
}
