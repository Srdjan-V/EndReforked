package io.github.srdjanv.endreforked.utils;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class PlayerUtils {

    public static final List<Function<EntityPlayer, Stream<ItemStack>>> PLAYER_INVENTORIES = new ObjectArrayList<>();
    static {
        PLAYER_INVENTORIES.add(player -> IntStream.range(0, player.inventory.getSizeInventory())
                .mapToObj(player.inventory::getStackInSlot));
    }

    public static List<ItemStack> getAllPlayerItems(EntityPlayer player) {
        return PLAYER_INVENTORIES.stream().flatMap(t -> t.apply(player)).filter(Objects::nonNull)
                .collect(Collectors.toCollection(ObjectArrayList::new));
    }

    public static void syncInventory(EntityPlayerMP player) {
        player.inventory.markDirty();
        player.server.getPlayerList().syncPlayerInventory(player);
    }
}
