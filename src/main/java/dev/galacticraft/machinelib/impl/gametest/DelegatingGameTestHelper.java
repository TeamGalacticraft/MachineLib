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

package dev.galacticraft.machinelib.impl.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.*;

public abstract class DelegatingGameTestHelper extends GameTestHelper {
    protected GameTestHelper helper;

    public DelegatingGameTestHelper(GameTestHelper helper) {
        super(null);
        this.helper = helper;
    }

    @Override
    public ServerLevel getLevel() {
        return this.helper.getLevel();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.helper.getBlockState(pos);
    }

    @Override
    public <T extends BlockEntity> T getBlockEntity(BlockPos pos) {
        return this.helper.getBlockEntity(pos);
    }

    @Override
    public void killAllEntities() {
        this.helper.killAllEntities();
    }

    @Override
    public void killAllEntitiesOfClass(Class entityClass) {
        this.helper.killAllEntitiesOfClass(entityClass);
    }

    @Override
    public ItemEntity spawnItem(Item item, Vec3 pos) {
        return this.helper.spawnItem(item, pos);
    }

    @Override
    public ItemEntity spawnItem(Item item, float x, float y, float z) {
        return this.helper.spawnItem(item, x, y, z);
    }

    @Override
    public ItemEntity spawnItem(Item item, BlockPos pos) {
        return this.helper.spawnItem(item, pos);
    }

    @Override
    public <E extends Entity> E spawn(EntityType<E> type, BlockPos pos) {
        return this.helper.spawn(type, pos);
    }

    @Override
    public <E extends Entity> E spawn(EntityType<E> type, Vec3 pos) {
        return this.helper.spawn(type, pos);
    }

    @Override
    public <E extends Entity> E findOneEntity(EntityType<E> type) {
        return this.helper.findOneEntity(type);
    }

    @Override
    public <E extends Entity> E findClosestEntity(EntityType<E> type, int x, int y, int z, double margin) {
        return this.helper.findClosestEntity(type, x, y, z, margin);
    }

    @Override
    public <E extends Entity> List<E> findEntities(EntityType<E> type, int x, int y, int z, double margin) {
        return this.helper.findEntities(type, x, y, z, margin);
    }

    @Override
    public <E extends Entity> List<E> findEntities(EntityType<E> type, Vec3 pos, double margin) {
        return this.helper.findEntities(type, pos, margin);
    }

    @Override
    public <E extends Entity> E spawn(EntityType<E> type, int x, int y, int z) {
        return this.helper.spawn(type, x, y, z);
    }

    @Override
    public <E extends Entity> E spawn(EntityType<E> type, float x, float y, float z) {
        return this.helper.spawn(type, x, y, z);
    }

    @Override
    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> type, BlockPos pos) {
        return this.helper.spawnWithNoFreeWill(type, pos);
    }

    @Override
    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> type, int x, int y, int z) {
        return this.helper.spawnWithNoFreeWill(type, x, y, z);
    }

    @Override
    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> type, Vec3 pos) {
        return this.helper.spawnWithNoFreeWill(type, pos);
    }

    @Override
    public <E extends Mob> E spawnWithNoFreeWill(EntityType<E> type, float x, float y, float z) {
        return this.helper.spawnWithNoFreeWill(type, x, y, z);
    }

    @Override
    public void moveTo(Mob entity, float x, float y, float z) {
        this.helper.moveTo(entity, x, y, z);
    }

    @Override
    public GameTestSequence walkTo(Mob entity, BlockPos pos, float speed) {
        return this.helper.walkTo(entity, pos, speed);
    }

    @Override
    public void pressButton(int x, int y, int z) {
        this.helper.pressButton(x, y, z);
    }

    @Override
    public void pressButton(BlockPos pos) {
        this.helper.pressButton(pos);
    }

    @Override
    public void useBlock(BlockPos pos) {
        this.helper.useBlock(pos);
    }

    @Override
    public void useBlock(BlockPos pos, Player player) {
        this.helper.useBlock(pos, player);
    }

    @Override
    public void useBlock(BlockPos pos, Player player, BlockHitResult result) {
        this.helper.useBlock(pos, player, result);
    }

    @Override
    public LivingEntity makeAboutToDrown(LivingEntity entity) {
        return this.helper.makeAboutToDrown(entity);
    }

    @Override
    public LivingEntity withLowHealth(LivingEntity entity) {
        return this.helper.withLowHealth(entity);
    }

    @Override
    public Player makeMockPlayer(GameType gameType) {
        return this.helper.makeMockPlayer(gameType);
    }

    @Deprecated(forRemoval = true)
    @Override
    public ServerPlayer makeMockServerPlayerInLevel() {
        return this.helper.makeMockServerPlayerInLevel();
    }

    @Override
    public void pullLever(int x, int y, int z) {
        this.helper.pullLever(x, y, z);
    }

    @Override
    public void pullLever(BlockPos pos) {
        this.helper.pullLever(pos);
    }

    @Override
    public void pulseRedstone(BlockPos pos, long delay) {
        this.helper.pulseRedstone(pos, delay);
    }

    @Override
    public void destroyBlock(BlockPos pos) {
        this.helper.destroyBlock(pos);
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {
        this.helper.setBlock(x, y, z, block);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState state) {
        this.helper.setBlock(x, y, z, state);
    }

    @Override
    public void setBlock(BlockPos pos, Block block) {
        this.helper.setBlock(pos, block);
    }

    @Override
    public void setBlock(BlockPos pos, BlockState state) {
        this.helper.setBlock(pos, state);
    }

    @Override
    public void setNight() {
        this.helper.setNight();
    }

    @Override
    public void setDayTime(int timeOfDay) {
        this.helper.setDayTime(timeOfDay);
    }

    @Override
    public void assertBlockPresent(Block block, int x, int y, int z) {
        this.helper.assertBlockPresent(block, x, y, z);
    }

    @Override
    public void assertBlockPresent(Block block, BlockPos pos) {
        this.helper.assertBlockPresent(block, pos);
    }

    @Override
    public void assertBlockNotPresent(Block block, int x, int y, int z) {
        this.helper.assertBlockNotPresent(block, x, y, z);
    }

    @Override
    public void assertBlockNotPresent(Block block, BlockPos pos) {
        this.helper.assertBlockNotPresent(block, pos);
    }

    @Override
    public void succeedWhenBlockPresent(Block block, int x, int y, int z) {
        this.helper.succeedWhenBlockPresent(block, x, y, z);
    }

    @Override
    public void succeedWhenBlockPresent(Block block, BlockPos pos) {
        this.helper.succeedWhenBlockPresent(block, pos);
    }

    @Override
    public void assertBlock(BlockPos pos, Predicate<Block> predicate, String errorMessage) {
        this.helper.assertBlock(pos, predicate, errorMessage);
    }

    @Override
    public void assertBlock(BlockPos pos, Predicate<Block> predicate, Supplier<String> errorMessageSupplier) {
        this.helper.assertBlock(pos, predicate, errorMessageSupplier);
    }

    @Override
    public <T extends Comparable<T>> void assertBlockProperty(BlockPos pos, Property<T> dataSlot, T value) {
        this.helper.assertBlockProperty(pos, dataSlot, value);
    }

    @Override
    public <T extends Comparable<T>> void assertBlockProperty(BlockPos pos, Property<T> dataSlot, Predicate<T> predicate, String errorMessage) {
        this.helper.assertBlockProperty(pos, dataSlot, predicate, errorMessage);
    }

    @Override
    public void assertBlockState(BlockPos pos, Predicate<BlockState> predicate, Supplier<String> errorMessageSupplier) {
        this.helper.assertBlockState(pos, predicate, errorMessageSupplier);
    }

    @Override
    public <T extends BlockEntity> void assertBlockEntityData(BlockPos pos, Predicate<T> predicate, Supplier<String> errorMessageSupplier) {
        this.helper.assertBlockEntityData(pos, predicate, errorMessageSupplier);
    }

    @Override
    public void assertRedstoneSignal(BlockPos pos, Direction axisDirection, IntPredicate powerPredicate, Supplier<String> errorMessage) {
        this.helper.assertRedstoneSignal(pos, axisDirection, powerPredicate, errorMessage);
    }

    @Override
    public void assertEntityPresent(EntityType<?> type) {
        this.helper.assertEntityPresent(type);
    }

    @Override
    public void assertEntityPresent(EntityType<?> type, int x, int y, int z) {
        this.helper.assertEntityPresent(type, x, y, z);
    }

    @Override
    public void assertEntityPresent(EntityType<?> type, BlockPos pos) {
        this.helper.assertEntityPresent(type, pos);
    }

    @Override
    public void assertEntityPresent(EntityType<?> type, Vec3 pos, Vec3 pos1) {
        this.helper.assertEntityPresent(type, pos, pos1);
    }

    @Override
    public void assertEntitiesPresent(EntityType<?> type, int amount) {
        this.helper.assertEntitiesPresent(type, amount);
    }

    @Override
    public void assertEntitiesPresent(EntityType<?> type, BlockPos pos, int amount, double radius) {
        this.helper.assertEntitiesPresent(type, pos, amount, radius);
    }

    @Override
    public void assertEntityPresent(EntityType<?> type, BlockPos pos, double radius) {
        this.helper.assertEntityPresent(type, pos, radius);
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityType<T> type, BlockPos pos, double radius) {
        return this.helper.getEntities(type, pos, radius);
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityType<T> type) {
        return this.helper.getEntities(type);
    }

    @Override
    public void assertEntityInstancePresent(Entity entityGoalInfo, int x, int y, int z) {
        this.helper.assertEntityInstancePresent(entityGoalInfo, x, y, z);
    }

    @Override
    public void assertEntityInstancePresent(Entity entityGoalInfo, BlockPos pos) {
        this.helper.assertEntityInstancePresent(entityGoalInfo, pos);
    }

    @Override
    public void assertItemEntityCountIs(Item item, BlockPos pos, double radius, int amount) {
        this.helper.assertItemEntityCountIs(item, pos, radius, amount);
    }

    @Override
    public void assertItemEntityPresent(Item item, BlockPos pos, double radius) {
        this.helper.assertItemEntityPresent(item, pos, radius);
    }

    @Override
    public void assertItemEntityNotPresent(Item item, BlockPos pos, double radius) {
        this.helper.assertItemEntityNotPresent(item, pos, radius);
    }

    @Override
    public void assertItemEntityPresent(Item item) {
        this.helper.assertItemEntityPresent(item);
    }

    @Override
    public void assertItemEntityNotPresent(Item item) {
        this.helper.assertItemEntityNotPresent(item);
    }

    @Override
    public void assertEntityNotPresent(EntityType<?> type) {
        this.helper.assertEntityNotPresent(type);
    }

    @Override
    public void assertEntityNotPresent(EntityType<?> type, int x, int y, int z) {
        this.helper.assertEntityNotPresent(type, x, y, z);
    }

    @Override
    public void assertEntityNotPresent(EntityType<?> type, BlockPos pos) {
        this.helper.assertEntityNotPresent(type, pos);
    }

    @Override
    public void assertEntityNotPresent(EntityType<?> type, Vec3 pos, Vec3 pos1) {
        this.helper.assertEntityNotPresent(type, pos, pos1);
    }

    @Override
    public void assertEntityTouching(EntityType<?> type, double x, double y, double z) {
        this.helper.assertEntityTouching(type, x, y, z);
    }

    @Override
    public void assertEntityNotTouching(EntityType<?> type, double x, double y, double z) {
        this.helper.assertEntityNotTouching(type, x, y, z);
    }

    @Override
    public <E extends Entity, T> void assertEntityData(BlockPos pos, EntityType<E> type, Function<? super E, T> entityDataGetter, @Nullable T data) {
        this.helper.assertEntityData(pos, type, entityDataGetter, data);
    }

    @Override
    public <E extends LivingEntity> void assertEntityIsHolding(BlockPos pos, EntityType<E> entityType, Item item) {
        this.helper.assertEntityIsHolding(pos, entityType, item);
    }

    @Override
    public <E extends Entity & InventoryCarrier> void assertEntityInventoryContains(BlockPos pos, EntityType<E> entityType, Item item) {
        this.helper.assertEntityInventoryContains(pos, entityType, item);
    }

    @Override
    public void assertContainerEmpty(BlockPos pos) {
        this.helper.assertContainerEmpty(pos);
    }

    @Override
    public void assertContainerContains(BlockPos pos, Item item) {
        this.helper.assertContainerContains(pos, item);
    }

    @Override
    public void assertSameBlockStates(BoundingBox checkedBlockBox, BlockPos correctStatePos) {
        this.helper.assertSameBlockStates(checkedBlockBox, correctStatePos);
    }

    @Override
    public void assertSameBlockState(BlockPos checkedPos, BlockPos correctStatePos) {
        this.helper.assertSameBlockState(checkedPos, correctStatePos);
    }

    @Override
    public void assertAtTickTimeContainerContains(long delay, BlockPos pos, Item item) {
        this.helper.assertAtTickTimeContainerContains(delay, pos, item);
    }

    @Override
    public void assertAtTickTimeContainerEmpty(long delay, BlockPos pos) {
        this.helper.assertAtTickTimeContainerEmpty(delay, pos);
    }

    @Override
    public <E extends Entity, T> void succeedWhenEntityData(BlockPos pos, EntityType<E> type, Function<E, T> entityDataGetter, T data) {
        this.helper.succeedWhenEntityData(pos, type, entityDataGetter, data);
    }

    @Override
    public void assertEntityPosition(Entity entityGoalInfo, AABB aABB, String message) {
        this.helper.assertEntityPosition(entityGoalInfo, aABB, message);
    }

    @Override
    public <E extends Entity> void assertEntityProperty(E entityGoalInfo, Predicate<E> predicate, String testName) {
        this.helper.assertEntityProperty(entityGoalInfo, predicate, testName);
    }

    @Override
    public <E extends Entity, T> void assertEntityProperty(E entityGoalInfo, Function<E, T> propertyGetter, String propertyName, T expectedValue) {
        this.helper.assertEntityProperty(entityGoalInfo, propertyGetter, propertyName, expectedValue);
    }

    @Override
    public void assertLivingEntityHasMobEffect(LivingEntity entity, Holder<MobEffect> effect, int amplifier) {
        this.helper.assertLivingEntityHasMobEffect(entity, effect, amplifier);
    }

    @Override
    public void succeedWhenEntityPresent(EntityType<?> type, int x, int y, int z) {
        this.helper.succeedWhenEntityPresent(type, x, y, z);
    }

    @Override
    public void succeedWhenEntityPresent(EntityType<?> type, BlockPos pos) {
        this.helper.succeedWhenEntityPresent(type, pos);
    }

    @Override
    public void succeedWhenEntityNotPresent(EntityType<?> type, int x, int y, int z) {
        this.helper.succeedWhenEntityNotPresent(type, x, y, z);
    }

    @Override
    public void succeedWhenEntityNotPresent(EntityType<?> type, BlockPos pos) {
        this.helper.succeedWhenEntityNotPresent(type, pos);
    }

    @Override
    public void succeed() {
        this.helper.succeed();
    }

    @Override
    public void succeedIf(Runnable runnable) {
        this.helper.succeedIf(runnable);
    }

    @Override
    public void succeedWhen(Runnable runnable) {
        this.helper.succeedWhen(runnable);
    }

    @Override
    public void succeedOnTickWhen(int duration, Runnable runnable) {
        this.helper.succeedOnTickWhen(duration, runnable);
    }

    @Override
    public void runAtTickTime(long tick, Runnable runnable) {
        this.helper.runAtTickTime(tick, runnable);
    }

    @Override
    public void runAfterDelay(long ticks, Runnable runnable) {
        this.helper.runAfterDelay(ticks, runnable);
    }

    @Override
    public void randomTick(BlockPos pos) {
        this.helper.randomTick(pos);
    }

    @Override
    public void tickPrecipitation(BlockPos pos) {
        this.helper.tickPrecipitation(pos);
    }

    @Override
    public void tickPrecipitation() {
        this.helper.tickPrecipitation();
    }

    @Override
    public int getHeight(Heightmap.Types heightmap, int x, int z) {
        return this.helper.getHeight(heightmap, x, z);
    }

    @Override
    public void fail(String message, BlockPos pos) {
        this.helper.fail(message, pos);
    }

    @Override
    public void fail(String message, Entity entityGoalInfo) {
        this.helper.fail(message, entityGoalInfo);
    }

    @Override
    public void fail(String message) {
        this.helper.fail(message);
    }

    @Override
    public void failIf(Runnable task) {
        this.helper.failIf(task);
    }

    @Override
    public void failIfEver(Runnable task) {
        this.helper.failIfEver(task);
    }

    @Override
    public GameTestSequence startSequence() {
        return this.helper.startSequence();
    }

    @Override
    public BlockPos absolutePos(BlockPos pos) {
        return this.helper.absolutePos(pos);
    }

    @Override
    public BlockPos relativePos(BlockPos pos) {
        return this.helper.relativePos(pos);
    }

    @Override
    public Vec3 absoluteVec(Vec3 pos) {
        return this.helper.absoluteVec(pos);
    }

    @Override
    public Vec3 relativeVec(Vec3 pos) {
        return this.helper.relativeVec(pos);
    }

    @Override
    public Rotation getTestRotation() {
        return this.helper.getTestRotation();
    }

    @Override
    public void assertTrue(boolean condition, String message) {
        this.helper.assertTrue(condition, message);
    }

    @Override
    public <N> void assertValueEqual(N value, N expected, String name) {
        this.helper.assertValueEqual(value, expected, name);
    }

    @Override
    public void assertFalse(boolean condition, String message) {
        this.helper.assertFalse(condition, message);
    }

    @Override
    public long getTick() {
        return this.helper.getTick();
    }

    @Override
    public AABB getBounds() {
        return this.helper.getBounds();
    }

    @Override
    public void forEveryBlockInStructure(Consumer<BlockPos> posConsumer) {
        this.helper.forEveryBlockInStructure(posConsumer);
    }

    @Override
    public void onEachTick(Runnable runnable) {
        this.helper.onEachTick(runnable);
    }

    @Override
    public void placeAt(Player player, ItemStack stack, BlockPos pos, Direction axisDirection) {
        this.helper.placeAt(player, stack, pos, axisDirection);
    }

    @Override
    public void setBiome(ResourceKey<Biome> biome) {
        this.helper.setBiome(biome);
    }
}
