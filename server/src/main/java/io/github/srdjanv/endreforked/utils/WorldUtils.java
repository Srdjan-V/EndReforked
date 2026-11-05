package io.github.srdjanv.endreforked.utils;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

// Copied cofh core
public class WorldUtils {

    private WorldUtils() {}

    public static boolean isClientWorld(World world) {
        return world.isRemote;
    }

    public static WorldClient castToClientWorld(World world) {
        return (WorldClient) world;
    }

    public static boolean isServerWorld(World world) {
        return !world.isRemote;
    }

    public static WorldServer castToServerWorld(World world) {
        return (WorldServer) world;
    }

    public static boolean isSinglePlayerServer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance() != null;
    }

    public static boolean isMultiPlayerServer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance() == null;
    }

    public static void sendItemUsePacket(World world, BlockPos pos, EnumFacing hitSide, EnumHand hand, float hitX,
                                         float hitY, float hitZ) {
        if (!isServerWorld(world)) {
            NetHandlerPlayClient netClientHandler = (NetHandlerPlayClient) FMLClientHandler.instance()
                    .getClientPlayHandler();
            netClientHandler.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, hitSide, hand, hitX, hitY, hitZ));
        }
    }
}
