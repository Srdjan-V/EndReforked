package io.github.srdjanv.endreforked.compat.jei.fluids

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.common.registry.GameAdapter
import io.github.srdjanv.endreforked.common.registry.GameAdapter.toStack
import io.github.srdjanv.endreforked.compat.jei.RecipeFeed
import io.github.srdjanv.endreforked.impl.world.FluidEntityInteractions
import io.github.srdjanv.endreforked.utils.LangUtil
import jeresources.util.Font
import jeresources.util.RenderHelper
import mezz.jei.api.IGuiHelper
import mezz.jei.api.gui.IDrawable
import mezz.jei.api.gui.IRecipeLayout
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.ingredients.VanillaTypes
import mezz.jei.api.recipe.IRecipeCategory
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityEvoker
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityVindicator
import net.minecraft.entity.monster.EntityWitch
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

/**
 * A fluid entity interaction as JEI shows it: the entity is drawn, the fluid and an item entity's stack go in,
 * and the new item or fluid block comes out. A spawned mob has no output slot.
 */
class FluidEntityInteractionWrapper private constructor(
    private val interaction: FluidEntityInteraction,
    private val fluid: FluidStack,
    val inItem: ItemStack?,
    val outItem: ItemStack?,
    val outFluid: FluidStack?,
) : IRecipeWrapper {
    private var entity: Entity? = null

    private fun entity(minecraft: Minecraft): Entity? {
        val world = minecraft.world ?: return null
        if (entity?.world !== world) entity = GameAdapter.createEntity(interaction.entity, world)
        return entity
    }

    override fun drawInfo(minecraft: Minecraft, recipeWidth: Int, recipeHeight: Int, mouseX: Int, mouseY: Int) {
        val entity = entity(minecraft) ?: return
        if (entity is EntityLivingBase) {
            RenderHelper.scissor(minecraft, 7, 43, 59, 79)
            val offsetY = offsetY(entity)
            RenderHelper.renderEntity(25, 50 - offsetY, scale(entity), (38 - mouseX).toFloat(), (70 - offsetY - mouseY).toFloat(), entity)
            RenderHelper.stopScissor()
        }
        Font.normal.print(entity.name, 7, 2)
    }

    override fun getIngredients(ingredients: IIngredients) {
        ingredients.setInput(VanillaTypes.FLUID, fluid)
        inItem?.let { ingredients.setInput(VanillaTypes.ITEM, it) }
        outItem?.let { ingredients.setOutput(VanillaTypes.ITEM, it) }
        outFluid?.let { ingredients.setOutput(VanillaTypes.FLUID, it) }
    }

    private fun scale(entity: Entity): Float {
        val width = entity.width
        val height = entity.height
        return if (width <= height) {
            when {
                height < 0.9 -> 50f
                height < 1.0 -> 35f
                height < 1.8 -> 33f
                height < 2.0 -> 32f
                height < 3.0 -> 24f
                height < 4.0 -> 20f
                else -> 10f
            }
        } else {
            when {
                width < 1.0 -> 38f
                width < 2.0 -> 27f
                width < 3.0 -> 13f
                else -> 9f
            }
        }
    }

    private fun offsetY(entity: Entity) = when (entity) {
        is EntitySquid -> 20
        is EntityWitch -> -10
        is EntityGhast -> 15
        is EntityWither -> -15
        is EntityDragon -> 15
        is EntityEnderman -> -10
        is EntityGolem -> -10
        is EntityAnimal -> -20
        is EntityVillager -> -15
        is EntityVindicator -> -15
        is EntityEvoker -> -10
        is EntityBlaze -> -10
        is EntityCreeper -> -15
        else -> 0
    }

    companion object {
        /** Returns null when this client does not have the fluid, the entity, the items or the blocks. */
        fun of(interaction: FluidEntityInteraction): FluidEntityInteractionWrapper? {
            val fluid = GameAdapter.fluid(interaction.fluid) ?: return null
            if (!GameAdapter.hasEntity(interaction.entity)) return null
            val input = interaction.item?.let { i -> GameAdapter.item(i.id)?.let { ItemStack(it, i.count, i.meta) } ?: return null }
            var outItem: ItemStack? = null
            var outFluid: FluidStack? = null
            when (val r = interaction.result) {
                is EntityResult.SpawnItem -> outItem = r.item.toStack().takeIf { !it.isEmpty } ?: return null
                is EntityResult.SpawnEntity -> if (!GameAdapter.hasEntity(r.entity)) return null
                is EntityResult.SetFluidBlock -> {
                    val block = BlockIngredient(GameAdapter.state(r.block) ?: return null)
                    if (!block.valid) return null
                    outFluid = block.fluid
                    outItem = block.item.takeIf { !it.isEmpty }
                }
            }
            return FluidEntityInteractionWrapper(interaction, FluidStack(fluid, 1_000), input, outItem, outFluid)
        }
    }
}

class FluidInteractionEntityCategory(helper: IGuiHelper) : IRecipeCategory<FluidEntityInteractionWrapper> {
    private val background = helper.createBlankDrawable(160, 60)

    override fun getUid() = UID

    override fun getTitle(): String = LangUtil.translateToLocal("fluid.interaction.entity")

    override fun getModName() = Tags.MODNAME

    override fun getBackground(): IDrawable = background

    override fun setRecipe(layout: IRecipeLayout, recipe: FluidEntityInteractionWrapper, ingredients: IIngredients) {
        val fluids = layout.fluidStacks
        val items = layout.itemStacks
        fluids.init(0, true, 70, 20)
        if (recipe.inItem != null) items.init(0, true, 95, 20)
        if (recipe.outItem != null) items.init(1, false, 125, 20)
        if (recipe.outFluid != null) fluids.init(1, false, 125, 20)
        fluids.set(ingredients)
        items.set(ingredients)
    }

    companion object {
        const val UID = Tags.MODID + ".fluid_entity_interaction"

        @JvmField
        val FEED = RecipeFeed<FluidEntityInteraction, FluidEntityInteractions>(UID) { snapshot ->
            snapshot.entries.mapNotNull(FluidEntityInteractionWrapper::of)
        }
    }
}
