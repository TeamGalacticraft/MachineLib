package dev.galacticraft.machinelib.impl.schematic;

import dev.galacticraft.machinelib.api.block.SimpleMachineBlock;
import dev.galacticraft.machinelib.api.block.entity.MachineBlockEntity;
import dev.galacticraft.machinelib.api.menu.SynchronizedMenuType;
import dev.galacticraft.machinelib.impl.Constant;
import dev.galacticraft.machinelib.impl.schematic.block.entity.SchematicProjectorBlockEntity;
import dev.galacticraft.machinelib.impl.schematic.block.entity.SchematicWorkbenchBlockEntity;
import dev.galacticraft.machinelib.impl.schematic.item.SchematicPaperItem;
import dev.galacticraft.machinelib.impl.schematic.menu.SchematicWorkbenchMenu;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registers MachineLib's optional schematic helper content.
 *
 * <p>The schematic paper, schematic workbench, and schematic projector only exist when at least
 * one MachineLib multiblock definition has been registered. This prevents MachineLib from adding
 * unused content when it is installed purely as a backend library.</p>
 */
public final class MachineLibSchematicContent {
    public static final ResourceLocation SCHEMATIC_PAPER_ID = Constant.id("schematic_paper");
    public static final ResourceLocation SCHEMATIC_WORKBENCH_ID = Constant.id("schematic_workbench");
    public static final ResourceLocation SCHEMATIC_PROJECTOR_ID = Constant.id("schematic_projector");

    private static boolean registered;

    private static @Nullable Item schematicPaper;
    private static @Nullable Block schematicWorkbench;
    private static @Nullable Block schematicProjector;
    private static @Nullable BlockEntityType<SchematicWorkbenchBlockEntity> schematicWorkbenchBlockEntity;
    private static @Nullable BlockEntityType<SchematicProjectorBlockEntity> schematicProjectorBlockEntity;

    private static @Nullable MenuType<SchematicWorkbenchMenu> schematicWorkbenchMenu;

    private MachineLibSchematicContent() {
    }

    /**
     * Registers schematic content if the current runtime has at least one registered multiblock.
     *
     * @param hasRegisteredMultiblocks whether at least one multiblock definition exists
     */
    public static void registerIfNeeded(boolean hasRegisteredMultiblocks) {
        if (registered || !hasRegisteredMultiblocks) {
            return;
        }

        registered = true;

        schematicPaper = Registry.register(
                BuiltInRegistries.ITEM,
                SCHEMATIC_PAPER_ID,
                new SchematicPaperItem(new Item.Properties().stacksTo(1))
        );

        schematicWorkbench = Registry.register(
                BuiltInRegistries.BLOCK,
                SCHEMATIC_WORKBENCH_ID,
                new SimpleMachineBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.METAL)
                                .sound(SoundType.METAL)
                                .strength(3.0F, 6.0F),
                        SCHEMATIC_WORKBENCH_ID
                )
        );

        Registry.register(
                BuiltInRegistries.ITEM,
                SCHEMATIC_WORKBENCH_ID,
                new BlockItem(schematicWorkbench, new Item.Properties())
        );

        schematicProjector = Registry.register(
                BuiltInRegistries.BLOCK,
                SCHEMATIC_PROJECTOR_ID,
                new SimpleMachineBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.METAL)
                                .sound(SoundType.METAL)
                                .strength(3.0F, 6.0F),
                        SCHEMATIC_PROJECTOR_ID
                )
        );

        Registry.register(
                BuiltInRegistries.ITEM,
                SCHEMATIC_PROJECTOR_ID,
                new BlockItem(schematicProjector, new Item.Properties())
        );

        schematicWorkbenchBlockEntity = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                SCHEMATIC_WORKBENCH_ID,
                BlockEntityType.Builder.of(SchematicWorkbenchBlockEntity::new, schematicWorkbench).build(null)
        );

        schematicProjectorBlockEntity = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                SCHEMATIC_PROJECTOR_ID,
                BlockEntityType.Builder.of(SchematicProjectorBlockEntity::new, schematicProjector).build(null)
        );

        MachineBlockEntity.registerProviders(schematicWorkbenchBlockEntity, schematicProjectorBlockEntity);

        schematicWorkbenchMenu = Registry.register(
                BuiltInRegistries.MENU,
                SCHEMATIC_WORKBENCH_ID,
                SynchronizedMenuType.create(SchematicWorkbenchMenu::new, 48, 140)
        );

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(schematicWorkbench);
            entries.accept(schematicProjector);
            entries.accept(schematicPaper);
        });
    }

    /**
     * {@return schematic workbench menu type}
     */
    public static @NotNull MenuType<SchematicWorkbenchMenu> schematicWorkbenchMenu() {
        if (schematicWorkbenchMenu == null) {
            throw new IllegalStateException("Schematic workbench menu requested before registration.");
        }

        return schematicWorkbenchMenu;
    }

    /**
     * {@return whether schematic content has been registered}
     */
    @Contract(pure = true)
    public static boolean isRegistered() {
        return registered;
    }

    /**
     * {@return the schematic paper item}
     */
    public static @NotNull Item schematicPaper() {
        if (schematicPaper == null) {
            throw new IllegalStateException("Schematic paper requested before registration.");
        }

        return schematicPaper;
    }

    /**
     * {@return the schematic workbench block}
     */
    public static @NotNull Block schematicWorkbench() {
        if (schematicWorkbench == null) {
            throw new IllegalStateException("Schematic workbench requested before registration.");
        }

        return schematicWorkbench;
    }

    /**
     * {@return the schematic projector block}
     */
    public static @NotNull Block schematicProjector() {
        if (schematicProjector == null) {
            throw new IllegalStateException("Schematic projector requested before registration.");
        }

        return schematicProjector;
    }

    /**
     * {@return the schematic workbench block entity type}
     */
    public static @NotNull BlockEntityType<SchematicWorkbenchBlockEntity> schematicWorkbenchBlockEntity() {
        if (schematicWorkbenchBlockEntity == null) {
            throw new IllegalStateException("Schematic workbench block entity type requested before registration.");
        }

        return schematicWorkbenchBlockEntity;
    }

    /**
     * {@return the schematic projector block entity type}
     */
    public static @NotNull BlockEntityType<SchematicProjectorBlockEntity> schematicProjectorBlockEntity() {
        if (schematicProjectorBlockEntity == null) {
            throw new IllegalStateException("Schematic projector block entity type requested before registration.");
        }

        return schematicProjectorBlockEntity;
    }
}