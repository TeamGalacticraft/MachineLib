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

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.material.Fluids;

public final class GraphicsUtil {
    private GraphicsUtil() {
    }

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

        int tileWidth = 16;
        int tileHeight = 16;
        int fluidHeight = (int) (((double) available / (double) capacity) * height);
        int startY = fillFromTop ? y : y + (height - fluidHeight);
        int depth = startY + fluidHeight;
        float u0 = sprite.getU0();
        float v0 = sprite.getV0();

        for (int splitX = 0; splitX < width; splitX += tileWidth) {
            int realWidth = Math.min(width - splitX, tileWidth);
            for (int splitY = startY; splitY < depth; splitY += tileHeight) {
                int realHeight = Math.min(depth - splitY, tileHeight);
                float u1 = sprite.getU((float) realWidth / (float) tileWidth);
                float v1 = sprite.getV((float) realHeight / (float) tileHeight);
                innerBlit(graphics.pose(), sprite.atlasLocation(), x + splitX, x + splitX + realWidth, splitY, splitY + realHeight, 0, u0, u1, v0, v1, r, g, b, 1.0f);
            }
        }
    }

    private static void innerBlit(PoseStack poseStack, ResourceLocation resourceLocation, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, float r, float g, float b, float a) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.addVertex(matrix, (float)x0, (float)y0, (float)z).setUv(u0, v0).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, (float)x0, (float)y1, (float)z).setUv(u0, v1).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, (float)x1, (float)y1, (float)z).setUv(u1, v1).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, (float)x1, (float)y0, (float)z).setUv(u1, v0).setColor(r, g, b, a);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void highlightElement(GuiGraphics graphics, int left, int top, int x, int y, int width, int height, int color) {
        color |= (0xFF << 24);
        color ^= (0b1110000 << 24);
        graphics.fill(left + x, top + y, left + x + width, top + y + height, color);
    }
}
