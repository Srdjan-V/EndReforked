package io.github.srdjanv.endreforked.compat.baubles;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.base.Suppliers;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.compat.CompatManger;
import io.github.srdjanv.endreforked.utils.PlayerUtils;
import io.github.srdjanv.endreforked.utils.models.IAsset;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BaublesCompat implements CompatManger.ModCompat {

    public static final Supplier<EntropyWingsRing> ENTROPY_WINGS_RING = Suppliers.memoize(EntropyWingsRing::new);

    public BaublesCompat() {
        PlayerUtils.PLAYER_INVENTORIES.add(player -> {
            IBaublesItemHandler itemHandler = BaublesApi.getBaublesHandler(player);
            if (itemHandler == null) return Stream.of();
            List<ItemStack> stackList = new ObjectArrayList<>(itemHandler.getSlots());
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                stackList.add(itemHandler.getStackInSlot(i));
            }
            return stackList.stream();
        });
    }

    @SubscribeEvent
    public void onRecipeRegister(RegistryEvent.Register<IRecipe> event) {
        var registry = event.getRegistry();
        registry.register(init(ENTROPY_WINGS_RING.get(), ModItems.ENTROPY_WINGS.get()));
        registry.register(init(ModItems.ENTROPY_WINGS.get(), ENTROPY_WINGS_RING.get()));
    }

    private IRecipe init(Item in, Item out) {
        NonNullList<Ingredient> items = NonNullList.create();
        items.add(Ingredient.fromItem(out));
        ShapelessRecipes recipe = new ShapelessRecipes(in.getRegistryName().getPath(), new ItemStack(in), items);
        recipe.setRegistryName(new ResourceLocation(Tags.MODID, recipe.getGroup()));
        return recipe;
    }

    @SubscribeEvent
    public void onItemRegister(RegistryEvent.Register<Item> event) {
        var registry = event.getRegistry();
        Stream.of(ENTROPY_WINGS_RING)
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .forEach(registry::register);
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        Stream.of(ENTROPY_WINGS_RING)
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .filter(item -> item instanceof IAsset)
                .map(item -> (IAsset) item)
                .forEach(IAsset::handleAssets);
    }

    @Override
    public void registerEventBus() {
        registerThisToEventBus();
    }

    @Override
    public String modID() {
        return "baubles";
    }
}
