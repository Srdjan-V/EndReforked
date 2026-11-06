package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.entity.*
import io.github.srdjanv.endreforked.utils.Initializer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EnumCreatureType
import net.minecraft.init.Biomes
import net.minecraft.util.ResourceLocation
import net.minecraft.world.biome.Biome
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.registry.EntityRegistry

object ModEntities : Initializer {
    private class Spawn(val biome: () -> Biome, val weight: Int, val min: Int, val max: Int)

    private class Def(
        val name: String, val cls: Class<out Entity>, val id: Int, val range: Int, val freq: Int,
        val velocity: Boolean, val egg: Pair<Int, Int>?, val spawns: List<Spawn>,
    )

    private fun end(weight: Int, min: Int, max: Int) = listOf(
        Spawn({ ModBioms.ORGANA_BIOME.get() }, weight, min, max),
        Spawn({ ModBioms.ENTROPY_BIOME.get() }, weight, min, max),
    )

    private val defs = listOf(
        Def("endguard", EntityEndGuard::class.java, 0, 64, 3, false, 9654933 to 11237052,
            listOf(Spawn({ Biomes.SKY }, 4, 1, 2)) + end(5, 2, 4)),
        Def("entropy_fireball", EntityEntropyFireball::class.java, 1, 30, 1, true, null, emptyList()),
        Def("watcher", EntityWatcher::class.java, 2, 64, 3, false, 461076 to 2236447,
            end(10, 1, 3) + listOf(Spawn({ Biomes.SKY }, 10, 1, 3), Spawn({ Biomes.PLAINS }, 4, 1, 1))),
        Def("chronologist", EntityChronologist::class.java, 4, 64, 3, false, 461076 to 13680725,
            end(5, 1, 4) + listOf(Spawn({ Biomes.SKY }, 4, 1, 3))),
    )

    override fun init(event: FMLInitializationEvent) {
        for (d in defs) {
            val rl = ResourceLocation(Tags.MODID, d.name)
            if (d.egg != null) {
                EntityRegistry.registerModEntity(rl, d.cls, d.name, d.id, EndReforked.instance, d.range, d.freq,
                    d.velocity, d.egg.first, d.egg.second)
            } else {
                EntityRegistry.registerModEntity(rl, d.cls, d.name, d.id, EndReforked.instance, d.range, d.freq,
                    d.velocity)
            }
            for (s in d.spawns) EntityRegistry.addSpawn(d.cls.asSubclass(EntityLiving::class.java), s.weight, s.min, s.max, EnumCreatureType.MONSTER, s.biome())
        }
    }
}
