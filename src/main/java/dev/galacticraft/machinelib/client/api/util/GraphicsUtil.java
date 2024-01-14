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

package dev.galacticraft.machinelib.client.api.util;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.material.Fluids;

public final class GraphicsUtil {
    private GraphicsUtil() {}

    public static void drawFluid(GuiGraphics graphics, int x, int y, int width, int height, long capacity, FluidVariant variant, long available) {
        if (variant.isBlank()) return;
        boolean fillFromTop = FluidVariantAttributes.isLighterThanAir(variant);
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(variant);
        int fluidColor = FluidVariantRendering.getColor(variant);

        float r = FastColor.ARGB32.red(fluidColor) / 255.0f;
        float g = FastColor.ARGB32.green(fluidColor) / 255.0f;
        float b = FastColor.ARGB32.blue(fluidColor) / 255.0f;

        if (sprite == null) {
            sprite = FluidVariantRendering.getSprite(FluidVariant.of(Fluids.WATER));
            assert sprite != null;
        }

        int fluidHeight = (int) (((double) available / (double) capacity) * height);
        int startY = fillFromTop ? y : y + (height - fluidHeight);

        for (int splitX = 0; splitX < width; splitX += Math.min(width, 16)){
            int realWidth = Math.min(width - splitX, 16);
            for (int splitY = startY; splitY < startY + fluidHeight; splitY += realWidth){
                graphics.blit(x + splitX, splitY, 0, realWidth, Math.min(realWidth, startY + fluidHeight - splitY), sprite, r, g, b, 1.0f);
            }
        }
    }

    public static void highlightElement(GuiGraphics graphics, int left, int top, int x, int y, int width, int height, int color) {
        color |= (0xFF << 24);
        color ^= (0b1110000 << 24);
        graphics.fill(left + x, top + y, left + x + width, top + y + height, color);
    }
}
