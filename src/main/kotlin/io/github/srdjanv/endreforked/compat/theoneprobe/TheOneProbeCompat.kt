package io.github.srdjanv.endreforked.compat.theoneprobe

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.compat.CompatManger
import mcjty.theoneprobe.api.*
import mcjty.theoneprobe.TheOneProbe
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraft.block.state.IBlockState

class TheOneProbeCompat : CompatManger.ModCompat {
    override fun init(event: net.minecraftforge.fml.common.event.FMLInitializationEvent) {
        TheOneProbe.theOneProbeImp.registerProvider(Provider())
    }

    private class Provider : IProbeInfoProvider {
        override fun getID() = "endreforked:entropy"
        override fun addProbeInfo(mode: ProbeMode, info: IProbeInfo, player: EntityPlayer, world: World, state: IBlockState, data: IProbeHitData) {
            val provider = (world.getTileEntity(data.pos) as? EntropyDataProvider)
                ?: (state.block as? EntropyDataProvider)
            provider?.formatedEntropyData?.ifPresent { lines -> lines.forEach(info::text) }
        }
    }
}
