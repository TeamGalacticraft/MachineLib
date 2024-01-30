package dev.galacticraft.machinelib.client.impl.compat.rei;

import dev.architectury.event.CompoundEventResult;
import dev.galacticraft.machinelib.client.api.screen.MachineScreen;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screens.Screen;

public class MachineFocusedStackProvider implements FocusedStackProvider {
    public static final MachineFocusedStackProvider INSTANCE = new MachineFocusedStackProvider();

    private MachineFocusedStackProvider() {
    }

    @Override
    public CompoundEventResult<EntryStack<?>> provide(Screen screen, Point mouse) {
        if (screen instanceof MachineScreen<?,?> machineScreen) {
            if (machineScreen.hoveredTank != null && !machineScreen.hoveredTank.isEmpty()) {
                return CompoundEventResult.interruptTrue(EntryStacks.of(machineScreen.hoveredTank.getFluid(), machineScreen.hoveredTank.getAmount()));
            }
        }
        return CompoundEventResult.pass();
    }

    @Override
    public double getPriority() {
        return -5.0;
    }
}
