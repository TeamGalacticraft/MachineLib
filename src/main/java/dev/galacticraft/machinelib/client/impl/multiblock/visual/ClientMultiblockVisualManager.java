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

package dev.galacticraft.machinelib.client.impl.multiblock.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.galacticraft.machinelib.api.multiblock.MultiblockDefinition;
import dev.galacticraft.machinelib.api.multiblock.visual.MultiblockVisual;
import dev.galacticraft.machinelib.api.multiblock.visual.MultiblockVisualContext;
import dev.galacticraft.machinelib.impl.multiblock.MachineLibMultiblocks;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side lifecycle manager for formed multiblock visuals.
 *
 * <p>The manager mirrors the client formed multiblock index. When a formed
 * multiblock is synced to the client, this manager looks up the definition's
 * visual factory and creates a visual instance. When the formed multiblock is
 * removed, the visual is closed and discarded.</p>
 *
 * <p>This manager renders one visual per formed multiblock instance. It does not
 * render through block entities, which avoids duplicated rendering and allows
 * future large-machine, animated, instanced, and translucent rendering systems to
 * work at the formed-machine level.</p>
 */
public final class ClientMultiblockVisualManager {

    private static final Map<UUID, ClientMultiblockVisualInstance> VISUALS =
            new HashMap<>();

    private ClientMultiblockVisualManager() {

    }

    /**
     * Adds or replaces the visual for a synced formed multiblock.
     *
     * <p>If the definition has no visual factory, no visual is created. If a
     * visual already exists for the supplied instance id, it is closed before the
     * new one is stored.</p>
     *
     * @param instanceId formed multiblock instance id
     * @param definitionId registered multiblock definition id
     * @param origin world-space origin
     * @param orientation formed multiblock orientation
     * @param partPositions world-space occupied part positions
     */
    public static void addMachine(
            final UUID instanceId,
            final ResourceLocation definitionId,
            final BlockPos origin,
            final dev.galacticraft.machinelib.api.multiblock.MultiblockOrientation orientation,
            final List<BlockPos> partPositions
    ) {
        removeMachine(instanceId);

        final MultiblockDefinition definition =
                MachineLibMultiblocks.getDefinition(definitionId);

        if (definition == null || definition.visualFactory() == null) {
            return;
        }

        final MultiblockVisualContext context = new MultiblockVisualContext(
                instanceId,
                definitionId,
                origin,
                orientation,
                partPositions,
                definition.pattern().sizeX(),
                definition.pattern().sizeY(),
                definition.pattern().sizeZ()
        );

        final MultiblockVisual visual = definition.visualFactory().create(context);

        if (visual == null) {
            return;
        }

        VISUALS.put(
                instanceId,
                new ClientMultiblockVisualInstance(
                        context,
                        visual
                )
        );
    }

    /**
     * Removes and closes one formed multiblock visual.
     *
     * @param instanceId formed multiblock instance id
     */
    public static void removeMachine(final UUID instanceId) {
        final ClientMultiblockVisualInstance instance = VISUALS.remove(instanceId);

        if (instance != null) {
            instance.close();
        }
    }

    /**
     * Removes and closes every known formed multiblock visual.
     */
    public static void clear() {
        for (final ClientMultiblockVisualInstance instance : VISUALS.values()) {
            instance.close();
        }

        VISUALS.clear();
    }

    /**
     * Ticks all active multiblock visuals.
     */
    public static void tick() {
        for (final ClientMultiblockVisualInstance instance : VISUALS.values()) {
            instance.tick();
        }
    }

    /**
     * Renders all active formed multiblock visuals.
     *
     * <p>This method is intended to be registered with a Fabric world render
     * event, such as {@code WorldRenderEvents.AFTER_TRANSLUCENT}. Static debug
     * and frame visuals can render directly here. Future translucent model support
     * can either render sorted quads here or delegate into Glasswork when that
     * integration is ready.</p>
     *
     * @param renderContext Fabric world render context
     */
    public static void render(final WorldRenderContext renderContext) {
        final Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        final PoseStack matrices = renderContext.matrixStack();

        if (matrices == null) {
            return;
        }

        final Vec3 cameraPos = renderContext.camera().getPosition();
        final Frustum frustum = renderContext.frustum();
        final float tickDelta = renderContext.tickCounter().getRealtimeDeltaTicks();

        for (final ClientMultiblockVisualInstance instance : VISUALS.values()) {
            if (!(instance.visual() instanceof ClientFormedMultiblockVisual renderable)) {
                continue;
            }

            if (!renderable.shouldRender(
                    instance.context(),
                    instance.bounds(),
                    frustum,
                    cameraPos
            )) {
                continue;
            }

            renderable.render(
                    instance.context(),
                    instance.bounds(),
                    renderContext,
                    matrices,
                    cameraPos,
                    tickDelta
            );
        }
    }

    /**
     * Gets all currently active visual instances.
     *
     * <p>This is primarily intended for debugging and future developer tooling.</p>
     *
     * @return active visual instances
     */
    public static Collection<ClientMultiblockVisualInstance> visuals() {
        return List.copyOf(VISUALS.values());
    }

}