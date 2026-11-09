package io.github.srdjanv.endreforked;

import io.github.srdjanv.endreforked.common.CommonProxy;
import io.github.srdjanv.endreforked.common.comands.EndRebornCommands;
import io.github.srdjanv.endreforked.common.network.Network;
import io.github.srdjanv.endreforked.compat.CompatManger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

@Mod(modid = Tags.MODID, name = Tags.MODNAME, version = Tags.VERSION, dependencies = Tags.MOD_DEPS)
public class EndReforked {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    public static final CreativeTabs ENDERTAB = new CreativeTabs("endertab") {

        @Override
        public @NotNull
        ItemStack createIcon() {
            return new ItemStack(Items.ENDER_PEARL);
        }

    };

    public static Network NET;

    public static EndReforked instance;

    private static CommonProxy proxy;

    static {
        FluidRegistry.enableUniversalBucket();
    }

    public EndReforked() {
        instance = this;
        Side side = FMLCommonHandler.instance().getSide();
        ModServices services = new ModServices(side, LOGGER);
        NET = services.getNet();
        proxy = side.isClient() ? newClientProxy(services) : services.getServerProxy();
        proxy.registerEventBus();
        MinecraftForge.EVENT_BUS.register(EndReforked.class);
    }

    public static CommonProxy getProxy() {
        return Objects.requireNonNull(proxy);
    }

    // The client module is not visible to this module, so the client services are created by name
    private static CommonProxy newClientProxy(ModServices services) {
        try {
            return (CommonProxy) Class.forName("io.github.srdjanv.endreforked.client.di.ClientServices")
                .getMethod("create", ModServices.class)
                .invoke(null, services);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create the client services", e);
        }
    }

    @SubscribeEvent
    public static void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Tags.MODID)) {
            ConfigManager.sync(Tags.MODID, Config.Type.INSTANCE);
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
        CompatManger.invalidate();
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete(event);
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        proxy.onServerStarted(event);
    }

    @EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        proxy.onServerStopped(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent evt) {
        evt.registerServerCommand(new EndRebornCommands());
    }

}
