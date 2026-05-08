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

package dev.galacticraft.machinelib.impl.storage;

import dev.galacticraft.machinelib.api.multiblock.port.MultiblockPortTarget;
import dev.galacticraft.machinelib.api.storage.MachineEnergyStorage;
import dev.galacticraft.machinelib.api.transfer.ResourceFlow;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.Set;

/**
 * Empty zero-capacity energy storage.
 */
public class EmptyMachineEnergyStorage implements MachineEnergyStorage {

    public static final MachineEnergyStorage INSTANCE = new EmptyMachineEnergyStorage();

    @Override
    public boolean canExtract(final long amount) {
        return false;
    }

    @Override
    public boolean canInsert(final long amount) {
        return false;
    }

    @Override
    public long tryExtract(final long amount) {
        return 0;
    }

    @Override
    public long tryInsert(final long amount) {
        return 0;
    }

    @Override
    public long extract(final long amount) {
        return 0;
    }

    @Override
    public long insert(final long amount) {
        return 0;
    }

    @Override
    public boolean extractExact(final long amount) {
        return false;
    }

    @Override
    public boolean insertExact(final long amount) {
        return false;
    }

    @Override
    public long extract(
            final long amount,
            final @NotNull TransactionContext transaction
    ) {
        return 0;
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return 0;
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public long insert(
            final long amount,
            final @NotNull TransactionContext transaction
    ) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public boolean isFull() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void setEnergy(
            final long amount,
            final @Nullable TransactionContext context
    ) {

    }

    @Override
    public void setEnergy(final long amount) {

    }

    @Override
    public @Nullable ResourceLocation id() {
        return null;
    }

    @Override
    public @NotNull Set<ResourceLocation> groups() {
        return Set.of();
    }

    @Override
    public @Nullable EnergyStorage getExposedStorage(final @NotNull ResourceFlow flow) {
        return null;
    }

    @Override
    public @Nullable EnergyStorage getExposedStorage(
            final @NotNull ResourceFlow flow,
            final @NotNull MultiblockPortTarget target
    ) {
        return null;
    }

    @Override
    public long externalInsertionRate() {
        return 0;
    }

    @Override
    public long externalExtractionRate() {
        return 0;
    }

    @Override
    public void setParent(final BlockEntity parent) {

    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public @NotNull LongTag createTag() {
        return LongTag.valueOf(0);
    }

    @Override
    public void readTag(final @NotNull LongTag tag) {

    }

    @Override
    public long getModifications() {
        return -1;
    }

    @Override
    public boolean hasChanged(final long[] previous) {
        return false;
    }

    @Override
    public void copyInto(final long[] other) {

    }

    @Override
    public void readPacket(final @NotNull ByteBuf buf) {

    }

    @Override
    public void writePacket(final @NotNull ByteBuf buf) {

    }

    @Override
    public long @Nullable [] createEquivalent() {
        return null;
    }
}