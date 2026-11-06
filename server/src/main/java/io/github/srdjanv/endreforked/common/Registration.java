package io.github.srdjanv.endreforked.common;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;


import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler;
import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler;
import io.github.srdjanv.endreforked.common.configs.Configs;
import io.github.srdjanv.endreforked.common.tiles.*;
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.FluidEntropyTile;
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.FluidOrganaTile;
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.OrganaFlowerTile;
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.OrganaWeedTile;
import io.github.srdjanv.endreforked.common.village.EndVillagerHandler;
import io.github.srdjanv.endreforked.utils.Initializer;

final class Registration implements Initializer {

    @Override
    public void registerEventBus() {
        registerThisToEventBus();
    }

    public void preInit(FMLPreInitializationEvent event) {
        CapabilityTimedFlightHandler.register();
        CapabilityEntropyHandler.register();

        GameRegistry.registerTileEntity(MaterializerTile.class,
                new ResourceLocation(Tags.MODID, "materializer_tile"));
        GameRegistry.registerTileEntity(EndForgeTile.class,
                new ResourceLocation(Tags.MODID, "endforge_Tile"));
        GameRegistry.registerTileEntity(SmallEntropyBatteryTile.class,
                new ResourceLocation(Tags.MODID, "small_entropy_battery_tile"));
        GameRegistry.registerTileEntity(EntropyChamberTile.class,
                new ResourceLocation(Tags.MODID, "entropy_chamber_tile"));

        GameRegistry.registerTileEntity(OrganaFlowerTile.class,
                new ResourceLocation(Tags.MODID, "organa_flower_tile"));
        GameRegistry.registerTileEntity(OrganaWeedTile.class,
                new ResourceLocation(Tags.MODID, "organa_weed_tile"));
        GameRegistry.registerTileEntity(FluidEntropyTile.class,
                new ResourceLocation(Tags.MODID, "fluid_entropy_tile"));
        GameRegistry.registerTileEntity(FluidOrganaTile.class,
                new ResourceLocation(Tags.MODID, "fluid_organa_tile"));
    }

    public void init(FMLInitializationEvent event) {
        registerBannerPatterns();

        // todo remove or improve
        if (Configs.SERVER_SIDE_CONFIGS.spawnNewVillagers) {
            EndVillagerHandler.initVillagerTrades();
            EndVillagerHandler.initVillagerHouse();
        }

        GameRegistry.addSmelting(ModBlocks.TUNGSTEN_ORE.get(), new ItemStack(ModItems.TUNGSTEN_INGOT.get(), 1), 1.5f);
        handleOreDictionary();
    }

    @Override
    public boolean dispose() {
        return true;
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> register) {
        class RegistryHandler {

            final IForgeRegistry<IRecipe> reg = register.getRegistry();

            private void register(String id, IRecipe recipe) {
                recipe.setRegistryName(new ResourceLocation(Tags.MODID, id));
                reg.register(recipe);
            }

            private NonNullList<Ingredient> ingOf(ItemStack... stacks) {
                NonNullList<Ingredient> list = NonNullList.create();
                for (ItemStack stack : stacks) list.add(Ingredient.fromStacks(stack));
                return list;
            }
        }
        var handler = new RegistryHandler();
        var path = ModItems.DRAGONITE_TEA.get().getRegistryName().getPath();
        handler.register(path, new ShapelessRecipes(path,
                new ItemStack(ModItems.DRAGONITE_TEA.get(), 2),
                handler.ingOf(
                        new ItemStack(Items.GLASS_BOTTLE, 2),
                        new ItemStack(ModItems.DRAGONITE_BERRIES.get()),
                        new ItemStack(ModItems.ORGANA_FRUIT.get()))));
    }

    private static void registerBannerPatterns() {
        Class<? extends Enum<?>> clazz = BannerPattern.class;
        addPattern(clazz, "rune", "run", new ItemStack(ModItems.END_RUNE.get()));
        addPattern(clazz, "end", "end", new ItemStack(Items.CHORUS_FRUIT_POPPED));
        addPattern(clazz, "pearl", "prl", new ItemStack(Items.ENDER_PEARL));
    }

    private static void handleOreDictionary() {
        OreDictionary.registerOre("ingotEndorium", ModItems.INGOT_ENDORIUM.get());

        // todo dont register a null block
        OreDictionary.registerOre("ingotTungsten", ModItems.TUNGSTEN_INGOT.get());
        OreDictionary.registerOre("nuggetTungsten", ModItems.TUNGSTEN_NUGGET.get());
        OreDictionary.registerOre("blockTungsten ", ModBlocks.TUNGSTEN_BLOCK.get());
        OreDictionary.registerOre("oreTungsten", ModBlocks.TUNGSTEN_ORE.get());
        OreDictionary.registerOre("oreTungsten", ModBlocks.TUNGSTEN_END_ORE.get());

        OreDictionary.registerOre("dustObsidian", ModItems.CATALYST.get());
        OreDictionary.registerOre("shardObsidian", ModItems.SHARD_OBSIDIAN.get());
        OreDictionary.registerOre("hammerIron", ModItems.HAMMER_IRON.get());
        OreDictionary.registerOre("hammer", ModItems.HAMMER_IRON.get());
        OreDictionary.registerOre("shardLormyte", ModItems.LORMYTE_CRYSTAL.get());
        OreDictionary.registerOre("essence", ModItems.END_ESSENCE.get());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void addPattern(Class<? extends Enum<?>> clazz, String name, String id, ItemStack craftingItem) {
        name = "endreborn_" + name;
        id = "er_" + id;
        EnumHelper.addEnum((Class) clazz, name.toUpperCase(), new Class[] { String.class, String.class, ItemStack.class }, name,
                id, craftingItem);
    }
}
