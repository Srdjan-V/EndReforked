package io.github.srdjanv.endreforked.utils;

import net.minecraft.world.World;

// Copied cofh core
public class WorldUtils {

    private WorldUtils() {}

    public static boolean isClientWorld(World world) {
        return world.isRemote;
    }
}
