/*
 * Copyright (c) 2021-2022 Team Galacticraft
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

package dev.galacticraft.api.block;

import com.mojang.authlib.GameProfile;
import dev.galacticraft.api.block.entity.MachineBlockEntity;
import dev.galacticraft.api.machine.AccessLevel;
import dev.galacticraft.api.machine.RedstoneActivation;
import dev.galacticraft.api.machine.SecuritySettings;
import dev.galacticraft.api.machine.storage.MachineItemStorage;
import dev.galacticraft.impl.MLConstant;
import dev.galacticraft.impl.block.entity.MachineBlockEntityTicker;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The base block for all machines.
 *
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 * @see MachineBlockEntity
 */
public abstract class MachineBlock<T extends MachineBlockEntity> extends BlockWithEntity {
    /**
     * This property represents whether the machine is active.
     * It is used for world rendering purposes.
     */
    public static final BooleanProperty ACTIVE = MLConstant.Property.ACTIVE;

    /**
     * Creates a new machine block.
     * @param settings The settings for the block.
     */
    protected MachineBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.HORIZONTAL_FACING, ACTIVE);
    }

    @Override
    public abstract T createBlockEntity(BlockPos pos, BlockState state);

    @Override
    public BlockState getPlacementState(@NotNull ItemPlacementContext context) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, context.getPlayerFacing().getOpposite()).with(ACTIVE, false);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && placer instanceof PlayerEntity player) {
            ((MachineBlockEntity) world.getBlockEntity(pos)).getSecurity().setOwner(/*((MinecraftServerTeamsGetter) world.getServer()).getSpaceRaceTeams(), */player); //todo: teams
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public final void appendTooltip(ItemStack stack, BlockView view, List<Text> tooltip, @NotNull TooltipContext context) {
        Text text = machineDescription(stack, view, context.isAdvanced());
        if (text != null) {
            if (Screen.hasShiftDown()) {
                char[] line = text instanceof TranslatableText ? I18n.translate(((TranslatableText) text).getKey()).toCharArray() : text.getString().toCharArray();
                int len = 0;
                final int maxLength = 175;
                StringBuilder builder = new StringBuilder();
                for (char c : line) {
                    len += MinecraftClient.getInstance().textRenderer.getWidth(String.valueOf(c));
                    if (c == ' ' && len >= maxLength) {
                        len = 0;
                        tooltip.add(new LiteralText(builder.toString()).setStyle(text.getStyle()));
                        builder = new StringBuilder();
                        continue;
                    }
                    builder.append(c);
                }
                tooltip.add(new LiteralText(builder.toString()).setStyle(text.getStyle()));
            } else {
                tooltip.add(new TranslatableText(MLConstant.TranslationKey.PRESS_SHIFT).setStyle(MLConstant.Text.DARK_GRAY_STYLE));
            }
        }

        if (stack != null && stack.getNbt() != null && stack.getNbt().contains(MLConstant.Nbt.BLOCK_ENTITY_TAG)) {
            NbtCompound nbt = stack.getNbt().getCompound(MLConstant.Nbt.BLOCK_ENTITY_TAG);
            tooltip.add(LiteralText.EMPTY);
            if (nbt.contains(MLConstant.Nbt.ENERGY, NbtElement.INT_TYPE)) tooltip.add(new TranslatableText(MLConstant.TranslationKey.CURRENT_ENERGY, new LiteralText(String.valueOf(nbt.getInt(MLConstant.Nbt.ENERGY))).setStyle(MLConstant.Text.BLUE_STYLE)).setStyle(MLConstant.Text.GOLD_STYLE));
            if (nbt.contains(MLConstant.Nbt.SECURITY, NbtElement.COMPOUND_TYPE)) {
                NbtCompound security = nbt.getCompound(MLConstant.Nbt.SECURITY);
                if (security.contains(MLConstant.Nbt.OWNER, NbtElement.COMPOUND_TYPE)) {
                    GameProfile profile = NbtHelper.toGameProfile(security.getCompound(MLConstant.Nbt.OWNER));
                    if (profile != null) {
                        MutableText text1 = new TranslatableText(MLConstant.TranslationKey.OWNER, new LiteralText(profile.getName()).setStyle(MLConstant.Text.LIGHT_PURPLE_STYLE)).setStyle(MLConstant.Text.GRAY_STYLE);
                        if (Screen.hasControlDown()) {
                            text1.append(new LiteralText(" (" + profile.getId().toString() + ")").setStyle(MLConstant.Text.AQUA_STYLE));
                        }
                        tooltip.add(text1);
                    } else {
                        tooltip.add(new TranslatableText(MLConstant.TranslationKey.OWNER, new TranslatableText(MLConstant.TranslationKey.UNKNOWN).setStyle(MLConstant.Text.LIGHT_PURPLE_STYLE)).setStyle(MLConstant.Text.GRAY_STYLE));
                    }
                    tooltip.add(new TranslatableText(MLConstant.TranslationKey.ACCESS_LEVEL, AccessLevel.fromString(security.getString(MLConstant.Nbt.ACCESS_LEVEL)).getName()).setStyle(MLConstant.Text.GREEN_STYLE));
                }
            }
            tooltip.add(new TranslatableText(MLConstant.TranslationKey.REDSTONE_ACTIVATION, RedstoneActivation.readNbt(nbt).getName()).setStyle(MLConstant.Text.DARK_RED_STYLE));
        }
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.BLOCK;
    }

    @Override
    public final ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof MachineBlockEntity machine) {
                SecuritySettings security = machine.getSecurity();
                if (security.getOwner() == null) security.setOwner(/*((MinecraftServerTeamsGetter) world.getServer()).getSpaceRaceTeams(), */player); //todo: teams
                if (security.isOwner(player.getGameProfile())) {
                    NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(world, pos);

                    if (factory != null) {
                        player.openHandledScreen(factory);
                        security.sendPacket(pos, (ServerPlayerEntity) player);
                        machine.getRedstoneActivation().sendPacket(pos, (ServerPlayerEntity) player);
                        return ActionResult.CONSUME;
                    }
                }
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof MachineBlockEntity machine) {
            MachineItemStorage inv = machine.itemStorage();
            List<ItemEntity> entities = new ArrayList<>();
            try (Transaction transaction = Transaction.openOuter()) {
                inv.iterator(transaction).forEachRemaining(view -> entities.add(new ItemEntity(world, pos.getX(), pos.getY() + 1, pos.getZ(), view.getResource().toStack(Math.toIntExact(view.extract(view.getResource(), view.getAmount(), transaction))))));
                transaction.commit();
            }
            for (ItemEntity itemEntity : entities) {
                world.spawnEntity(itemEntity);
            }
        }
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.@NotNull Builder builder) {
        BlockEntity entity = builder.get(LootContextParameters.BLOCK_ENTITY);
        if (entity instanceof MachineBlockEntity machine) {
            if (machine.areDropsDisabled()) return Collections.emptyList();
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    public ItemStack getPickStack(BlockView view, BlockPos pos, BlockState state) {
        ItemStack stack = super.getPickStack(view, pos, state);
        NbtCompound nbt = (stack.getNbt() != null ? stack.getNbt() : new NbtCompound());
        BlockEntity blockEntity = view.getBlockEntity(pos);
        if (blockEntity != null) { // todo: limit to IO config
            nbt.put(MLConstant.Nbt.BLOCK_ENTITY_TAG, blockEntity.createNbt());
        }

        stack.setNbt(nbt);
        return stack;
    }

    @NotNull
    @Override
    public <B extends BlockEntity> BlockEntityTicker<B> getTicker(World world, BlockState state, BlockEntityType<B> type) {
        return MachineBlockEntityTicker.getInstance();
    }

    /**
     * Returns this machine's description for the tooltip when left shift is pressed.
     * @param stack The item stack (the contained item is this block).
     * @param view The world.
     * @param advanced Whether advanced tooltips are enabled.
     * @return This machine's description.
     */
    public abstract @Nullable Text machineDescription(ItemStack stack, BlockView view, boolean advanced);
}
