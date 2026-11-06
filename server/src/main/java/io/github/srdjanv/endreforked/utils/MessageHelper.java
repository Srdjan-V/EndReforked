package io.github.srdjanv.endreforked.utils;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public final class MessageHelper {

    public static void LordGroup(World world, String text) {
        customGroup(world, text);
    }

    public static void customGroup(World world, String text) {
        List<EntityPlayer> players = world.playerEntities;
        for (EntityPlayer player : players)
            player.sendMessage(new TextComponentString(text));
    }

    private MessageHelper() {}
}
