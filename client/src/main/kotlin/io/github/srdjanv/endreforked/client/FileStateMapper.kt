package io.github.srdjanv.endreforked.client

import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.ItemMeshDefinition
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.block.statemap.StateMapperBase
import net.minecraft.item.ItemStack

class FileStateMapper(modName: String, fileName: String, modelName: String) : StateMapperBase(), ItemMeshDefinition {
    private val location = ModelResourceLocation("$modName:$fileName", modelName)

    override fun getModelResourceLocation(state: IBlockState) = location

    override fun getModelLocation(stack: ItemStack) = location
}
