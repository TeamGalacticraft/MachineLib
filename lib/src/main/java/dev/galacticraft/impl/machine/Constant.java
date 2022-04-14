/*
 * Copyright (c) 2021-${year} ${company}
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

package dev.galacticraft.impl.machine;

import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface Constant {
    String MOD_ID = "machinelib";

    interface Text {
        Style DARK_GRAY_STYLE = Style.EMPTY.withColor(Formatting.DARK_GRAY);
        Style GOLD_STYLE = Style.EMPTY.withColor(Formatting.GOLD);
        Style GREEN_STYLE = Style.EMPTY.withColor(Formatting.GREEN);
        Style RED_STYLE = Style.EMPTY.withColor(Formatting.RED);
        Style BLUE_STYLE = Style.EMPTY.withColor(Formatting.BLUE);
        Style AQUA_STYLE = Style.EMPTY.withColor(Formatting.AQUA);
        Style GRAY_STYLE = Style.EMPTY.withColor(Formatting.GRAY);
        Style DARK_RED_STYLE = Style.EMPTY.withColor(Formatting.DARK_RED);
        Style LIGHT_PURPLE_STYLE = Style.EMPTY.withColor(Formatting.LIGHT_PURPLE);
        Style YELLOW_STYLE = Style.EMPTY.withColor(Formatting.YELLOW);
        Style WHITE_STYLE = Style.EMPTY.withColor(Formatting.WHITE);

        static Style getStorageLevelColor(double scale) {
            return Style.EMPTY.withColor(TextColor.fromRgb(((int)(255 * scale) << 16) + (((int)(255 * ( 1.0 - scale))) << 8)));
        }

        static Style getRainbow(int ticks) {
            return Style.EMPTY.withColor(TextColor.fromRgb(MathHelper.hsvToRgb(ticks / 1000.0f, 1, 1)));
        }
    }

    interface Nbt {
        String BLOCK_ENTITY_TAG = "BlockEntityTag";
        String NO_DROP = "NoDrop";
        String OWNER = "Owner";
        String PROGRESS = "Progress";
        String TEAM = "Team";
        String ACCESSIBILITY = "Accessibility";
        String SECURITY = "Security";
        String CONFIGURATION = "Configuration";
        String VALUE = "Value";
        String ENERGY = "Energy";
        String REDSTONE_ACTIVATION = "RedstoneActivation";
        String MATCH = "Match";
        String IS_SLOT_ID = "IsSlotId";
        String MAX_PROGRESS = "MaxProgress";
        String RESOURCE = "Resource";
        String FLOW = "Flow";
        String AMOUNT = "Amount";
        String ENERGY_STORAGE = "EnergyStorage";
        String ITEM_STORAGE = "ItemStorage";
        String FLUID_STORAGE = "FluidStorage";
    }

    interface Property {
        BooleanProperty ACTIVE = BooleanProperty.of("active");
    }

    interface ScreenTexture {
        Identifier MACHINE_CONFIG_PANELS = new Identifier(Constant.MOD_ID, "textures/gui/machine_panels.png");
        Identifier OVERLAY_BARS = new Identifier(Constant.MOD_ID, "textures/gui/overlay_bars.png");
    }

    interface TextureCoordinate {
        int OVERLAY_TEX_WIDTH = 64;
        int OVERLAY_TEX_HEIGHT = 64;

        int OVERLAY_WIDTH = 16;
        int OVERLAY_HEIGHT = 48;

        int ENERGY_BACKGROUND_X = 0;
        int ENERGY_BACKGROUND_Y = 0;
        int ENERGY_X = 16;
        int ENERGY_Y = 0;

        int OXYGEN_BACKGROUND_X = 32;
        int OXYGEN_BACKGROUND_Y = 0;
        int OXYGEN_X = 48;
        int OXYGEN_Y = 0;
    }
}
