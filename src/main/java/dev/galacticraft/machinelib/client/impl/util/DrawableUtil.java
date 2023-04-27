/*
 * Copyright (c) 2021-2023 Team Galacticraft
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

package dev.galacticraft.machinelib.client.impl.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.galacticraft.machinelib.impl.Constant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class DrawableUtil {
    private DrawableUtil() {
    }

    public static int normalize(int dist, int value) {
        return value - dist;
    }

    public static double normalize(double dist, double value) {
        return value - dist;
    }

    public static void drawCenteredStringWithoutShadow(PoseStack matrices, Font textRenderer, Component text, int x, int y, int color) {
        textRenderer.draw(matrices, text.getVisualOrderText(), (float) (x - textRenderer.width(text) / 2), (float) y, color);
    }

    public static void drawOxygenBuffer(PoseStack matrices, int x, int y, int oxygen, int capacity) {
        if (oxygen == 0 && capacity == 0) capacity = 1;
        drawOxygenBuffer(matrices, x, y, (float) oxygen / (float) capacity);
    }

    public static void drawOxygenBuffer(PoseStack matrices, int x, int y, float scale) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Constant.ScreenTexture.OVERLAY_BARS);
        drawProgressTexture(matrices, x, y, 0, Constant.TextureCoordinate.OXYGEN_BACKGROUND_X, Constant.TextureCoordinate.OXYGEN_BACKGROUND_Y, Constant.TextureCoordinate.OVERLAY_WIDTH, Constant.TextureCoordinate.OVERLAY_HEIGHT, Constant.TextureCoordinate.OVERLAY_TEX_WIDTH, Constant.TextureCoordinate.OVERLAY_TEX_HEIGHT);
        drawProgressTexture(matrices, x, (y + Constant.TextureCoordinate.OVERLAY_HEIGHT - (Constant.TextureCoordinate.OVERLAY_HEIGHT * scale)), 0, Constant.TextureCoordinate.OXYGEN_X, Constant.TextureCoordinate.OXYGEN_Y, Constant.TextureCoordinate.OVERLAY_WIDTH, Constant.TextureCoordinate.OVERLAY_HEIGHT * scale, Constant.TextureCoordinate.OVERLAY_TEX_WIDTH, Constant.TextureCoordinate.OVERLAY_TEX_HEIGHT);
    }

    public static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    public static void drawProgressTexture(PoseStack matrices, float x, float y, float u, float v, float width, float height) {
        DrawableUtil.drawProgressTexture(matrices, x, y, 0, u, v, width, height, 256, 256);
    }

    public static void drawProgressTexture(PoseStack matrices, float x, float y, float z, float u, float v, float width, float height, int textureWidth, int textureHeight) {
        DrawableUtil.drawProgressTexture(matrices, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
    }

    public static void drawProgressTexture(PoseStack matrices, float x, float y, float z, float u, float v, float width, float height) {
        DrawableUtil.drawProgressTexture(matrices, x, x + width, y, y + height, z, width, height, u, v, 256, 256);
    }

    public static void drawProgressTexture(PoseStack matrices, float x0, float x1, float y0, float y1, float z, float regionWidth, float regionHeight, float u, float v, float textureWidth, float textureHeight) {
        DrawableUtil.drawTexturedQuad_F(matrices.last().pose(), x0, x1, y0, y1, z, (u + 0.0F) / textureWidth, (u + regionWidth) / textureWidth, (v) / textureHeight, (v + regionHeight) / textureHeight);
    }

    public static void drawTexturedQuad_F(Matrix4f matrices, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrices, x0, y1, z).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrices, x1, y1, z).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrices, x1, y0, z).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrices, x0, y0, z).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void drawTextureColor(PoseStack matrices, int x, int y, int z, float u, float v, int width, int height, int textureHeight, int textureWidth, int red, int green, int blue, int alpha) {
        drawTextureColor(matrices, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight, red, green, blue, alpha);
    }

    public static void drawTextureColor(PoseStack matrices, int x0, int x1, int y0, int y1, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight, int red, int green, int blue, int alpha) {
        drawTexturedQuadColor(matrices.last().pose(), x0, x1, y0, y1, z, (u + 0.0F) / (float) textureWidth, (u + (float) regionWidth) / (float) textureWidth, (v + 0.0F) / (float) textureHeight, (v + (float) regionHeight) / (float) textureHeight, red, green, blue, alpha);
    }

    public static void drawTexturedQuadColor(Matrix4f matrices, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, int red, int green, int blue, int alpha) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferBuilder.vertex(matrices, (float) x0, (float) y1, (float) z).color(red, green, blue, alpha).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrices, (float) x1, (float) y1, (float) z).color(red, green, blue, alpha).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrices, (float) x1, (float) y0, (float) z).color(red, green, blue, alpha).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrices, (float) x0, (float) y0, (float) z).color(red, green, blue, alpha).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

}
