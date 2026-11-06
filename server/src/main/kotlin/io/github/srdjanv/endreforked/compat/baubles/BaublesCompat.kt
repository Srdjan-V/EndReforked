package io.github.srdjanv.endreforked.compat.baubles

import baubles.api.BaublesApi
import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.compat.CompatManger
import io.github.srdjanv.endreforked.utils.PlayerUtils
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.crafting.ShapelessRecipes
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class BaublesCompat : CompatManger.ModCompat {
    private val ring by lazy { EntropyWingsRing() }

    init {
        PlayerUtils.inventories += { player ->
            val handler = BaublesApi.getBaublesHandler(player)
            if (handler == null) emptyList() else List(handler.slots) { handler.getStackInSlot(it) }
        }
    }

    @SubscribeEvent
    fun onRecipeRegister(event: RegistryEvent.Register<IRecipe>) {
        event.registry.register(recipe(ring, ModItems.ENTROPY_WINGS.get()))
        event.registry.register(recipe(ModItems.ENTROPY_WINGS.get(), ring))
    }

    private fun recipe(result: Item, ingredient: Item): IRecipe {
        val recipe = ShapelessRecipes(
            result.registryName!!.path, ItemStack(result), NonNullList.from(Ingredient.EMPTY, Ingredient.fromItem(ingredient)),
        )
        recipe.setRegistryName(ResourceLocation(Tags.MODID, recipe.group))
        return recipe
    }

    @SubscribeEvent
    fun onItemRegister(event: RegistryEvent.Register<Item>) = event.registry.register(ring)

    @SubscribeEvent
    fun registerModels(event: ModelRegistryEvent) = ring.handleAssets()

    override fun registerEventBus() = registerThisToEventBus()
}
