package io.github.srdjanv.endreforked.common.widgets

import com.cleanroommc.modularui.api.widget.Interactable
import com.cleanroommc.modularui.screen.ModularScreen
import com.cleanroommc.modularui.screen.viewport.GuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widget.ParentWidget
import io.github.srdjanv.endreforked.EndReforked
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.init.Biomes
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.WorldType
import net.minecraft.world.biome.Biome
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

class BlockStateRendereWidget : ParentWidget<BlockStateRendereWidget>(), Interactable {
    var isDisableOnUpdateListener = false
    private var disableRender = false
    private var tick = 0
    private var scale = 30f
    private var rotX = 25f
    private var rotY = -45f
    private var renderInfo: StructureRenderData? = null
    private var blockAccess: BlockAccessRendererWrapper? = null
    private var lastX = 0
    private var lastY = 0

    override fun onUpdate() {
        if (isDisableOnUpdateListener) return
        super.onUpdate()
    }

    fun setStructure(structure: Array<Array<Array<IBlockState>>>) {
        if (renderInfo?.structure === structure) return
        val info = StructureRenderData(structure)
        renderInfo = info
        blockAccess = BlockAccessRendererWrapper(info)
        updateScale()
    }

    override fun onMouseScroll(scrollDirection: ModularScreen.UpOrDown, amount: Int): Boolean {
        when (scrollDirection) {
            ModularScreen.UpOrDown.UP -> scale += amount.toFloat() / 100
            ModularScreen.UpOrDown.DOWN -> scale -= amount.toFloat() / 100
            else -> return false
        }
        return true
    }

    override fun onMouseDrag(mouseButton: Int, timeSinceClick: Long) {
        if (mouseButton != 0) return
        val mouseX = context.mouseX
        val mouseY = context.mouseY
        if (timeSinceClick != 0L && lastX != 0 && lastY != 0) {
            rotY += (mouseX - lastX) / 104f * 80
            rotX += (mouseY - lastY) / 100f * 80
        }
        lastX = mouseX
        lastY = mouseY
    }

    override fun postResize() {
        super.postResize()
        updateScale()
    }

    private fun updateScale() {
        val info = renderInfo ?: return
        scale = min(area.width / info.structureWidth * 0.5, area.height.toDouble() / info.structureHeight * 0.5).toFloat() // the width part divides as int, as before
    }

    fun prevRenderLayer() = setRenderLayer(max(renderInfo!!.blockIndex - 1, -1))

    fun nextRenderLayer() = renderInfo!!.blockIndex.plus(1).let { setRenderLayer(if (it >= maxRenderLayer) -1 else it) }

    fun setRenderLayer(layer: Int) = renderInfo!!.setLayerLimiter(layer)

    val maxRenderLayer: Int get() = renderInfo!!.maxBlockIndex

    fun disableRender(disableRender: Boolean) {
        this.disableRender = disableRender
    }

    override fun draw(context: GuiContext, widgetTheme: WidgetTheme) {
        if (disableRender) return

        val stackDepth = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)
        try {
            val info = renderInfo!!
            val access = blockAccess!!
            if (++tick % 20 == 0) info.step()

            val structureLength = info.structureLength
            val structureWidth = info.structureWidth
            val structureHeight = info.structureHeight
            GlStateManager.enableRescaleNormal()
            GlStateManager.pushMatrix()
            RenderHelper.disableStandardItemLighting()
            val blockRender = Minecraft.getMinecraft().blockRendererDispatcher
            GlStateManager.translate(
                area.width.toFloat() / 2, area.height.toFloat() / 2,
                max(structureHeight, max(structureWidth, structureLength)).toFloat(),
            )
            GlStateManager.scale(scale, -scale, 1f)
            GlStateManager.rotate(rotX, 1f, 0f, 0f)
            GlStateManager.rotate(90f + rotY, 0f, 1f, 0f)
            GlStateManager.translate(structureLength / -2f, structureHeight / -2f, structureWidth / -2f)
            GlStateManager.disableLighting()
            GlStateManager.enableDepth()
            GlStateManager.shadeModel(if (Minecraft.isAmbientOcclusionEnabled()) 7425 else 7424)

            Minecraft.getMinecraft().textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
            for (h in 0 until structureHeight) for (l in 0 until structureLength) for (w in 0 until structureWidth) {
                val pos = BlockPos(l, h, w)
                if (!access.isAirBlock(pos)) {
                    val tessellator = Tessellator.getInstance()
                    val buffer = tessellator.buffer
                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK)
                    blockRender.renderBlock(access.getBlockState(pos), pos, access, buffer)
                    tessellator.draw()
                }
            }

            GlStateManager.disableDepth()
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()
            RenderHelper.disableStandardItemLighting()
            GlStateManager.disableRescaleNormal()
            GlStateManager.enableBlend()
            RenderHelper.disableStandardItemLighting()
        } catch (exception: Exception) {
            EndReforked.LOGGER.error(exception)
        }

        repeat(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) - stackDepth) { GlStateManager.popMatrix() }
    }

    private class StructureRenderData(val structure: Array<Array<Array<IBlockState>>>) {
        val structureHeight = structure.size
        val structureLength = structure.maxOfOrNull { it.size } ?: 0
        val structureWidth = structure.maxOfOrNull { layer -> layer.maxOfOrNull { it.size } ?: 0 } ?: 0
        val maxBlockIndex = structureHeight * structureLength * structureWidth
        var blockIndex = maxBlockIndex
            private set

        fun setLayerLimiter(layer: Int) {
            blockIndex = if (layer < 0) maxBlockIndex else (layer + 1) * (structureLength * structureWidth) - 1
        }

        fun step() {
            val currentIndex = blockIndex
            do {
                if (++blockIndex >= maxBlockIndex) blockIndex = 0
            } while (isEmpty(blockIndex) && blockIndex != currentIndex)
        }

        private fun isEmpty(index: Int): Boolean {
            val y = index / (structureLength * structureWidth)
            val r = index % (structureLength * structureWidth)
            val state = structure.getOrNull(y)?.getOrNull(r / structureWidth)?.getOrNull(r % structureWidth)
            return state == null || state == Blocks.AIR.defaultState
        }

        fun getBlockState(pos: BlockPos): IBlockState {
            val (x, y, z) = Triple(pos.x, pos.y, pos.z)
            val state = structure.getOrNull(y)?.getOrNull(x)?.getOrNull(z)
            return if (state != null && y * (structureLength * structureWidth) + x * structureWidth + z <= blockIndex) state
            else Blocks.AIR.defaultState
        }
    }

    private class BlockAccessRendererWrapper(private val data: StructureRenderData) : IBlockAccess {
        override fun getTileEntity(pos: BlockPos): TileEntity? = null

        override fun getCombinedLight(pos: BlockPos, lightValue: Int) = 15728880

        override fun getBlockState(pos: BlockPos): IBlockState = data.getBlockState(pos)

        override fun isAirBlock(pos: BlockPos) = getBlockState(pos).block === Blocks.AIR

        override fun getBiome(pos: BlockPos): Biome = Minecraft.getMinecraft().world?.getBiome(pos) ?: Biomes.BIRCH_FOREST

        override fun getStrongPower(pos: BlockPos, direction: EnumFacing) = 0

        override fun getWorldType(): WorldType = Minecraft.getMinecraft().world?.worldType ?: WorldType.DEFAULT

        override fun isSideSolid(pos: BlockPos, side: EnumFacing, default: Boolean) = false
    }
}
