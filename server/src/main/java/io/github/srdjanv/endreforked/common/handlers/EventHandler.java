package io.github.srdjanv.endreforked.common.handlers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.configs.Configs;

public final class EventHandler {

    @SubscribeEvent
    public static void onDragonTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if (living.world.isRemote || !(living instanceof EntityDragon dragon)) return;
        if (dragon.deathTicks < 150 || dragon.deathTicks % 10 != 0) return;

        for (int i = 0; i < 6; i++) {
            int x = dragon.world.rand.nextInt(256) - 128;
            int z = dragon.world.rand.nextInt(256) - 128;
            BlockPos pos = new BlockPos(x, dragon.world.getHeight(x, z) - 1, z);
            if (!dragon.world.isBlockLoaded(pos))
                continue;
            if (dragon.world.getBlockState(pos.down()).getBlock() != Blocks.END_STONE)
                continue;
            dragon.world.setBlockState(pos, ModBlocks.ENTROPY_CROP_BLOCK.get().getDefaultState());
        }
    }

    @SubscribeEvent
    public static void onPlayerPosition(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayerMP player && event.getEntityLiving().dimension == 1 &&
                Configs.SERVER_SIDE_CONFIGS.teleporterEnd && event.getEntityLiving().getPosition().getY() <= -6) {
            PlayerList playerList = player.getEntityWorld().getMinecraftServer().getPlayerList();

            event.setCanceled(true);
            playerList.changePlayerDimension(player, 0);
        }
    }

    /*
     * @SubscribeEvent
     * public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
     * if (event.getEntityPlayer().getEntityWorld().isRemote && event.getItemStack().getItem() == Items.STICK) {
     * GuiManager.openClientUI(event.getEntityPlayer(), createGUI());
     * }
     * }
     */

    /*
     * // TODO: 27/11/2023 remove
     * public static ModularScreen createGUI() {
     * ModularPanel panel = ModularPanel.defaultPanel("widgetTest");
     */
    /*
     * var page = new PagedWidget<>().size(160);
     * page.addPage(new BlockPatternWidget(
     * new IBlockState[][][]{{{Blocks.END_STONE.getDefaultState(), Blocks.END_STONE.getDefaultState()}}},
     * new ItemStack[][][]{{{new ItemStack(Blocks.END_STONE), new ItemStack(Blocks.END_STONE)}}}));
     *
     * page.addPage(new BlockPatternWidget(
     * new IBlockState[][][]{{{Blocks.SAND.getDefaultState(), Blocks.SAND.getDefaultState()}}},
     * new ItemStack[][][]{{{new ItemStack(Blocks.SAND), new ItemStack(Blocks.SAND)}}}));
     *
     * var controller = new PagedWidget.Controller();
     * page.controller(controller);
     * panel.child(page);
     * page.listenGuiAction((IGuiAction.MousePressed) id-> {
     * if (id == 0) {
     * controller.nextPage();
     * return true;
     * }
     * if (id == 1) {
     * controller.previousPage();
     * return true;
     * }
     * return false;
     * });
     *//*
       
       
       */
    /*
     * IBlockState[][] z1;
     *
     * {
     * IBlockState[] x1 = new IBlockState[]{Blocks.END_STONE.getDefaultState(), Blocks.END_STONE.getDefaultState()};
     * IBlockState[] x2 = new IBlockState[]{Blocks.COBBLESTONE.getDefaultState(),
     * Blocks.COBBLESTONE.getDefaultState()};
     * IBlockState[] x3 = new IBlockState[]{Blocks.SAND.getDefaultState(), Blocks.SAND.getDefaultState()};
     * z1 = new IBlockState[][]{x1, x2, x3};
     * }
     *
     * IBlockState[][] z2;
     * {
     *
     * IBlockState[] x1 = new IBlockState[]{Blocks.FURNACE.getDefaultState(), Blocks.FURNACE.getDefaultState()};
     * IBlockState[] x2 = new IBlockState[]{Blocks.FARMLAND.getDefaultState()};
     * IBlockState[] x3 = new IBlockState[]{Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState()};
     * z2 = new IBlockState[][]{x1, x2, x3};
     * }
     *//*
        * 
        * 
        * // panel.child(new BlockPatternWidget(new IBlockState[][][]{z1,z2}).size(160));
        */
    /*
     * panel.child(new BlockPatternWidget(Structure.builder()
     * .aisle("EEE", "EEE", "EEE")
     * .aisle("EEE", "EAE", "EEE")
     * .aisle("EEE", "EAE", "EEE")
     * .aisle("EEE", "EAE", "EEE")
     * .where('E', Blocks.COBBLESTONE.getDefaultState())
     * .where('A', Blocks.AIR.getDefaultState())
     * .build().getStructure()).size(160));
     *//*
        * 
        * 
        * return new ModularScreen(panel);
        * }
        */

    private EventHandler() {}
}
