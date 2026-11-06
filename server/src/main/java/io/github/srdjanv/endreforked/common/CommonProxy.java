package io.github.srdjanv.endreforked.common;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlowerStem;
import io.github.srdjanv.endreforked.api.entropy.world.EntropyWorldHandler;
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler;
import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler;
import io.github.srdjanv.endreforked.common.configs.JsonConfigs;
import io.github.srdjanv.endreforked.common.configs.bioms.EndBiomesConfig;
import io.github.srdjanv.endreforked.common.configs.content.DisabledContentConfig;
import io.github.srdjanv.endreforked.common.configs.mobs.MobConfig;
import io.github.srdjanv.endreforked.common.configs.worldgen.GenericGenConfig;
import io.github.srdjanv.endreforked.common.configs.worldgen.OreGenConfig;
import io.github.srdjanv.endreforked.common.configs.worldgen.StructureGenConfig;
import io.github.srdjanv.endreforked.common.datafixers.Fixer;
import io.github.srdjanv.endreforked.common.handlers.EventHandler;
import io.github.srdjanv.endreforked.common.handlers.TimedFlightHandler;
import io.github.srdjanv.endreforked.common.registry.Registries;
import io.github.srdjanv.endreforked.common.registry.RegistrySync;
import io.github.srdjanv.endreforked.compat.CompatManger;
import io.github.srdjanv.endreforked.utils.Initializer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CommonProxy {

    public final Side SIDE;
    public final RegistrySync registrySync;
    protected final List<Initializer> components = new ObjectArrayList<>();

    public CommonProxy(Side SIDE, RegistrySync registrySync) {
        this.SIDE = SIDE;
        this.registrySync = registrySync;
        loadStaticCLasses();
        components.add(CompatManger.getInstance());
        components.add(Fixer.getInstance());
        components.add(new Registration());
        components.add(new LootHandler());
        components.addAll(JsonConfigs.getConfigs());
        components.add(TimedFlightHandler.getInstance());
        components.add(new ModGameRules());
    }

    protected void loadStaticCLasses() {
        MobConfig.getInstance();
        DisabledContentConfig.getInstance();
        EndBiomesConfig.getInstance();
        GenericGenConfig.getInstance();
        OreGenConfig.getInstance();
        StructureGenConfig.getInstance();
    }

    /** The registries a world reads: the client copy for a client world, else the server ones. */
    public Registries registries(World world) {
        return registrySync.getRegistries();
    }

    public void registerEventBus() {
        MinecraftForge.EVENT_BUS.register(registrySync);
        for (Initializer component : components) component.registerEventBus();
        MinecraftForge.EVENT_BUS.register(CapabilityTimedFlightHandler.class);
        MinecraftForge.EVENT_BUS.register(CapabilityEntropyHandler.class);
        MinecraftForge.EVENT_BUS.register(ModFluids.class);
        MinecraftForge.EVENT_BUS.register(ModBlocks.class);
        MinecraftForge.EVENT_BUS.register(ModItems.class);
        MinecraftForge.EVENT_BUS.register(ModEnchants.class);
        MinecraftForge.EVENT_BUS.register(ModSounds.class);
        MinecraftForge.EVENT_BUS.register(ModPotions.class);
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        MinecraftForge.EVENT_BUS.register(ModBioms.class);
        MinecraftForge.EVENT_BUS.register(EntropyWorldHandler.class);
    }

    public void preInit(FMLPreInitializationEvent event) {
        for (Initializer component : components) component.preInit(event);
    }

    public void init(FMLInitializationEvent event) {
        for (Initializer component : components) component.init(event);
        // After the components: the mobs and the other content they register must exist for the id checks.
        registrySync.build();
    }

    public void postInit(FMLPostInitializationEvent event) {
        for (Initializer component : components) component.postInit(event);
        dispose();
    }

    public void onServerStarted(FMLServerStartedEvent event) {
        for (Initializer component : components) component.onServerStarted(event);
    }

    public void onServerStopped(FMLServerStoppedEvent event) {
        for (Initializer component : components) component.onServerStopped(event);
    }

    private void dispose() {
        for (Iterator<Initializer> iterator = components.iterator(); iterator.hasNext();) {
            Initializer component = iterator.next();
            if (component.dispose()) {
                component.onDispose();
                iterator.remove();
            }
        }
    }

    public void registerItemRenderer(Item item, int meta, String id) {}

    public void registerItemRenderer(Item item, int meta, String postfix, String id) {}

    public void registerToTextureAtlas(ResourceLocation location) {}

    public void registerFileStateMapper(Block block, String file, String variantName) {
        registerFileStateMapper(block, Item.getItemFromBlock(block), file, variantName);
    }

    public void registerFileStateMapper(Item item, String file, String variantName) {
        registerFileStateMapper(Block.getBlockFromItem(item), file, variantName);
    }

    public void registerFileStateMapper(@Nullable Block block, @Nullable Item item, String file, String variantName) {}

    public void registerOrganaFlowerStemStateMapper(BlockOrganaFlowerStem block) {}
}
