package io.github.srdjanv.endreforked.common.sounds

import io.github.srdjanv.endreforked.Tags
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent

class EndSound @JvmOverloads constructor(
    @JvmField val name: String,
    @JvmField val category: SoundCategory = SoundCategory.NEUTRAL,
) : SoundEvent(ResourceLocation(Tags.MODID, name)) {

    init {
        setRegistryName(ResourceLocation(Tags.MODID, name))
    }
}
