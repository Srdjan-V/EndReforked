package io.github.srdjanv.endreforked.common.capabilities.entropy

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage
import io.github.srdjanv.endreforked.api.capabilities.entropy.WeakEntropyStorage
import io.github.srdjanv.endreforked.common.entropy.storage.DefaultEntropyStorage
import io.github.srdjanv.endreforked.common.entropy.storage.DefaultWeakEntropyStorage
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagInt
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.Callable

object CapabilityEntropyHandler {
    // Forge injects these static fields
    @CapabilityInject(EntropyStorage::class)
    lateinit var STORAGE: Capability<EntropyStorage>

    @CapabilityInject(WeakEntropyStorage::class)
    lateinit var WEEK_STORAGE: Capability<WeakEntropyStorage>

    @CapabilityInject(EntropyChunk::class)
    lateinit var ENTROPY_CHUNK: Capability<EntropyChunk>

    @JvmField
    val ENTROPY_CHUNK_STORAGE_LOC = ResourceLocation(Tags.MODID, "chunk_entropy_storage")

    @SubscribeEvent
    @JvmStatic
    fun attachChunkEntropyCap(event: AttachCapabilitiesEvent<Chunk>) {
        if (!event.`object`.world.isRemote) event.addCapability(ENTROPY_CHUNK_STORAGE_LOC, ChunkEntropyProvider(event.`object`))
    }

    fun register() {
        CapabilityManager.INSTANCE.register(EntropyStorage::class.java, object : Capability.IStorage<EntropyStorage> {
            override fun writeNBT(capability: Capability<EntropyStorage>, instance: EntropyStorage, side: EnumFacing?): NBTBase =
                NBTTagInt(instance.currentEntropy)

            override fun readNBT(capability: Capability<EntropyStorage>, instance: EntropyStorage, side: EnumFacing?, nbt: NBTBase) {
                require(instance is DefaultEntropyStorage) { "Can not deserialize to an instance that isn't the default implementation" }
                instance.currentEntropy = (nbt as NBTTagInt).int
            }
        }) { DefaultEntropyStorage(1_000) }

        CapabilityManager.INSTANCE.register(WeakEntropyStorage::class.java, object : Capability.IStorage<WeakEntropyStorage> {
            override fun writeNBT(capability: Capability<WeakEntropyStorage>, instance: WeakEntropyStorage, side: EnumFacing?): NBTBase =
                NBTTagCompound().apply {
                    setInteger("entropy", instance.currentEntropy)
                    setInteger("decay", instance.decay)
                }

            override fun readNBT(capability: Capability<WeakEntropyStorage>, instance: WeakEntropyStorage, side: EnumFacing?, nbt: NBTBase) {
                require(instance is DefaultWeakEntropyStorage) { "Can not deserialize to an instance that isn't the default implementation" }
                nbt as NBTTagCompound
                instance.currentEntropy = nbt.getInteger("entropy")
                instance.decay = nbt.getInteger("decay")
            }
        }) { DefaultWeakEntropyStorage(1_000, 10) }

        CapabilityManager.INSTANCE.register(EntropyChunk::class.java, object : Capability.IStorage<EntropyChunk> {
            override fun writeNBT(capability: Capability<EntropyChunk>, instance: EntropyChunk, side: EnumFacing?): NBTBase =
                instance.serializeNBT()

            override fun readNBT(capability: Capability<EntropyChunk>, instance: EntropyChunk, side: EnumFacing?, nbt: NBTBase) =
                instance.deserializeNBT(nbt as NBTTagCompound)
        }, Callable<EntropyChunk?> { null })
    }
}
