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

package dev.galacticraft.machinelib.testmod;

import dev.galacticraft.api.screen.SimpleMachineScreenHandler;
import dev.galacticraft.machinelib.testmod.block.TestBlock;
import dev.galacticraft.machinelib.testmod.block.entity.TestBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMod implements ModInitializer {
    public static final String MOD_ID = "machinelib-test";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String TEST_BLOCK_ID = "test_block";
    public static final Block TEST_BLOCK = new TestBlock(FabricBlockSettings.of(Material.METAL));
    public static final BlockEntityType<TestBlockEntity> TEST_BE_TYPE = FabricBlockEntityTypeBuilder.create(TestBlockEntity::new, TEST_BLOCK).build();
    public static final ScreenHandlerType<SimpleMachineScreenHandler<TestBlockEntity>> TEST_SH_TYPE = new ExtendedScreenHandlerType<>(SimpleMachineScreenHandler.createFactory(() -> TestMod.TEST_SH_TYPE));

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing MachineLib test mod");
        assert FabricLoader.getInstance().isDevelopmentEnvironment() : "Test mod loaded outside of development environment!?";
        Registry.register(Registry.BLOCK, id(TEST_BLOCK_ID), TEST_BLOCK);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, id(TEST_BLOCK_ID), TEST_BE_TYPE);
        Registry.register(Registry.SCREEN_HANDLER, id(TEST_BLOCK_ID), TEST_SH_TYPE);
    }

    @Contract("_ -> new")
    public static @NotNull Identifier id(@NotNull String id) {
        return new Identifier(MOD_ID, id);
    }
}
