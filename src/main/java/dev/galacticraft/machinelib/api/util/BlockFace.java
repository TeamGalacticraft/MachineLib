/*
 * Copyright (c) 2021-2026 Team Galacticraft
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

package dev.galacticraft.machinelib.api.util;

import dev.galacticraft.machinelib.impl.Constant;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An enum representing a face of a block.
 * Used in machine I/O face calculations.
 */
public enum BlockFace implements StringRepresentable {
    /**
     * The face of a block that is facing forwards.
     */
    FRONT("Front", Component.translatable(Constant.TranslationKey.FRONT), true),
    /**
     * The face of a block that is facing to the right, when facing in the direction the block is facing.
     */
    RIGHT("Right", Component.translatable(Constant.TranslationKey.LEFT), true),
    /**
     * The face of a block that is facing backwards.
     */
    BACK("Back", Component.translatable(Constant.TranslationKey.BACK), true),
    /**
     * The face of a block that is facing to the left, when facing in the direction the block is facing.
     */
    LEFT("Left", Component.translatable(Constant.TranslationKey.RIGHT), true),
    /**
     * The top face of a block.
     */
    TOP("Top", Component.translatable(Constant.TranslationKey.TOP), false),
    /**
     * The bottom face of a block.
     */
    BOTTOM("Bottom", Component.translatable(Constant.TranslationKey.BOTTOM), false);

    public static final StreamCodec<ByteBuf, BlockFace> CODEC = ByteBufCodecs.BYTE.map(i -> values()[i], face -> (byte) face.ordinal());

    private final String id;
    /**
     * The text of the face
     */
    private final Component name;
    /**
     * Whether the face is considered the side of a block.
     * Includes all faces except for top and bottom.
     */
    private final boolean side;

    /**
     * Constructs a block face.
     *
     * @param name the name of the block face
     * @param side whether the block face is a side face or not
     */
    BlockFace(String id, @NotNull MutableComponent name, boolean side) {
        this.id = id;
        this.name = name.setStyle(Constant.Text.GOLD_STYLE);
        this.side = side;
    }

    /**
     * Gets the face corresponding to the given direction and rotation (derived from the block state).
     *
     * @param state the block state to get the rotation from
     * @param target the direction to get the face for
     * @return the face corresponding to the given direction and rotation
     */
    public static @Nullable BlockFace from(@NotNull BlockState state, @Nullable Direction target) {
        return from(state.getValue(BlockStateProperties.HORIZONTAL_FACING), target);
    }

    /**
     * Gets the face corresponding to the given direction and rotation.
     *
     * @param facing the rotation to get the face for
     * @param target the direction to get the face for
     * @return the face corresponding to the given direction and rotation
     */
    @Contract(pure = true, value = "_, null -> null; _, !null -> !null")
    public static @Nullable BlockFace from(@NotNull Direction facing, @Nullable Direction target) { //todo: a better way to do this?
        assert facing != Direction.UP && facing != Direction.DOWN;

        if (target == null) return null;

        if (target == Direction.DOWN) {
            return BOTTOM;
        } else if (target == Direction.UP) {
            return TOP;
        }

        return switch (facing) {
            case NORTH -> switch (target) {
                case NORTH -> FRONT;
                case EAST -> RIGHT;
                case SOUTH -> BACK;
                case WEST -> LEFT;
                default -> throw new IllegalStateException("Unexpected value: " + target);
            };
            case EAST -> switch (target) {
                case EAST -> FRONT;
                case NORTH -> LEFT;
                case WEST -> BACK;
                case SOUTH -> RIGHT;
                default -> throw new IllegalStateException("Unexpected value: " + target);
            };
            case SOUTH -> switch (target) {
                case SOUTH -> FRONT;
                case WEST -> RIGHT;
                case NORTH -> BACK;
                case EAST -> LEFT;
                default -> throw new IllegalStateException("Unexpected value: " + target);
            };
            case WEST -> switch (target) {
                case WEST -> FRONT;
                case SOUTH -> LEFT;
                case EAST -> BACK;
                case NORTH -> RIGHT;
                default -> throw new IllegalStateException("Unexpected value: " + target);
            };
            default -> throw new IllegalStateException("Unexpected value: " + target);
        };
    }

    /**
     * The text of this face.
     *
     * @return The text of this face.
     */
    @Contract(pure = true)
    public Component getName() {
        return name;
    }

    /**
     * Converts this face to the corresponding direction, based on the rotation of the block.
     *
     * @param facing The rotation of the block.
     * @return The corresponding direction.
     */
    @Contract(pure = true)
    public @NotNull Direction toDirection(@NotNull Direction facing) {
        assert facing != Direction.UP && facing != Direction.DOWN;

        if (this == BOTTOM) {
            return Direction.DOWN;
        } else if (this == TOP) {
            return Direction.UP;
        }

        return switch (facing) {
            case NORTH -> switch (this) {
                case FRONT -> Direction.NORTH;
                case RIGHT -> Direction.EAST;
                case BACK -> Direction.SOUTH;
                case LEFT -> Direction.WEST;
                default -> throw new IllegalStateException("Unexpected value: " + this);
            };
            case EAST -> switch (this) {
                case RIGHT -> Direction.SOUTH;
                case FRONT -> Direction.EAST;
                case LEFT -> Direction.NORTH;
                case BACK -> Direction.WEST;
                default -> throw new IllegalStateException("Unexpected value: " + this);
            };
            case SOUTH -> switch (this) {
                case BACK -> Direction.NORTH;
                case LEFT -> Direction.EAST;
                case FRONT -> Direction.SOUTH;
                case RIGHT -> Direction.WEST;
                default -> throw new IllegalStateException("Unexpected value: " + this);
            };
            case WEST -> switch (this) {
                case LEFT -> Direction.SOUTH;
                case BACK -> Direction.EAST;
                case RIGHT -> Direction.NORTH;
                case FRONT -> Direction.WEST;
                default -> throw new IllegalStateException("Unexpected value: " + this);
            };
            default -> throw new IllegalStateException("Unexpected value: " + facing);
        };
    }

    /**
     * {@return the face opposite to this one}
     */
    @Contract(pure = true)
    public @NotNull BlockFace getOpposite() {
        return switch (this) {
            case BOTTOM -> TOP;
            case TOP -> BOTTOM;
            case BACK -> FRONT;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case FRONT -> BACK;
        };
    }

    /**
     * {@return whether this face is the side of a block}
     */
    @Contract(pure = true)
    public boolean side() {
        return this.side;
    }

    /**
     * {@return whether this face is the top or bottom of a block}
     */
    @Contract(pure = true)
    public boolean base() {
        return !this.side;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
