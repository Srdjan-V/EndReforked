package io.github.srdjanv.endreforked.client

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.CommonProxy
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlowerStem
import io.github.srdjanv.endreforked.common.registry.Registries
import io.github.srdjanv.endreforked.common.registry.RegistrySync
import io.github.srdjanv.endreforked.wire.s2c.ChunkEntropy
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelBakery
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.block.statemap.StateMapperBase
import net.minecraft.item.Item
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.relauncher.Side

class ClientProxy(side: Side, registrySync: RegistrySync, val clientRegistries: Registries,
                  private val chunkEntropy: () -> ChunkEntropy?) : CommonProxy(side, registrySync) {
    init {
        components.add(Registration())
    }

    override fun registries(world: World): Registries = if (world.isRemote) clientRegistries else super.registries(world)

    override fun init(event: FMLInitializationEvent) {
        super.init(event)
        clientRegistries.rebuildAll()
    }

    override fun registerEventBus() {
        super.registerEventBus()
        MinecraftForge.EVENT_BUS.register(ParticleHandler::class.java)
        MinecraftForge.EVENT_BUS.register(TextureHandler::class.java)
        MinecraftForge.EVENT_BUS.register(EntropyHud(chunkEntropy))
    }

    override fun registerItemRenderer(item: Item, meta: Int, id: String) =
        ModelLoader.setCustomModelResourceLocation(item, meta, ModelResourceLocation(item.registryName!!, id))

    override fun registerItemRenderer(item: Item, meta: Int, postfix: String, id: String) =
        ModelLoader.setCustomModelResourceLocation(item, meta, ModelResourceLocation(item.registryName.toString() + postfix, id))

    override fun postInit(event: FMLPostInitializationEvent) {
        super.postInit(event)
        Minecraft.getMinecraft().blockColors.registerBlockColorHandler(
            { _, _, _, _ -> 0xffe580ff.toInt() },
            ModBlocks.ORGANA_FLOWER_STEM_BLOCK.get(), ModBlocks.ORGANA_FLOWER_STEM_DEAD_BLOCK.get(),
        )
    }

    override fun registerToTextureAtlas(location: ResourceLocation) = TextureHandler.registerTexture(location)

    override fun registerFileStateMapper(block: Block?, item: Item?, file: String, variantName: String) {
        val mapper = FileStateMapper(Tags.MODID, file, variantName)
        if (item != null) {
            ModelBakery.registerItemVariants(item)
            ModelLoader.setCustomMeshDefinition(item, mapper)
        }
        if (block != null) ModelLoader.setCustomStateMapper(block, mapper)
    }

    override fun registerOrganaFlowerStemStateMapper(block: BlockOrganaFlowerStem) {
        ModelLoader.setCustomStateMapper(block, object : StateMapperBase() {
            override fun getModelResourceLocation(state: IBlockState): ModelResourceLocation {
                val map = LinkedHashMap<IProperty<*>, Comparable<*>>(state.properties)
                if (state.getValue(BlockOrganaFlowerStem.FACING) != EnumFacing.UP) map.remove(BlockOrganaFlowerStem.AGE)
                return ModelResourceLocation(Block.REGISTRY.getNameForObject(state.block), getPropertyString(map))
            }
        })
    }
}
