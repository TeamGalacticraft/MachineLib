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

package dev.galacticraft.api.gas;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GasFluid extends Fluid implements FluidVariantAttributeHandler {
    @ApiStatus.Internal
    public static final List<GasFluid> GAS_FLUIDS = new ArrayList<>(); // used for registering client hooks

    private final @NotNull Text name;
    private final @NotNull String symbol;
    private final @NotNull Identifier texture;
    private final int tint;
    private final @NotNull Object2IntFunction<FluidVariant> luminance;
    private final @NotNull Object2IntFunction<FluidVariant> viscosity;
    private final @NotNull Optional<SoundEvent> fillSound;
    private final @NotNull Optional<SoundEvent> emptySound;

    @Contract("_, _, _ -> new")
    public static @NotNull GasFluid create(@NotNull Text name, @NotNull Identifier texture, @NotNull String symbol) {
        return create(name, texture, symbol, v -> 0);
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull GasFluid create(@NotNull Text name, @NotNull Identifier texture, @NotNull String symbol, @NotNull Object2IntFunction<FluidVariant> luminance) {
        return create(name, texture, symbol, luminance, v -> 50);
    }

    @Contract("_, _, _, _, _ -> new")
    public static @NotNull GasFluid create(@NotNull Text name, @NotNull Identifier texture, @NotNull String symbol, @NotNull Object2IntFunction<FluidVariant> luminance, @NotNull Object2IntFunction<FluidVariant> viscosity) {
        return create(name, texture, symbol, luminance, viscosity, Optional.empty(), Optional.empty());
    }

    @Contract("_, _, _, _, _, _, _ -> new")
    public static @NotNull GasFluid create(@NotNull Text name, @NotNull Identifier texture, @NotNull String symbol, @NotNull Object2IntFunction<FluidVariant> luminance, @NotNull Object2IntFunction<FluidVariant> viscosity, @NotNull Optional<SoundEvent> fillSound, @NotNull Optional<SoundEvent> emptySound) {
        return create(name, texture, symbol, 0xFFFFFFFF, luminance, viscosity, fillSound, emptySound);
    }
    
    @Contract("_, _, _, _ -> new")
    public static @NotNull GasFluid create(@NotNull Text name, @NotNull Identifier texture, @NotNull String symbol, int tint) {
        return create(name, texture, symbol, tint, v -> 0);
    }

    @Contract("_, _, _, _, _ -> new")
    public static @NotNull GasFluid create(@NotNull Text name, @NotNull Identifier texture, @NotNull String symbol, int tint, @NotNull Object2IntFunction<FluidVariant> luminance) {
        return create(name, texture, symbol, tint, luminance, v -> 50);
    }

    @Contract("_, _, _, _, _, _ -> new")
    public static @NotNull GasFluid create(@NotNull Text name, @NotNull Identifier texture, @NotNull String symbol, int tint, @NotNull Object2IntFunction<FluidVariant> luminance, @NotNull Object2IntFunction<FluidVariant> viscosity) {
        return create(name, texture, symbol, tint, luminance, viscosity, Optional.empty(), Optional.empty());
    }

    @Contract("_, _, _, _, _, _, _, _ -> new")
    public static @NotNull GasFluid create(@NotNull Text name, @NotNull Identifier texture, @NotNull String symbol, int tint, @NotNull Object2IntFunction<FluidVariant> luminance, @NotNull Object2IntFunction<FluidVariant> viscosity, @NotNull Optional<SoundEvent> fillSound, @NotNull Optional<SoundEvent> emptySound) {
        return new GasFluid(name, texture, symbol, tint, luminance, viscosity, fillSound, emptySound);
    }

    private GasFluid(@NotNull Text name, @NotNull Identifier texture, @NotNull String symbol, int tint, @NotNull Object2IntFunction<FluidVariant> luminance, @NotNull Object2IntFunction<FluidVariant> viscosity, @NotNull Optional<SoundEvent> fillSound, @NotNull Optional<SoundEvent> emptySound) {
        this.name = name;
        this.symbol = symbol.replaceAll("0", "₀")
                .replaceAll("1", "₁")
                .replaceAll("2", "₂")
                .replaceAll("3", "₃")
                .replaceAll("4", "₄")
                .replaceAll("5", "₅")
                .replaceAll("6", "₆")
                .replaceAll("7", "₇")
                .replaceAll("8", "₈")
                .replaceAll("9", "₉");
        this.texture = texture;
        this.tint = tint;
        this.luminance = luminance;
        this.viscosity = viscosity;
        this.fillSound = fillSound;
        this.emptySound = emptySound;

        GAS_FLUIDS.add(this);
        FluidVariantAttributes.register(this, this);
    }

    @Override
    public Item getBucketItem() {
        return Items.AIR;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return true;
    }

    @Override
    protected Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
        return Vec3d.ZERO;
    }

    @Override
    public int getTickRate(WorldView world) {
        return 0;
    }

    @Override
    protected float getBlastResistance() {
        return 0;
    }

    @Override
    public float getHeight(FluidState state, BlockView world, BlockPos pos) {
        return 0.0F;
    }

    @Override
    public float getHeight(FluidState state) {
        return 0.0F;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean isStill(FluidState state) {
        return true;
    }

    @Override
    public int getLevel(FluidState state) {
        return 0;
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public Text getName(FluidVariant fluidVariant) {
        return this.name;
    }

    @Override
    public Optional<SoundEvent> getFillSound(FluidVariant variant) {
        return this.fillSound;
    }

    @Override
    public Optional<SoundEvent> getEmptySound(FluidVariant variant) {
        return this.emptySound;
    }

    @Override
    public int getLuminance(FluidVariant variant) {
        return this.luminance.getInt(variant);
    }

    @Override
    public int getViscosity(FluidVariant variant, @Nullable World world) {
        return this.viscosity.getInt(variant);
    }

    @Override
    public boolean isLighterThanAir(FluidVariant variant) {
        return true;
    }

    public String getSymbolForDisplay() {
        return this.symbol;
    }

    public Identifier getTexture() {
        return texture;
    }

    public int getTint() {
        return tint;
    }
}
