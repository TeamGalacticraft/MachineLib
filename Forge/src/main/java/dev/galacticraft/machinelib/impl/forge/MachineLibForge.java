package dev.galacticraft.machinelib.impl.forge;

import dev.galacticraft.machinelib.api.machine.MachineStatuses;
import dev.galacticraft.machinelib.impl.Constant;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.galacticraft.machinelib.api.gas.Gases.*;

@Mod(Constant.MOD_ID)
public class MachineLibForge {
    public MachineLibForge() {
        MachineStatuses.init();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(MachineLibForge::registerFluids);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, MachineLibForge::registerStorages);
    }

    public static void registerStorages(AttachCapabilitiesEvent<BlockEntity> event) {
        event.addCapability(Constant.id("energy"), new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                return null;
            }
        });
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
