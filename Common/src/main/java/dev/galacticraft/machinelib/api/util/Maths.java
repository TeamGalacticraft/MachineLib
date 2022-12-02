package dev.galacticraft.machinelib.api.util;

public final class Maths {
    public static int floorLong(long l) {
        return l > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) l;
    }
}
