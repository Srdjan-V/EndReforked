package io.github.srdjanv.endreforked.common.village;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.utils.ItemStackUtils;

/**
 * @author BluSunrize - 23.07.2016
 */
public class EndVillagerHandler {

    private static final VillagerRegistry VILLAGER_REGISTRY = VillagerRegistry.instance();
    public static VillagerRegistry.VillagerProfession PROF_EXPLORER;

    public static void initVillagerHouse() {
        VILLAGER_REGISTRY.registerVillageCreationHandler(new EndVillagerHouse.VillageManager());
        MapGenStructureIO.registerStructureComponent(EndVillagerHouse.class, Tags.MOD_PREFIX + "ExplorerHouse");
    }

    public static void initVillagerTrades() {
        PROF_EXPLORER = new VillagerRegistry.VillagerProfession(Tags.MOD_PREFIX + "explorer",
                "endreborn:textures/models/villager_explorer.png",
                "endreborn:textures/models/villager_explorer_zombie.png");
        ForgeRegistries.VILLAGER_PROFESSIONS.register(PROF_EXPLORER);

        VillagerRegistry.VillagerCareer overworld_explorer = new VillagerRegistry.VillagerCareer(PROF_EXPLORER,
                Tags.MODID + ".overworld_explorer");
        overworld_explorer.addTrade(1,
                new ItemStackForEmerald(
                        new ItemStack(ModItems.HAMMER_IRON.get(), 1, 0),
                        new EntityVillager.PriceInfo(1, 1)),
                new ItemStackForEmerald(
                        new ItemStack(ModItems.TUNGSTEN_INGOT.get(), 1, 0),
                        new EntityVillager.PriceInfo(-4, -1)),
                new EmeraldForItemStack(
                        new ItemStack(Blocks.IRON_ORE, 1, 0),
                        new EntityVillager.PriceInfo(1, 3)));

        VillagerRegistry.VillagerCareer nether_explorer = new VillagerRegistry.VillagerCareer(PROF_EXPLORER,
                Tags.MODID + ".nether_explorer");
        nether_explorer.addTrade(1,
                new EmeraldForItemStack(
                        new ItemStack(Items.GOLD_NUGGET, 1, 0),
                        new EntityVillager.PriceInfo(8, 16)),
                new ItemStackForEmerald(
                        new ItemStack(Blocks.NETHERRACK, 1, 0),
                        new EntityVillager.PriceInfo(-4, -2)),
                new ItemStackForEmerald(
                        new ItemStack(Items.NETHER_WART, 1, 0),
                        new EntityVillager.PriceInfo(-1, -1)));

        VillagerRegistry.VillagerCareer end_explorer = new VillagerRegistry.VillagerCareer(PROF_EXPLORER,
                Tags.MODID + ".end_explorer");
        end_explorer.addTrade(1,
                new EmeraldForItemStack(
                        new ItemStack(Items.ENDER_PEARL, 3, 0),
                        new EntityVillager.PriceInfo(6, 8)),
                new ItemStackForEmerald(
                        new ItemStack(ModItems.END_ESSENCE.get(), 3, 0),
                        new EntityVillager.PriceInfo(-2, -1)),
                new ItemStackForEmerald(
                        new ItemStack(Blocks.END_STONE, 3, 0),
                        new EntityVillager.PriceInfo(-4, -2)),
                new ItemStackForEmerald(
                        new ItemStack(ModItems.CHORUS_SOUP.get(), 3, 0),
                        new EntityVillager.PriceInfo(-2, -1)));
    }

    private static class EmeraldForItemStack implements EntityVillager.ITradeList {

        public ItemStack buyingItem;
        public EntityVillager.PriceInfo buyAmounts;

        public EmeraldForItemStack(@Nonnull ItemStack item, @Nonnull EntityVillager.PriceInfo buyAmounts) {
            this.buyingItem = item;
            this.buyAmounts = buyAmounts;
        }

        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            recipeList.add(new MerchantRecipe(
                    ItemStackUtils.copyStackWithAmount(this.buyingItem, this.buyAmounts.getPrice(random)),
                    Items.EMERALD));
        }
    }

    private static class ItemStackForEmerald implements EntityVillager.ITradeList {

        public ItemStack sellingItem;
        public EntityVillager.PriceInfo priceInfo;

        @SuppressWarnings("unused")
        public ItemStackForEmerald(Item par1Item, EntityVillager.PriceInfo priceInfo) {
            this.sellingItem = new ItemStack(par1Item);
            this.priceInfo = priceInfo;
        }

        public ItemStackForEmerald(ItemStack stack, EntityVillager.PriceInfo priceInfo) {
            this.sellingItem = stack;
            this.priceInfo = priceInfo;
        }

        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            int i = 1;
            if (this.priceInfo != null)
                i = this.priceInfo.getPrice(random);
            ItemStack itemstack;
            ItemStack itemstack1;
            if (i < 0) {
                itemstack = new ItemStack(Items.EMERALD);
                itemstack1 = ItemStackUtils.copyStackWithAmount(sellingItem, -i);
            } else {
                itemstack = new ItemStack(Items.EMERALD, i, 0);
                itemstack1 = ItemStackUtils.copyStackWithAmount(sellingItem, 1);
            }
            recipeList.add(new MerchantRecipe(itemstack, itemstack1));
        }
    }
}
