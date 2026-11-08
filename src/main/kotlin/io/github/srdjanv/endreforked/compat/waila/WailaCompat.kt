package io.github.srdjanv.endreforked.compat.waila

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.compat.CompatManger
import mcp.mobius.waila.api.*
import mcp.mobius.waila.api.IWailaPlugin
import mcp.mobius.waila.api.IWailaRegistrar
import mcp.mobius.waila.api.WailaPlugin
import net.minecraft.block.Block
import net.minecraft.item.ItemStack

@WailaPlugin
class WailaCompat : CompatManger.ModCompat, IWailaPlugin {
    override fun register(registrar: IWailaRegistrar) = registrar.registerBodyProvider(Provider(), Block::class.java)

    private class Provider : IWailaDataProvider {
        override fun getWailaBody(stack: ItemStack, tooltip: MutableList<String>, accessor: IWailaDataAccessor, config: IWailaConfigHandler): MutableList<String> {
            val provider = (accessor.tileEntity as? EntropyDataProvider)
                ?: (accessor.block as? EntropyDataProvider)
            provider?.formatedEntropyData?.ifPresent { tooltip.addAll(it) }
            return tooltip
        }
    }
}
