package io.github.srdjanv.endreforked.common;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.base.Suppliers;

import io.github.srdjanv.endreforked.common.bioms.EntropyBiome;
import io.github.srdjanv.endreforked.common.bioms.OrganaBiome;
import io.github.srdjanv.endreforked.common.bioms.base.BiomeDictionaryHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ModBioms {

    public static final List<Supplier<? extends Biome>> END_BIOMES = new ObjectArrayList<>();

    public static final Supplier<OrganaBiome> ORGANA_BIOME = regEnd(OrganaBiome::new);
    public static final Supplier<EntropyBiome> ENTROPY_BIOME = regEnd(EntropyBiome::new);

    private static <B extends Biome> Supplier<B> regEnd(com.google.common.base.Supplier<B> sup) {
        sup = Suppliers.memoize(sup);
        END_BIOMES.add(sup);
        return sup;
    }

    @SubscribeEvent
    public static void onBiomeRegister(RegistryEvent.Register<Biome> event) {
        var registry = event.getRegistry();
        END_BIOMES.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .peek(registry::register)
                .forEach(biome -> {
                    if (biome instanceof BiomeDictionaryHandler handler)
                        handler.registerToBiomeDictionary();
                });
    }
}
