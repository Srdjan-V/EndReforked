package io.github.srdjanv.endreforked.common;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.base.Suppliers;

import io.github.srdjanv.endreforked.common.fluids.FluidEndMagma;
import io.github.srdjanv.endreforked.common.fluids.FluidEntropy;
import io.github.srdjanv.endreforked.common.fluids.FluidOrgana;
import io.github.srdjanv.endreforked.utils.models.IAsset;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ModFluids {

    private static final List<Supplier<? extends Fluid>> FLUIDS = new ObjectArrayList<>();

    public static final Supplier<FluidEndMagma> END_MAGMA = register(FluidEndMagma::new);
    public static final Supplier<FluidEntropy> ENTROPY = register(FluidEntropy::new);
    public static final Supplier<FluidOrgana> ORGANA = register(FluidOrgana::new);

    public static <F extends Fluid> Supplier<F> register(com.google.common.base.Supplier<F> supplier) {
        Supplier<F> memorized = Suppliers.memoize(() -> {
            var fluid = supplier.get();
            // if (fluid instanceof IAsset model) model.handleAssets();
            FluidRegistry.registerFluid(fluid);
            return fluid;
        });
        FLUIDS.add(memorized);
        return memorized;
    }

    @SubscribeEvent
    static void registerModels(ModelRegistryEvent event) {
        FLUIDS.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .filter(item -> item instanceof IAsset)
                .map(item -> (IAsset) item)
                .forEach(IAsset::handleAssets);
    }

    @SubscribeEvent
    static void registerItems(RegistryEvent.Register<Item> event) {
        for (Supplier<? extends Fluid> fluid : FLUIDS) {
            FluidRegistry.addBucketForFluid(fluid.get());
        }
    }
}
