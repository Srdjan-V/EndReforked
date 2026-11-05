package io.github.srdjanv.endreforked.common.machine

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.impl.machine.MachineRecipes
import net.minecraft.tileentity.TileEntity

private val NO_RECIPES = MachineRecipes(emptyList())

/** The machine recipes this tile reads. A client world reads the client registry, never the server one. */
fun TileEntity.recipes(): MachineRecipes {
    val world = world ?: return NO_RECIPES
    return EndReforked.getProxy().registries(world).machineRecipes.current
}
