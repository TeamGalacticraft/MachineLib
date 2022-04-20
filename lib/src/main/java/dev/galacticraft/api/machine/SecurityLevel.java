package dev.galacticraft.api.machine;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.StringIdentifiable;

/**
 * Represents the level of protection a machine has from other players.
 */
public enum SecurityLevel implements StringIdentifiable {
    /**
     * All players can use this machine.
     */
    PUBLIC(new TranslatableText("ui.galacticraft.machine.security.accessibility.public")),
    /**
     * Only team members can use this machine.
     */
    TEAM(new TranslatableText("ui.galacticraft.machine.security.accessibility.team")),
    /**
     * Only the owner can use this machine.
     */
    PRIVATE(new TranslatableText("ui.galacticraft.machine.security.accessibility.private"));

    /**
     * The name of the security level.
     */
    private final Text name;

    SecurityLevel(TranslatableText name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.toString();
    }

    /**
     * Returns the name of the security level.
     *
     * @return The name of the security level.
     */
    public Text getName() {
        return this.name;
    }
}
