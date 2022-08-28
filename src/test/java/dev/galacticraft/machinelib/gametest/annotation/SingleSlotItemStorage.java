package dev.galacticraft.machinelib.gametest.annotation;

import dev.galacticraft.api.machine.storage.io.ResourceFlow;
import dev.galacticraft.machinelib.gametest.misc.ItemType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SingleSlotItemStorage {
    ItemType block() default ItemType.NONE;
    boolean blockNbt() default false;

    ItemType type() default ItemType.STACK_64;
    int amount() default 0;

    int maxCount() default 64;
    ResourceFlow flow() default ResourceFlow.BOTH;
}
