package dev.galacticraft.machinelib.api.gametest.annotation.timing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InexactTime {
    int setup() default 0;
    int maxTime();
}
