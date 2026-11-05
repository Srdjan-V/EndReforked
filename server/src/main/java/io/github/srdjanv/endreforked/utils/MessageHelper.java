package io.github.srdjanv.endreforked.utils;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;

public final class MessageHelper {

    public static void LordGroup(World world, String text) {
        customGroup(world, text);
    }

    public static void LordText(String text) {
        customText(I18n.format("tile.ender_lord.name"), text);
    }

    public static void customText(String name, String text) {
        FMLClientHandler.instance().getClient().ingameGUI.getChatGUI()
                .printChatMessage(new TextComponentString(name + ": " + text));
    }

    public static void customGroup(World world, String text) {
        List<EntityPlayer> players = world.playerEntities;
        for (EntityPlayer player : players)
            player.sendMessage(new TextComponentString(text));
    }

    private MessageHelper() {}
}
