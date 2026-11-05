package io.github.srdjanv.endreforked.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.CommonProxy;
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlowerStem;

public class ClientProxy extends CommonProxy {

    public ClientProxy(Side side) {
        super(side);
        components.add(new Registration());
    }

    @Override
    public void registerEventBus() {
        super.registerEventBus();
        MinecraftForge.EVENT_BUS.register(ParticleHandler.class);
        MinecraftForge.EVENT_BUS.register(TextureHandler.class);
    }

    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        ColorsHandler.init();
    }

    @Override
    public void registerItemRenderer(Item item, int meta, String postfix, String id) {
        ModelLoader.setCustomModelResourceLocation(item, meta,
                new ModelResourceLocation(item.getRegistryName() + postfix, id));
    }

    @Override
    public void registerToTextureAtlas(ResourceLocation location) {
        TextureHandler.registerTexture(location);
    }

    @Override
    public void registerFileStateMapper(
                                        @Nullable Block block,
                                        @Nullable Item item,
                                        String file, String variantName) {
        FileStateMapper mapper = new FileStateMapper(Tags.MODID, file, variantName);
        if (item != null) {
            ModelBakery.registerItemVariants(item);
            ModelLoader.setCustomMeshDefinition(item, mapper);
        }
        if (block != null) {
            ModelLoader.setCustomStateMapper(block, mapper);
        }
    }

    @Override
    public void registerOrganaFlowerStemStateMapper(BlockOrganaFlowerStem block) {
        StateMappers.initOrganaFlowerStem(block);
    }
}
