package io.github.srdjanv.endreforked.common.village

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.utils.ItemStackUtils
import net.minecraft.entity.IMerchant
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.passive.EntityVillager.ITradeList
import net.minecraft.entity.passive.EntityVillager.PriceInfo
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.village.MerchantRecipe
import net.minecraft.village.MerchantRecipeList
import net.minecraft.world.gen.structure.MapGenStructureIO
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.fml.common.registry.VillagerRegistry
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerCareer
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession
import java.util.Random

/** @author BluSunrize - 23.07.2016 */
object EndVillagerHandler {
    lateinit var profExplorer: VillagerProfession
        private set

    @JvmStatic
    fun initVillagerHouse() {
        VillagerRegistry.instance().registerVillageCreationHandler(EndVillagerHouse.VillageManager())
        MapGenStructureIO.registerStructureComponent(EndVillagerHouse::class.java, Tags.MOD_PREFIX + "ExplorerHouse")
    }

    @JvmStatic
    fun initVillagerTrades() {
        profExplorer = VillagerProfession(
            Tags.MOD_PREFIX + "explorer",
            "endreborn:textures/models/villager_explorer.png",
            "endreborn:textures/models/villager_explorer_zombie.png",
        )
        ForgeRegistries.VILLAGER_PROFESSIONS.register(profExplorer)

        VillagerCareer(profExplorer, Tags.MODID + ".overworld_explorer").addTrade(
            1,
            sells(ItemStack(ModItems.TUNGSTEN_INGOT.get(), 1, 0), PriceInfo(-4, -1)),
            buys(ItemStack(Blocks.IRON_ORE, 1, 0), PriceInfo(1, 3)),
        )
        VillagerCareer(profExplorer, Tags.MODID + ".nether_explorer").addTrade(
            1,
            buys(ItemStack(Items.GOLD_NUGGET, 1, 0), PriceInfo(8, 16)),
            sells(ItemStack(Blocks.NETHERRACK, 1, 0), PriceInfo(-4, -2)),
            sells(ItemStack(Items.NETHER_WART, 1, 0), PriceInfo(-1, -1)),
        )
        VillagerCareer(profExplorer, Tags.MODID + ".end_explorer").addTrade(
            1,
            buys(ItemStack(Items.ENDER_PEARL, 3, 0), PriceInfo(6, 8)),
            sells(ItemStack(ModItems.END_ESSENCE.get(), 3, 0), PriceInfo(-2, -1)),
            sells(ItemStack(Blocks.END_STONE, 3, 0), PriceInfo(-4, -2)),
            sells(ItemStack(ModItems.CHORUS_SOUP.get(), 3, 0), PriceInfo(-2, -1)),
        )
    }

    /** The villager buys [item] and pays one emerald. */
    private fun buys(item: ItemStack, amount: PriceInfo) = ITradeList { _: IMerchant, list: MerchantRecipeList, random: Random ->
        list.add(MerchantRecipe(ItemStackUtils.copyStackWithAmount(item, amount.getPrice(random)), Items.EMERALD))
    }

    /** The villager sells [item]. A negative price means that many items for one emerald, else one item for that many emeralds. */
    private fun sells(item: ItemStack, price: PriceInfo) = ITradeList { _: IMerchant, list: MerchantRecipeList, random: Random ->
        val i = price.getPrice(random)
        list.add(
            if (i < 0) MerchantRecipe(ItemStack(Items.EMERALD), ItemStackUtils.copyStackWithAmount(item, -i))
            else MerchantRecipe(ItemStack(Items.EMERALD, i, 0), ItemStackUtils.copyStackWithAmount(item, 1)),
        )
    }
}
