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

package dev.galacticraft.machinelib.api.multiblock.components;

import dev.galacticraft.machinelib.api.machine.configuration.AccessLevel;
import dev.galacticraft.machinelib.api.machine.configuration.SecuritySettings;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponent;
import dev.galacticraft.machinelib.api.multiblock.MultiblockComponentContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Persistent security component for a formed multiblock machine.
 *
 * <p>This component mirrors the security behaviour used by normal MachineLib
 * machines. It stores an owner UUID and access level, and exposes a
 * {@link SecuritySettings} object so future multiblock menus can use the same
 * access-checking model as block-entity machines.</p>
 */
public final class MultiblockSecurityComponent implements MultiblockComponent {

    private static final String SECURITY = "Security";

    private final PersistentSecuritySettings security =
            new PersistentSecuritySettings();

    private MultiblockComponentContext context;

    /**
     * Gets the security settings for this multiblock.
     *
     * @return security settings
     */
    public SecuritySettings security() {
        return this.security;
    }

    /**
     * Attempts to claim ownership for a player.
     *
     * <p>This matches normal machine behaviour: if the machine has no owner yet,
     * the first player to interact with it becomes the owner.</p>
     *
     * @param player player to claim ownership for
     */
    public void tryClaim(final Player player) {
        this.security.tryUpdate(player.getUUID());
    }

    /**
     * Checks whether a player may access this multiblock.
     *
     * @param player player to check
     * @return {@code true} if access is allowed
     */
    public boolean hasAccess(final Player player) {
        return this.security.hasAccess(player);
    }

    /**
     * Loads persisted security state.
     *
     * @param context component context
     * @param tag saved component tag
     */
    @Override
    public void load(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        this.context = context;

        if (tag.contains(SECURITY, Tag.TAG_COMPOUND)) {
            this.security.readTag(tag.getCompound(SECURITY));
        }
    }

    /**
     * Saves persisted security state.
     *
     * @param context component context
     * @param tag component tag to write into
     */
    @Override
    public void save(
            final MultiblockComponentContext context,
            final CompoundTag tag
    ) {
        tag.put(
                SECURITY,
                this.security.createTag()
        );
    }

    /**
     * Stores the active component context.
     *
     * @param context component context
     */
    @Override
    public void onFormed(final MultiblockComponentContext context) {
        this.context = context;
    }

    /**
     * Security settings implementation that marks the multiblock as changed when
     * owner or access level changes.
     */
    private final class PersistentSecuritySettings extends SecuritySettings {

        /**
         * Claims ownership if this multiblock does not already have an owner.
         *
         * @param uuid player UUID
         */
        @Override
        public void tryUpdate(final UUID uuid) {
            if (this.owner == null) {
                this.owner = uuid;
                MultiblockSecurityComponent.this.setChanged();
            }
        }

        /**
         * Updates the access level.
         *
         * @param accessLevel new access level
         */
        @Override
        public void setAccessLevel(final AccessLevel accessLevel) {
            if (this.accessLevel != accessLevel) {
                this.accessLevel = accessLevel;
                MultiblockSecurityComponent.this.setChanged();
            }
        }

    }

    private void setChanged() {
        if (this.context != null) {
            this.context.setChanged();
        }
    }

}