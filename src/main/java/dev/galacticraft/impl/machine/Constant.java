/*
 * Copyright (c) 2019-2022 Team Galacticraft
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
import net.minecraft.util.math.MathHelper;

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
        String SIZE = "Size";
        String MAX_SIZE = "MaxSize";
        String FUEL_TIME = "FuelTime";
        String FUEL_LENGTH = "FuelLength";
        String TEAM = "Team";
        String ACCESSIBILITY = "Accessibility";
        String SECURITY = "Security";
        String CONFIGURATION = "Configuration";
        String VALUE = "Value";
        String ENERGY = "Energy";
        String AUTOMATION_TYPE = "AutomationType";
        String BABY = "Baby";
        String DIRECTION = "Direction";
        String REDSTONE_INTERACTION_TYPE = "RedstoneInteraction";
        String MATCH = "Match";
        String IS_SLOT_ID = "IsSlotId";
        String MAX_PROGRESS = "MaxProgress";
        String COLOR = "Color";
        String PULL = "Pull";
        String HEAT = "Heat";
        String INPUTS = "Inputs";
        String OUTPUTS = "Outputs";
        String SHAPED = "Shaped";
        String ITEMS = "Items";
        String GASES = "Gases";
        String NAME = "Name";
        String ID = "Id";
        String RESOURCE = "Resource";
        String FLOW = "Flow";
        String AMOUNT = "Amount";
    }

    interface Property {
        BooleanProperty ACTIVE = BooleanProperty.of("active");
    }
}
