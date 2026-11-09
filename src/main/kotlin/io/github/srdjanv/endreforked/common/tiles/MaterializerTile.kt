package io.github.srdjanv.endreforked.common.tiles

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.drawable.keys.DynamicKey
import com.cleanroommc.modularui.drawable.keys.LangKey
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.BooleanSyncValue
import com.cleanroommc.modularui.value.sync.DoubleSyncValue
import com.cleanroommc.modularui.value.sync.GuiSyncManager
import com.cleanroommc.modularui.value.sync.IntSyncValue
import com.cleanroommc.modularui.value.sync.SyncHandlers
import com.cleanroommc.modularui.widget.ParentWidget
import com.cleanroommc.modularui.widgets.ButtonWidget
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.ProgressWidget
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import io.github.srdjanv.endreforked.api.base.crafting.TileStatus
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.api.entropy.EntropyRadiusUpgrade
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.api.machine.nextWorldEvent
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.blocks.BlockMaterializer
import io.github.srdjanv.endreforked.common.entropy.Stabilizers
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import io.github.srdjanv.endreforked.common.machine.InputHandler
import io.github.srdjanv.endreforked.common.machine.MachineIO
import io.github.srdjanv.endreforked.common.machine.MachineProcess
import io.github.srdjanv.endreforked.common.machine.OutputHandler
import io.github.srdjanv.endreforked.common.machine.ProcessHooks
import io.github.srdjanv.endreforked.common.machine.recipes
import io.github.srdjanv.endreforked.common.materializer.GhostPreview
import io.github.srdjanv.endreforked.common.materializer.WorldEvents
import io.github.srdjanv.endreforked.common.registry.MachineRecipeDefaults
import io.github.srdjanv.endreforked.common.tiles.base.BaseTileEntity
import io.github.srdjanv.endreforked.common.widgets.BasicTextWidget
import io.github.srdjanv.endreforked.common.widgets.BlockStateRendereWidget
import io.github.srdjanv.endreforked.utils.LangUtil
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ITickable
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import org.apache.commons.lang3.tuple.Pair

/**
 * The Materializer: three items go in (an ingredient, more of it or a second one, and the catalyst, in any slot), the output comes out.
 * A radius upgrade sets the chunks that the recipe takes entropy from, like in the Entropy Chamber.
 * While a recipe runs, its world events change blocks near the machine. An event that triggers and finds no structure ruins the run.
 * The inputs stay in their slots until the recipe is done, or the run fails and they are lost.
 */
class MaterializerTile : BaseTileEntity(), ITickable, IGuiHolder<PosGuiData> {
    private val reader = EntropyChunkReader.ofTileEntity(this, EntropyRadius.ONE)

    private val upgradeSlot = object : InternalItemStackHandler(1) {
        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack =
            if (stack.item is EntropyRadiusUpgrade) super.insertItem(slot, stack, simulate) else stack
    }

    private val process: MachineProcess = MachineProcess(
        MachineRecipeDefaults.MATERIALIZER,
        MachineIO(
            InputHandler(
                3,
                { recipes().acceptsItem(MachineRecipeDefaults.MATERIALIZER, it) },
                { markDirty(); process.inputChanged() },
            ),
            OutputHandler(1) { markDirty() },
            emptyList(),
            emptyList(),
        ),
        this::recipes,
        object : ProcessHooks {
            override fun onRunning(recipe: MachineRecipe, ticksRun: Int): Boolean {
                var next = recipe.nextWorldEvent(lastEvent)
                while (next != null && ticksRun * 100 >= next.atPercent * recipe.ticks) {
                    lastEvent = next.atPercent
                    if (!trigger(next)) {
                        world.addWeatherEffect(EntityLightningBolt(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), false))
                        circle(EnumParticleTypes.FLAME)
                        return false
                    }
                    next = recipe.nextWorldEvent(lastEvent)
                }
                return true
            }

            override fun absorbOverload() = Stabilizers.absorb(world as WorldServer, pos)

            override fun onFinished(recipe: MachineRecipe) = circle(EnumParticleTypes.PORTAL)
        },
    )

    private val itemIn get() = process.io.itemInputs
    private val itemOut get() = process.io.itemOutputs

    /** In manual mode a run only starts after the player pressed Start. After a run ends, the machine waits again. Default: manual. */
    private var manualStart = true
    private var startRequested = false

    /** The percent of the last world event that was tried in this run, 0 before the first. The GUI shows the next one. */
    private var lastEvent = 0
    private var blockStateTimer = 0

    // The last values from the server, for the GUI on the client
    private var percentage = 0.0
    private var failTick = 0
    private var failed = false

    init {
        inventoriesToDrop.add(itemIn)
        inventoriesToDrop.add(itemOut)
        inventoriesToDrop.add(upgradeSlot)
    }

    override fun getDisplayName(): ITextComponent = TextComponentTranslation("container.entropy_user")

    /** The WORKING state change must not recreate the tile. */
    override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState) =
        oldState.block !== newState.block

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)
        process.deserializeNBT(compound.getCompoundTag("process"))
        lastEvent = compound.getInteger("last_event")
        if (compound.hasKey("manual_start")) manualStart = compound.getBoolean("manual_start")
        startRequested = compound.getBoolean("start_requested")
        if (compound.hasKey("upgrade_slot")) upgradeSlot.deserializeNBT(compound.getCompoundTag("upgrade_slot"))
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setTag("process", process.serializeNBT())
        compound.setInteger("last_event", lastEvent)
        compound.setBoolean("manual_start", manualStart)
        compound.setBoolean("start_requested", startRequested)
        compound.setTag("upgrade_slot", upgradeSlot.serializeNBT())
        return super.writeToNBT(compound)
    }

    override fun update() {
        if (!world.isRemote) {
            updateBlockState()
            updateRange()
            tickProcess()
            if (process.ticksRun == 0) lastEvent = 0 // a new run, or the last one ended
        } else if (process.status == TileStatus.FAILED || failTick > 0) {
            // show the failure text for 3 seconds
            if (failTick == 0) failed = true
            if (failTick > 20 * 3) {
                failed = false
                failTick = 0
            }
            failTick++
        }
    }

    /** In manual mode the process only ticks after Start, or while a run is under way. When a run ends, the machine waits for Start again. */
    private fun tickProcess() {
        val underWay = process.ticksRun > 0
        if (manualStart && !startRequested && !underWay) {
            process.status = when {
                process.recipe != null -> TileStatus.WAITING_START
                process.io.isInputEmpty() -> TileStatus.IDLE
                else -> TileStatus.INVALID
            }
            return
        }
        process.tick(reader.entropyView, EntropyWorldHandler.levelAt(world.provider.dimension, pos))
        if (underWay && process.ticksRun == 0) startRequested = false
    }

    /** The world event whose structure the ghost preview shows: the next one of the loaded recipe, else the first. Null without a recipe or event. */
    fun ghostEvent(): WorldEvent? = process.recipe?.let { it.nextWorldEvent(lastEvent) ?: it.worldEvents.firstOrNull() }

    private fun updateRange() {
        val upgrade = upgradeSlot.getStackInSlot(0).item
        reader.updateRadius(if (upgrade is EntropyRadiusUpgrade) upgrade.entropyRadius else EntropyRadius.ONE)
    }

    /** Rolls the chance of the event, then runs it. False means that it triggered and found no structure. */
    private fun trigger(event: WorldEvent): Boolean =
        world.rand.nextInt(100) >= event.chance || WorldEvents.run(world as WorldServer, pos, event)

    private fun updateBlockState() {
        if (++blockStateTimer % (20 * 2) != 0) return
        blockStateTimer = 0
        val state = world.getBlockState(pos)
        val working = state.getValue(BlockMaterializer.WORKING)
        val running = process.status == TileStatus.RUNNING
        if (running == working) return
        if (running) {
            world.playSound(
                null, pos, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 0.5f,
                2.6f + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8f,
            )
        }
        world.setBlockState(
            pos,
            ModBlocks.MATERIALIZER_BLOCK.defaultState
                .withProperty(BlockMaterializer.FACING, state.getValue(BlockMaterializer.FACING))
                .withProperty(BlockMaterializer.WORKING, running),
            3,
        )
    }

    private fun circle(type: EnumParticleTypes) {
        for (i in 0 until 360 step 40) {
            val xSpeed = Math.cos(Math.toRadians(i.toDouble())) * 0.2
            val zSpeed = Math.sin(Math.toRadians(i.toDouble())) * 0.2
            (world as WorldServer).spawnParticle(type, pos.x + 0.5, pos.y + 1.0, pos.z + 0.5, 1, xSpeed, 0.0, zSpeed, 0.2)
        }
    }

    override fun buildUI(data: PosGuiData, syncManager: GuiSyncManager): ModularPanel {
        val panel = ModularPanel.defaultPanel("materializer_gui").bindPlayerInventory()

        syncManager.syncValue("last_event", IntSyncValue({ lastEvent }, { lastEvent = it }))
        // The buttons write the client value and send it to the server (the pattern of the battery GUI).
        syncManager.syncValue("manual_start", 0, BooleanSyncValue({ manualStart }, { manualStart = it }))
        syncManager.syncValue("start_requested", 0, BooleanSyncValue({ startRequested }, { startRequested = it && (world.isRemote || process.recipe != null) }))
        syncManager.syncValue(
            "status",
            SyncHandlers.enumValue(TileStatus::class.java, { process.status }, { process.status = it }),
        )
        syncManager.syncValue("percentage", DoubleSyncValue({ process.progress() * 100 }, { percentage = it }))

        if (data.isClient) {
            val textBox = BasicTextWidget().left(20).top(3).right(20).background(GuiTextures.MC_BACKGROUND)
            textBox.setKey { if (failed) TileStatus.FAILED.langKey else process.status.langKey }
            panel.child(textBox)

            // The preview reads the recipe from the client registry, with the input slots that the GUI synced.
            val render = BlockStateRendereWidget().background(GuiTextures.MC_BACKGROUND).size(176, 166).leftRel(1f)
            render.onUpdateListener { thatRender ->
                val recipe = process.recipe
                if (recipe == null) {
                    thatRender.disableRender(true)
                } else if (!failed) {
                    val event = recipe.nextWorldEvent(lastEvent)
                    if (event == null) {
                        thatRender.disableRender(true)
                    } else if (percentage == 0.0 || event.atPercent > percentage) {
                        thatRender.disableRender(false)
                        thatRender.setStructure(WorldEvents.compile(event.structure).states)
                    }
                }
            }

            val renderText = BasicTextWidget().background(GuiTextures.MC_BACKGROUND).bottomRel(1f).left(8).right(8).height(25)
            renderText.setKeyArg {
                val recipe = process.recipe
                if (recipe == null || percentage <= 0 || percentage >= 100) {
                    null
                } else if (failed) {
                    Pair.of("tile.materializer.render.fail", null)
                } else {
                    recipe.nextWorldEvent(lastEvent)?.let {
                        Pair.of("tile.materializer.render.info", arrayOf<Any>(it.chance, it.atPercent))
                    }
                }
            }
            render.child(renderText)

            val renderControls = Box().background(GuiTextures.MC_BACKGROUND).topRel(1f).left(8).right(8).height(25)
            render.child(renderControls)

            fun button(right: Int, tooltipKey: String, onClick: () -> Unit) =
                Btn().background(GuiTextures.MC_BACKGROUND).right(right).top(3).also { button ->
                    button.tooltip().addLine(LangKey(tooltipKey))
                    button.onMousePressed { mouseButton -> (mouseButton == 0).also { if (it) onClick() } }
                    renderControls.child(button)
                }
            button(20, "tile.materializer.render.followStruct") { render.isDisableOnUpdateListener = !render.isDisableOnUpdateListener }
            button(60, "tile.materializer.render.toggleFullRender") { render.setRenderLayer(render.maxRenderLayer) }
            button(80, "tile.materializer.render.nextLayer") { render.nextRenderLayer() }
            button(100, "tile.materializer.render.prevLayer") { render.prevRenderLayer() }

            panel.child(render)
            data.jeiSettings.addJeiExclusionArea(render)
            data.jeiSettings.addJeiExclusionArea(renderText)
            data.jeiSettings.addJeiExclusionArea(renderControls)

            val showButton = Btn().background(GuiTextures.MC_BACKGROUND).right(168).bottom(75)
                .tooltip { it.addLine(LangKey("tile.materializer.render.show")) }
            panel.child(showButton)
            showButton.overlay(DynamicKey { percentage.toInt().toString() })
            showButton.onMousePressed { mouseButton ->
                (mouseButton == 0).also {
                    if (it) {
                        render.isEnabled = !render.isEnabled
                        renderText.isEnabled = !renderText.isEnabled
                    }
                }
            }
            render.isEnabled = false
            renderText.isEnabled = false

            fun press(name: String, value: Boolean) =
                (syncManager.getSyncHandler(GuiSyncManager.makeSyncKey(name, 0)) as BooleanSyncValue).setBoolValue(value, true, true)

            fun textButton(left: Int, width: Int, key: () -> String, onClick: () -> Unit) {
                val button = Btn().background(GuiTextures.MC_BACKGROUND).left(left).top(24).size(width, 16).overlay(DynamicKey { LangUtil.translateToLocal(key()) })
                button.onMousePressed { mouseButton -> (mouseButton == 0).also { if (it) onClick() } }
                panel.child(button)
            }
            textButton(30, 50, { if (manualStart) "tile.materializer.mode.manual" else "tile.materializer.mode.auto" }) { press("manual_start", !manualStart) }
            textButton(85, 35, { "tile.materializer.start" }) { press("start_requested", true) }
            textButton(125, 44, { if (GhostPreview.enabled) "tile.materializer.ghost.on" else "tile.materializer.ghost.off" }) { GhostPreview.enabled = !GhostPreview.enabled }
            GhostPreview.opened(pos)

            panel.child(
                ProgressWidget().size(18).left(95).top(45)
                    .texture(GuiTextures.PROGRESS_ARROW, 25)
                    .progress { percentage / 100 },
            )
        }

        panel.child(ItemSlot().slot(ModularSlot(itemIn, 0)).left(30).top(45))
        panel.child(ItemSlot().slot(ModularSlot(itemIn, 1)).left(50).top(45))
        panel.child(ItemSlot().slot(ModularSlot(itemIn, 2)).left(70).top(45))

        val upgrade = ItemSlot().slot(ModularSlot(upgradeSlot, 0).slotGroup(SlotGroup.singleton("upgrade_item_in", 110))).left(7).top(45)
        upgrade.tooltip().setAutoUpdate(true).tooltipBuilder { tooltip ->
            val item = upgrade.ingredient
            if (item is EntropyRadiusUpgrade) {
                val range = item.entropyRadius
                tooltip.addLine(LangKey("entropy.range.chunks").get() + ": " + range.chunks)
                tooltip.addLine(LangKey("entropy.range.radius").get() + ": " + range.radius)
            }
        }
        panel.child(upgrade)
        panel.child(ItemSlot().slot(ModularSlot(itemOut, 0).accessibility(false, true)).left(120).top(45))
        return panel
    }

    // Every side but the top. The sides take input, the bottom gives the output.
    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability === CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null && facing != EnumFacing.UP ||
            super.hasCapability(capability, facing)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? = when {
        capability === CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null && facing != EnumFacing.UP ->
            (if (facing == EnumFacing.DOWN) itemOut else itemIn) as T
        else -> super.getCapability(capability, facing)
    }

    private class Box : ParentWidget<Box>()

    private class Btn : ButtonWidget<Btn>()
}
