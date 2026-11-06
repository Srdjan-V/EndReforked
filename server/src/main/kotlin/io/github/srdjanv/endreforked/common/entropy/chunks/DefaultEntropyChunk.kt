package io.github.srdjanv.endreforked.common.entropy.chunks

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage
import io.github.srdjanv.endreforked.api.util.DimPos
import io.github.srdjanv.endreforked.common.entropy.storage.DefaultWeakEntropyStorage
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.common.FMLCommonHandler
import java.util.Optional

class DefaultEntropyChunk(private val chunk: Chunk) : EntropyChunk {
    private val world = chunk.world
    private val storage = world.rand.let { DefaultWeakEntropyStorage(maxOf(1000, it.nextInt(1200)), maxOf(5, it.nextInt(10))) }
    private var reference: EntropyStorage? = null
    private var tickValidated = 0
    private var loaded = true

    override val dimPos = DimPos(world.provider.dimension, chunk.pos)

    override val isLoaded: Boolean
        get() {
            val counter = FMLCommonHandler.instance().minecraftServerInstance.tickCounter
            if (counter != tickValidated) {
                tickValidated = counter
                loaded = world.isAreaLoaded(dimPos.asBlockPos(), 0)
            }
            return loaded
        }

    override val entropyStorageReference: Optional<EntropyStorage> get() = Optional.ofNullable(reference)

    override fun setEntropyStorageReference(reference: EntropyStorage?, force: Boolean): Boolean {
        if (reference != null && this.reference === reference) return true
        if (this.reference == null || force) {
            this.reference = reference
            return true
        }
        return false
    }

    override val entropyStorage: Optional<EntropyStorage> get() = Optional.of(storage)

    override var loadFactor by storage::loadFactor
    override var decay by storage::decay
    override val isOverLoaded get() = storage.isOverLoaded
    override val maxEntropy get() = storage.maxEntropy
    override val currentEntropy get() = storage.currentEntropy

    override fun induceEntropy(entropy: Int, simulate: Boolean): Int {
        var induced = storage.induceEntropy(entropy, simulate)
        val ref = reference
        if (ref != null && entropy > induced) induced += ref.induceEntropy(entropy - induced, simulate)
        if (!simulate && induced > 0) chunk.markDirty()
        return induced
    }

    override fun drainEntropy(entropy: Int, simulate: Boolean): Int {
        var drained = storage.drainEntropy(entropy, simulate)
        val ref = reference
        if (ref != null && entropy > drained) drained += ref.drainEntropy(entropy - drained, simulate)
        if (!simulate && drained > 0) chunk.markDirty()
        return drained
    }

    override fun serializeNBT(): NBTTagCompound = storage.serializeNBT()

    override fun deserializeNBT(nbt: NBTTagCompound) = storage.deserializeNBT(nbt)
}
