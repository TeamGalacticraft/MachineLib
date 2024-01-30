package dev.galacticraft.machinelib.client.impl.compat.rei;

import dev.galacticraft.machinelib.client.api.screen.MachineScreen;
import dev.galacticraft.machinelib.impl.Constant;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConfigPanelExclusionZonesProvider implements ExclusionZonesProvider<MachineScreen<?, ?>> {
    public static final ConfigPanelExclusionZonesProvider INSTANCE = new ConfigPanelExclusionZonesProvider();

    private ConfigPanelExclusionZonesProvider() {
    }

    @Override
    public Collection<Rectangle> provide(MachineScreen<?, ?> screen) {
        List<Rectangle> areas = new ArrayList<>();
        if (MachineScreen.Tab.STATS.isOpen() || MachineScreen.Tab.SECURITY.isOpen()) {
            areas.add(new Rectangle(screen.getX() + screen.getImageWidth(), screen.getY() + (MachineScreen.Tab.STATS.isOpen() ? 0 : Constant.TextureCoordinate.TAB_HEIGHT), Constant.TextureCoordinate.PANEL_WIDTH, Constant.TextureCoordinate.PANEL_HEIGHT));
            areas.add(new Rectangle(screen.getX() + screen.getImageWidth(), screen.getY() + Constant.TextureCoordinate.TAB_HEIGHT, Constant.TextureCoordinate.TAB_WIDTH, Constant.TextureCoordinate.PANEL_HEIGHT));
        }
        areas.add(new Rectangle(screen.getX() + screen.getImageWidth(), screen.getY(), Constant.TextureCoordinate.TAB_WIDTH, Constant.TextureCoordinate.TAB_HEIGHT * 2));
        return areas;
    }
}
