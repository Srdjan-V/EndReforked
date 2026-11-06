package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlowerStem
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler
import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler
import io.github.srdjanv.endreforked.common.datafixers.Fixer
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import io.github.srdjanv.endreforked.common.handlers.EventHandler
import io.github.srdjanv.endreforked.common.handlers.TimedFlightHandler
import io.github.srdjanv.endreforked.common.registry.Registries
import io.github.srdjanv.endreforked.common.registry.RegistrySync
import io.github.srdjanv.endreforked.common.worldgen.WorldGen
import io.github.srdjanv.endreforked.compat.CompatManger
import io.github.srdjanv.endreforked.utils.Initializer
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.*
import net.minecraftforge.fml.relauncher.Side

open class CommonProxy(@JvmField val SIDE: Side, @JvmField val registrySync: RegistrySync) {
    protected val components: MutableList<Initializer> = mutableListOf(
        CompatManger.getInstance(), Fixer, Registration(), LootHandler(), ModEntities, TimedFlightHandler, ModGameRules(),
    )

    /** The registries a world reads: the client copy for a client world, else the server ones. */
    open fun registries(world: World): Registries = registrySync.registries

    open fun registerEventBus() {
        MinecraftForge.EVENT_BUS.register(registrySync)
        components.forEach { it.registerEventBus() }
        listOf<Any>(
            CapabilityTimedFlightHandler::class.java, CapabilityEntropyHandler::class.java, ModFluids::class.java,
            ModBlocks::class.java, ModItems::class.java, ModEnchants::class.java, ModSounds::class.java,
            ModPotions::class.java, EventHandler::class.java, ModBioms::class.java, EntropyWorldHandler::class.java,
        ).forEach(MinecraftForge.EVENT_BUS::register)
    }

    open fun preInit(event: FMLPreInitializationEvent) = components.forEach { it.preInit(event) }

    open fun init(event: FMLInitializationEvent) = components.forEach { it.init(event) }

    /**
     * The registries are built here, not in init: GroovyScript runs the scripts of the post init loader after the post init of all mods
     * and before this event. The mobs and the other content must exist for the id checks, and the scripts are in the script layers now.
     */
    open fun loadComplete(event: FMLLoadCompleteEvent) {
        registrySync.build()
        WorldGen.install(registrySync.registries, EndReforked.LOGGER)
    }

    open fun postInit(event: FMLPostInitializationEvent) {
        components.forEach { it.postInit(event) }
        components.removeAll { it.dispose().also { dispose -> if (dispose) it.onDispose() } }
    }

    open fun onServerStarted(event: FMLServerStartedEvent) = components.forEach { it.onServerStarted(event) }

    open fun onServerStopped(event: FMLServerStoppedEvent) = components.forEach { it.onServerStopped(event) }

    open fun registerItemRenderer(item: Item, meta: Int, id: String) {}

    open fun registerItemRenderer(item: Item, meta: Int, postfix: String, id: String) {}

    open fun registerToTextureAtlas(location: ResourceLocation) {}

    fun registerFileStateMapper(block: Block, file: String, variantName: String) =
        registerFileStateMapper(block, Item.getItemFromBlock(block), file, variantName)

    fun registerFileStateMapper(item: Item, file: String, variantName: String) =
        registerFileStateMapper(Block.getBlockFromItem(item), file, variantName)

    open fun registerFileStateMapper(block: Block?, item: Item?, file: String, variantName: String) {}

    open fun registerOrganaFlowerStemStateMapper(block: BlockOrganaFlowerStem) {}
}
