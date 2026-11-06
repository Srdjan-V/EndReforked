package io.github.srdjanv.endreforked.common.tiles

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.drawable.keys.LangKey
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.BooleanSyncValue
import com.cleanroommc.modularui.value.sync.GuiSyncManager
import com.cleanroommc.modularui.value.sync.IntSyncValue
import com.cleanroommc.modularui.widgets.ButtonWidget
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage
import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler
import io.github.srdjanv.endreforked.common.entropy.storage.DefaultEntropyStorage
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import io.github.srdjanv.endreforked.common.widgets.BasicTextWidget
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraftforge.common.capabilities.Capability
import java.util.Optional

class SmallEntropyBatteryTile : TileEntity(), IGuiHolder<PosGuiData>, ITickable, EntropyDataProvider {
    private val reader = EntropyChunkReader.ofTileEntity(this)
    private val storage = DefaultEntropyStorage(250)
    private var linkDirty = true
    private var forcedLink = false

    // client copies for the gui
    private var chunkEntropy = 0
    private var batteryEntropy = 0
    private var linked = false

    override val entropyStorage: Optional<EntropyStorage> get() = Optional.of(storage)

    override fun update() {
        if (!world.isRemote && linkDirty) {
            linkToChunk(forcedLink)
            linkDirty = false
        }
    }

    override fun buildUI(data: PosGuiData, syncManager: GuiSyncManager): ModularPanel {
        val panel = ModularPanel.defaultPanel("small_entropy_battery_gui", 130, 96)
        syncManager.syncValue("link_dirty", 0, BooleanSyncValue({ linkDirty }, { linkDirty = it }))
        syncManager.syncValue("linked", BooleanSyncValue({ isLinked() }, { linked = it }))
        syncManager.syncValue("forced_link", 0, BooleanSyncValue({ forcedLink }, { forcedLink = it }))
        syncManager.syncValue("battery_entropy", IntSyncValue({ storage.currentEntropy }, { batteryEntropy = it }))
        syncManager.syncValue("chunk_entropy", IntSyncValue({ reader.entropyView.currentEntropy }, { chunkEntropy = it }))

        if (data.isClient) {
            fun text(top: Int, tooltip: String?, key: () -> String) {
                val widget = BasicTextWidget().leftRel(0.5f).top(top).size(120, 16).background(GuiTextures.MC_BACKGROUND)
                widget.setKey(key)
                if (tooltip != null) widget.tooltip().addLine(LangKey(tooltip))
                panel.child(widget)
            }
            text(3, "tile.small_entropy_battery.entropy.combined") { (chunkEntropy + batteryEntropy).toString() }
            text(21, "entropy.chunk.available") { chunkEntropy.toString() }
            text(39, "entropy.storage.available") { batteryEntropy.toString() }
            text(57, null) { if (linked) "entropy.storage_reference.linked.true" else "entropy.storage_reference.linked.false" }

            val linkButton = Btn().leftRel(0.5f).top(75).size(120, 16).overlay(LangKey("tile.small_entropy_battery.entropy.link"))
            linkButton.onMousePressed { mouseButton ->
                if (mouseButton != 0) return@onMousePressed false
                linkDirty = true
                for (name in listOf("forced_link", "link_dirty")) {
                    (syncManager.getSyncHandler(GuiSyncManager.makeSyncKey(name, 0)) as BooleanSyncValue).setBoolValue(true, true, true)
                }
                true
            }
            panel.child(linkButton)
        }
        return panel
    }

    fun linkToChunk(force: Boolean): Boolean {
        val linked = reader.centerEntropy?.setEntropyStorageReference(storage, force) ?: false
        forcedLink = linked && force
        markDirty()
        return linked
    }

    fun isLinked() = reader.centerEntropy?.entropyStorageReference?.map { it === storage }?.orElse(false) ?: false

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)
        storage.deserializeNBT(compound.getCompoundTag("storage"))
        forcedLink = compound.getBoolean("forced_link")
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setTag("storage", storage.serializeNBT())
        compound.setBoolean("forced_link", forcedLink)
        return super.writeToNBT(compound)
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) =
        capability === CapabilityEntropyHandler.STORAGE || super.hasCapability(capability, facing)

    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
        if (capability === CapabilityEntropyHandler.STORAGE) CapabilityEntropyHandler.STORAGE.cast(storage) else super.getCapability(capability, facing)

    private class Btn : ButtonWidget<Btn>()
}
