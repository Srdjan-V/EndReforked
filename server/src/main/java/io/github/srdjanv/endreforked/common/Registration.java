package io.github.srdjanv.endreforked.common;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import com.google.common.collect.Lists;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.api.entropy.wand.EntropyWandHandler;
import io.github.srdjanv.endreforked.api.entropy.wand.WorldConversion;
import io.github.srdjanv.endreforked.api.fluids.base.CollisionRecipe;
import io.github.srdjanv.endreforked.api.fluids.base.EntityFluidRecipe;
import io.github.srdjanv.endreforked.api.fluids.base.EntityFluidRecipeResult;
import io.github.srdjanv.endreforked.api.fluids.base.EntityMather;
import io.github.srdjanv.endreforked.api.fluids.entropy.EntropyFluidAnyStateCollisionHandler;
import io.github.srdjanv.endreforked.api.fluids.entropy.EntropyFluidEntityCollisionHandler;
import io.github.srdjanv.endreforked.api.util.EntityMatchStrategy;
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlower;
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler;
import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler;
import io.github.srdjanv.endreforked.common.configs.Configs;
import io.github.srdjanv.endreforked.common.entity.EntityWatcher;
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
        registerEntropyWandRecipes();
        registerBannerPatterns();

        // todo remove or improve
        if (Configs.SERVER_SIDE_CONFIGS.spawnNewVillagers) {
            EndVillagerHandler.initVillagerTrades();
            EndVillagerHandler.initVillagerHouse();
        }

        GameRegistry.addSmelting(ModBlocks.TUNGSTEN_ORE.get(), new ItemStack(ModItems.TUNGSTEN_INGOT.get(), 1), 1.5f);
        handleOreDictionary();

        EntropyFluidAnyStateCollisionHandler.INSTANCE.registerRecipe(CollisionRecipe.<Block, IBlockState>builder()
                .withRegistryName("entropy_end_stone")
                .withInput(Blocks.END_STONE)
                .withChance(5)
                .withConsumeSource(false)
                .withRecipeFunction(block -> ModBlocks.ENTROPY_END_STONE.get().getDefaultState())
                .build());

        EntropyFluidEntityCollisionHandler.INSTANCE.register(EntityFluidRecipe.<EntityItem, EntityItem>builder()
                .withRegistryName("organa_flower")
                .withClazz(EntityItem.class)
                .withChance(10)
                .withConsumeSource(true)
                .withEntityMather(EntityMather.<EntityItem>builder()
                        .withEntityBuilder(world -> {
                            var entity = new EntityItem(world);
                            entity.setItem(new ItemStack(Blocks.CHORUS_FLOWER, 4));
                            return entity;
                        })
                        .withMatchStrategy(EntityMatchStrategy.memorizedComparingEntityItemAllButCount())
                        .build())
                .withRemoveEntity(false)
                .withEntityFunction((world, entity) -> {
                    var itemStack = entity.getItem();
                    if (itemStack.getItem() != Item.getItemFromBlock(Blocks.CHORUS_FLOWER)) return null;
                    if (itemStack.getCount() < 4) return null;
                    itemStack.setCount(itemStack.getCount() - 4);
                    var item = new EntityItem(world);
                    item.setItem(new ItemStack(ModBlocks.ORGANA_FLOWER_BLOCK.get(), 4));
                    return EntityFluidRecipeResult.ofEntity(item);
                })
                .build());

        EntropyFluidEntityCollisionHandler.INSTANCE.register(EntityFluidRecipe.<EntityItem, EntityItem>builder()
                .withRegistryName("fluid_organa")
                .withClazz(EntityItem.class)
                .withChance(20)
                .withConsumeSource(true)
                .withEntityMather(EntityMather.<EntityItem>builder()
                        .withEntityBuilder(world -> {
                            var entity = new EntityItem(world);
                            entity.setItem(new ItemStack(ModItems.ORGANA_FRUIT.get()));
                            return entity;
                        })
                        .withMatchStrategy(EntityMatchStrategy.memorizedComparingEntityItemAllButCount())
                        .build())
                .withRemoveEntity(false)
                .withEntityFunction((world, entity) -> {
                    var itemStack = entity.getItem();
                    if (itemStack.getItem() != ModItems.ORGANA_FRUIT.get()) return null;
                    if (itemStack.getCount() < 1) return null;
                    itemStack.setCount(itemStack.getCount() - 1);
                    return EntityFluidRecipeResult.ofState(ModBlocks.FLUID_ORGANA_BLOCK.get().getDefaultState());
                })
                .build());

        EntropyFluidEntityCollisionHandler.INSTANCE.register(EntityFluidRecipe.<EntityEnderman, EntityWatcher>builder()
                .withRegistryName("entity_watcher")
                .withClazz(EntityEnderman.class)
                .withConsumeSource(true)
                .withEntityMather(EntityMather.<EntityEnderman>builder()
                        .withEntityBuilder(EntityEnderman::new)
                        .withMatchStrategy(EntityMatchStrategy.comparingBaseType())
                        .build())
                .withRemoveEntity(true)
                .withEntityFunction((world, entity) -> {
                    var watcher = new EntityWatcher(world);
                    return EntityFluidRecipeResult.ofEntity(watcher);
                })
                .build());
    }

    @Override
    public boolean dispose() {
        return true;
    }

    private static void registerEntropyWandRecipes() {
        EntropyWandHandler.registerDefaultStateConversion(Blocks.END_STONE, ModBlocks.ENTROPY_END_STONE.get());
        EntropyWandHandler.registerDefaultStateConversion(Blocks.STONE, Blocks.COBBLESTONE);
        EntropyWandHandler.registerDefaultStateConversion(Blocks.COBBLESTONE, Blocks.GRAVEL);
        EntropyWandHandler.registerDefaultStateConversion(Blocks.GRAVEL, Blocks.SAND);
        EntropyWandHandler.registerDefaultStateConversion(Blocks.SANDSTONE, Blocks.SAND);
        EntropyWandHandler.registerConversions(
                Lists.newArrayList(Blocks.TALLGRASS, Blocks.RED_FLOWER, Blocks.YELLOW_FLOWER),
                WorldConversion.builder()
                        .matcherAny()
                        .newState(Blocks.DEADBUSH)
                        .playFlintSound()
                        .build());
        EntropyWandHandler.registerConversions(
                Lists.newArrayList(Blocks.LOG, Blocks.LOG2),
                WorldConversion.builder()
                        .matcherAny()
                        .newState(Blocks.COAL_BLOCK)
                        .addItemDamage(8)
                        .playFlintSound()
                        .build());

        EntropyWandHandler.registerConversions(ModBlocks.ORGANA_FLOWER_BLOCK.get(),
                WorldConversion.builder()
                        .matcher(input -> input.getValue(BlockOrganaFlower.AGE) == 8)
                        .newState(() -> ModBlocks.ORGANA_FLOWER_BLOCK.get().getDefaultState()
                                .withProperty(BlockOrganaFlower.AGE, 9))
                        .addItemDamage(10)
                        .build(),
                WorldConversion.builder()
                        .matcher(input -> input.getValue(BlockOrganaFlower.AGE) == 9)
                        .newState(() -> ModBlocks.ORGANA_FLOWER_BLOCK.get().getDefaultState()
                                .withProperty(BlockOrganaFlower.AGE, 10))
                        .addItemDamage(10)
                        .build());

        EntropyWandHandler.registerConversions(ModBlocks.END_MAGMA_BLOCK.get(),
                WorldConversion.builder()
                        .matcher(ModBlocks.END_MAGMA_BLOCK.get())
                        .newState(ModBlocks.FLUID_END_MAGMA_BLOCK.get())
                        .addItemDamage(15)
                        .build());
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
