package dev.galacticraft.machinelib.impl.forge;

import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import static dev.galacticraft.machinelib.api.gas.Gases.*;

@Mod(Constant.MOD_ID)
public class MachineLibForge {
    public MachineLibForge() {
        MachineStatuses.init();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(MachineLibForge::registerFluids);
    }

    public static void registerFluids(RegisterEvent event) {
        if (event.getRegistryKey() == ForgeRegistries.Keys.FLUIDS) {
            event.register(ForgeRegistries.Keys.FLUIDS, HYDROGEN_ID, () -> HYDROGEN);
            event.register(ForgeRegistries.Keys.FLUIDS, NITROGEN_ID, () -> NITROGEN);
            event.register(ForgeRegistries.Keys.FLUIDS, OXYGEN_ID, () -> OXYGEN);
            event.register(ForgeRegistries.Keys.FLUIDS, CARBON_DIOXIDE_ID, () -> CARBON_DIOXIDE);
            event.register(ForgeRegistries.Keys.FLUIDS, WATER_VAPOR_ID, () -> WATER_VAPOR);
            event.register(ForgeRegistries.Keys.FLUIDS, METHANE_ID, () -> METHANE);
            event.register(ForgeRegistries.Keys.FLUIDS, HELIUM_ID, () -> HELIUM);
            event.register(ForgeRegistries.Keys.FLUIDS, ARGON_ID, () -> ARGON);
            event.register(ForgeRegistries.Keys.FLUIDS, NEON_ID, () -> NEON);
            event.register(ForgeRegistries.Keys.FLUIDS, KRYPTON_ID, () -> KRYPTON);
            event.register(ForgeRegistries.Keys.FLUIDS, NITROUS_OXIDE_ID, () -> NITROUS_OXIDE);
            event.register(ForgeRegistries.Keys.FLUIDS, CARBON_MONOXIDE_ID, () -> CARBON_MONOXIDE);
            event.register(ForgeRegistries.Keys.FLUIDS, XENON_ID, () -> XENON);
            event.register(ForgeRegistries.Keys.FLUIDS, OZONE_ID, () -> OZONE);
            event.register(ForgeRegistries.Keys.FLUIDS, NITROUS_DIOXIDE_ID, () -> NITROUS_DIOXIDE);
            event.register(ForgeRegistries.Keys.FLUIDS, IODINE_ID, () -> IODINE);
        }
    }
}
