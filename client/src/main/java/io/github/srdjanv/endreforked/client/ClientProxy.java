package io.github.srdjanv.endreforked.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.CommonProxy;
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlowerStem;
import io.github.srdjanv.endreforked.common.network.servertoclient.PlayerParticlePacket;

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

    @Override
    public void handlePlayerParticle(PlayerParticlePacket message) {
        if (message.remove()) {
            ParticleHandler.removeFromRender(message.id());
        } else {
            final var particle = message.particleType();
            ParticleHandler.addToRender(
                    new ParticleHandler.RenderInfo(message.id(), message.renderIterations()) {

                        @Override
                        public void render() {
                            int color = 0x5900b3;
                            double xSpeed = (double) (color >> 16 & 255) / 255.0D;
                            double ySpeed = (double) (color >> 8 & 255) / 255.0D;
                            double zSpeed = (double) (color >> 0 & 255) / 255.0D;

                            var mc = Minecraft.getMinecraft();
                            var player = mc.player;
                            mc.world.spawnParticle(particle,
                                    player.posX + (player.getRNG().nextDouble() - 0.5D) * (double) player.width,
                                    player.posY + player.getRNG().nextDouble() * (double) player.height,
                                    player.posZ + (player.getRNG().nextDouble() - 0.5D) * (double) player.width,
                                    xSpeed, ySpeed, zSpeed);
                        }
                    });
        }
    }

    @Override
    public void handleTpParticle(double posX, double posY, double posZ, double newPosX, double newPosY, double newPosZ,
                                 double width, double height) {
        ParticleHandler.addToRender(
                new ParticleHandler.RenderInfo("TPParticlePacket" + Minecraft.getSystemTime(), 1) {

                    @Override
                    public void render() {
                        var mc = Minecraft.getMinecraft();
                        var random = mc.world.rand;
                        for (int j = 0; j < 128; ++j) {
                            double d6 = (double) j / 127.0D;
                            float f = (random.nextFloat() - 0.5F) * 0.2F;
                            float f1 = (random.nextFloat() - 0.5F) * 0.2F;
                            float f2 = (random.nextFloat() - 0.5F) * 0.2F;
                            double d3 = posX + (newPosX - posX) * d6 +
                                    (random.nextDouble() - 0.5D) * width * 2.0D;
                            double d4 = posY + (newPosY - posY) * d6 +
                                    random.nextDouble() * height;
                            double d5 = posZ + (newPosZ - posZ) * d6 +
                                    (random.nextDouble() - 0.5D) * width * 2.0D;
                            mc.world.spawnParticle(EnumParticleTypes.PORTAL, d3, d4, d5, f, f1, f2);
                        }
                    }
                });
    }
}
