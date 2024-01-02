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

package dev.galacticraft.machinelib.testmod;

import dev.galacticraft.machinelib.testmod.block.TestModBlocks;
import dev.galacticraft.machinelib.testmod.block.TestModMachineTypes;
import dev.galacticraft.machinelib.testmod.block.entity.TestModBlockEntityTypes;
import dev.galacticraft.machinelib.testmod.item.TestModItems;
import dev.galacticraft.machinelib.testmod.menu.TestModMenuTypes;
import net.fabricmc.api.ModInitializer;

public class TestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        Constant.LOGGER.info("Initializing MachineLib test mod");
        TestModBlocks.register();
        TestModItems.register();
        TestModBlockEntityTypes.register();
        TestModMenuTypes.register();
        TestModMachineTypes.initialize();
    }

}
