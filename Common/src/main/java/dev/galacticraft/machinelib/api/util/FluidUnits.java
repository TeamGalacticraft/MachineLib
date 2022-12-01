package dev.galacticraft.machinelib.api.util;

import dev.galacticraft.machinelib.impl.platform.Services;

public interface FluidUnits {
    long _getBucketVolume();
    long _getIngotVolume();
    long _getNuggetVolume();
    long _getBottleVolume();
    long _getBucketVolume(long buckets);
    long _getIngotVolume(long ingots);
    long _getNuggetVolume(long nuggets);
    long _getBottleVolume(long bottles);

    static long getBucketVolume() {
        return Services.FLUID_UNITS._getBucketVolume();
    }

    static long getIngotVolume() {
        return Services.FLUID_UNITS._getIngotVolume();
    }

    static long getNuggetVolume() {
        return Services.FLUID_UNITS._getNuggetVolume();
    }

    static long getBottleVolume() {
        return Services.FLUID_UNITS._getBottleVolume();
    }

    static long getBucketVolume(long buckets) {
        return Services.FLUID_UNITS._getBucketVolume(buckets);
    }

    static long getIngotVolume(long ingots) {
        return Services.FLUID_UNITS._getIngotVolume(ingots);
    }

    static long getNuggetVolume(long nuggets) {
        return Services.FLUID_UNITS._getNuggetVolume(nuggets);
    }

    static long getBottleVolume(long bottles) {
        return Services.FLUID_UNITS._getBottleVolume(bottles);
    }
}
