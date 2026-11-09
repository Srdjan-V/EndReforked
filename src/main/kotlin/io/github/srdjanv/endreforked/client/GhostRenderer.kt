package io.github.srdjanv.endreforked.client

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.blocks.BlockMaterializer
import io.github.srdjanv.endreforked.common.materializer.GhostPreview
import io.github.srdjanv.endreforked.common.materializer.WorldEvents
import io.github.srdjanv.endreforked.common.tiles.MaterializerTile
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.model.pipeline.LightUtil
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

/**
 * Draws the structure of the loaded recipe of the Materializer as translucent blocks, in its default place (see [WorldEvents.ghostPos]).
 * It is a guide only. Blocks that already stand there are not drawn. Only for the Materializer that the player opened last,
 * while the GUI toggle is on and the machine is within [RANGE] blocks.
 */
object GhostRenderer {
    private const val RANGE = 32.0
    private const val ALPHA = 0x80

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!GhostPreview.enabled) return
        val machine = GhostPreview.pos ?: return
        val mc = Minecraft.getMinecraft()
        val world = mc.world ?: return
        val player = mc.player ?: return
        if (player.getDistanceSq(machine) > RANGE * RANGE) return
        val state = world.getBlockState(machine)
        if (state.block !== ModBlocks.MATERIALIZER_BLOCK) return
        val event0 = (world.getTileEntity(machine) as? MaterializerTile)?.ghostEvent() ?: return
        val facing = state.getValue(BlockMaterializer.FACING)
        val states = WorldEvents.compile(event0.structure).states

        val partial = event.partialTicks.toDouble()
        val camX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partial
        val camY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partial
        val camZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partial

        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.disableLighting()
        GlStateManager.depthMask(false)
        mc.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        for ((a, aisle) in states.withIndex()) for ((r, row) in aisle.withIndex()) for ((c, ghost) in row.withIndex()) {
            if (ghost.block === net.minecraft.init.Blocks.AIR || ghost.renderType != EnumBlockRenderType.MODEL) continue
            val pos = WorldEvents.ghostPos(machine, facing, a, r, c)
            if (world.getBlockState(pos) == ghost) continue // already built
            GlStateManager.pushMatrix()
            GlStateManager.translate(pos.x - camX, pos.y - camY, pos.z - camZ)
            val buffer = Tessellator.getInstance().buffer
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM)
            val model = mc.blockRendererDispatcher.getModelForState(ghost)
            for (face in listOf<EnumFacing?>(null) + EnumFacing.values()) {
                for (quad in model.getQuads(ghost, face, 0L)) LightUtil.renderQuadColor(buffer, quad, (ALPHA shl 24) or 0xFFFFFF)
            }
            Tessellator.getInstance().draw()
            GlStateManager.popMatrix()
        }
        GlStateManager.depthMask(true)
        GlStateManager.enableLighting()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }
}
