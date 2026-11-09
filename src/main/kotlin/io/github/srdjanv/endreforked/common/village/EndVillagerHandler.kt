package io.github.srdjanv.endreforked.common.village

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.utils.ItemStackUtils
import net.minecraft.entity.IMerchant
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.passive.EntityVillager.ITradeList
import net.minecraft.entity.passive.EntityVillager.PriceInfo
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

        // The End explorer takes tungsten. It never sells a gate item (infused ingot, stabilizer, catalyst, machines).
        val spore = ItemStack(ModItems.ORGANA_SPORE_BLOCK, 4)
        val coral = ItemStack(ModItems.END_CORAL, 4)
        VillagerCareer(profExplorer, Tags.MODID + ".end_explorer")
            .addTrade(
                1,
                tungsten(4, ItemStack(Items.ENDER_PEARL, 2)),
                tungsten(6, ItemStack(ModItems.END_ESSENCE, 2)),
                buys(ItemStack(Items.ENDER_PEARL), PriceInfo(3, 3)),
            )
            .addTrade(
                2,
                tungsten(8, ItemStack(ModItems.DRAGONITE_TEA)),
                tungsten(6, ItemStack(ModItems.ORGANA_FRUIT, 2)),
                tungsten(4, ItemStack(ModItems.VOIDBLOOM_CROP)),
            )
            .addTrade(
                3,
                tungsten(10, ItemStack(ModItems.ITEM_LORMYTE_CRYSTAL, 2)),
                tungsten(12, ItemStack(ModItems.ORGANA_FLOWER_SEED, 2)),
                ITradeList { _: IMerchant, list: MerchantRecipeList, random: Random ->
                    list.add(MerchantRecipe(ItemStack(ModItems.TUNGSTEN_INGOT, 4), (if (random.nextBoolean()) spore else coral).copy()))
                },
            )
    }

    /** The villager sells [item] for [price] tungsten ingots. */
    private fun tungsten(price: Int, item: ItemStack) = ITradeList { _: IMerchant, list: MerchantRecipeList, _: Random ->
        list.add(MerchantRecipe(ItemStack(ModItems.TUNGSTEN_INGOT, price), item.copy()))
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
