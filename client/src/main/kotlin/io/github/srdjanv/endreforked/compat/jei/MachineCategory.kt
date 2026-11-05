package io.github.srdjanv.endreforked.compat.jei

import com.cleanroommc.modularui.drawable.GuiTextures
import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.utils.LangUtil
import mezz.jei.api.IGuiHelper
import mezz.jei.api.gui.IDrawable
import mezz.jei.api.recipe.IRecipeCategory
import net.minecraft.client.Minecraft

/**
 * The layout of the End Forge and the Materializer: two input slots at x 35 and 55, an arrow, and an output slot at x 105.
 * Subclasses put the ingredients in the slots.
 */
abstract class MachineCategory(helper: IGuiHelper, private val uid: String, private val titleKey: String) :
    IRecipeCategory<MachineRecipeWrapper> {
    private val background = helper.createBlankDrawable(160, 60)
    private val slot = helper.slotDrawable

    override fun getUid() = uid

    override fun getTitle(): String = LangUtil.translateToLocal(titleKey)

    override fun getModName() = Tags.MODNAME

    override fun getBackground(): IDrawable = background

    override fun drawExtras(minecraft: Minecraft) {
        GuiTextures.PROGRESS_ARROW.getSubArea(0f, 0f, 1f, 0.5f).draw(78f, 20f, 20f, 20f)
        val progress = Minecraft.getSystemTime() % 2000 / 2000f // 2 seconds
        GuiTextures.PROGRESS_ARROW.getSubArea(0f, 0.5f, progress, 1f).draw(78f, 20f, progress * 20, 20f)
        slot.draw(minecraft, 105, 20)
        slot.draw(minecraft, 55, 20)
        slot.draw(minecraft, 35, 20)
    }
}
