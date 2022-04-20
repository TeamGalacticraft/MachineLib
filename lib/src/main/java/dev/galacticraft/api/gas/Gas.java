package dev.galacticraft.api.gas;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public interface Gas {
    @NotNull Text getName();

    @NotNull String getSymbol();
}
