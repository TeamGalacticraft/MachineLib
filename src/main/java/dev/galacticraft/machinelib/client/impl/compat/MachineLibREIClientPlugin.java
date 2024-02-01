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

package dev.galacticraft.machinelib.client.impl.compat;

import dev.architectury.event.CompoundEventResult;
import dev.galacticraft.machinelib.client.api.screen.MachineScreen;
import dev.galacticraft.machinelib.impl.Constant;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MachineLibREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerFocusedStack((screen, mouse) -> {
            if (screen instanceof MachineScreen<?,?> machineScreen) {
                if (machineScreen.hoveredTank != null && !machineScreen.hoveredTank.isEmpty()) {
                    return CompoundEventResult.interruptTrue(EntryStacks.of(machineScreen.hoveredTank.getFluid(), machineScreen.hoveredTank.getAmount()));
                }
            }
            return CompoundEventResult.pass();
        });
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(MachineScreen.class, screen -> {
            List<Rectangle> areas = new ArrayList<>();
            if (MachineScreen.Tab.STATS.isOpen() || MachineScreen.Tab.SECURITY.isOpen()) {
                areas.add(new Rectangle(screen.getX() + screen.getImageWidth(), screen.getY() + (MachineScreen.Tab.STATS.isOpen() ? 0 : Constant.TextureCoordinate.TAB_HEIGHT), Constant.TextureCoordinate.PANEL_WIDTH, Constant.TextureCoordinate.PANEL_HEIGHT));
                areas.add(new Rectangle(screen.getX() + screen.getImageWidth(), screen.getY() + Constant.TextureCoordinate.TAB_HEIGHT, Constant.TextureCoordinate.TAB_WIDTH, Constant.TextureCoordinate.PANEL_HEIGHT));
            }
            areas.add(new Rectangle(screen.getX() + screen.getImageWidth(), screen.getY(), Constant.TextureCoordinate.TAB_WIDTH, Constant.TextureCoordinate.TAB_HEIGHT * 2));
            return areas;
        });
    }
}
