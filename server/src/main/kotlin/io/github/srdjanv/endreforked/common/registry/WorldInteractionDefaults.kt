package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.world.BlockMatch
import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.ITEM_ENTITY
import io.github.srdjanv.endreforked.api.world.WandConversion
import io.github.srdjanv.endreforked.api.world.WorldEffect
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModFluids
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlower
import io.github.srdjanv.endreforked.common.configs.Configs
import net.minecraft.block.Block
import net.minecraft.block.BlockSandStone
import net.minecraft.block.BlockStone
import net.minecraft.block.properties.IProperty
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

/** The wand conversions and fluid interactions that ship with the mod. Called when the registries are built, after the blocks and items exist. */
object WorldInteractionDefaults {
    private fun match(b: Block, vararg properties: Pair<IProperty<*>, Comparable<*>>) = BlockMatch(b, mapOf(*properties))

    private fun wandId(name: String) = ResourceLocation(Tags.MODID, "wand/$name")

    /** The damage of the wand config plus [extra]. The config is read when the defaults are built, like before. */
    private fun damage(extra: Int) = Configs.SERVER_SIDE_CONFIGS.entropyWandUseDamage + extra

    /** The old default conversions matched the default state of the block only. */
    fun wandConversions(): List<WandConversion> {
        val sound = listOf(WorldEffect.FLINT_SOUND)
        fun simple(name: String, from: BlockMatch, to: Block) = WandConversion(wandId(name), from, to.defaultState, effects = sound)
        val flower = ModBlocks.ORGANA_FLOWER_BLOCK.get()
        fun age(from: Int) = match(flower, BlockOrganaFlower.AGE to from)
        fun grown(age: Int) = flower.defaultState.withProperty(BlockOrganaFlower.AGE, age)
        return listOf(
            simple("end_stone", match(Blocks.END_STONE), ModBlocks.ENTROPY_END_STONE.get()),
            simple("stone", match(Blocks.STONE, BlockStone.VARIANT to BlockStone.EnumType.STONE), Blocks.COBBLESTONE),
            simple("cobblestone", match(Blocks.COBBLESTONE), Blocks.GRAVEL),
            simple("gravel", match(Blocks.GRAVEL), Blocks.SAND),
            simple("sandstone", match(Blocks.SANDSTONE, BlockSandStone.TYPE to BlockSandStone.EnumType.DEFAULT), Blocks.SAND),
            simple("tallgrass", match(Blocks.TALLGRASS), Blocks.DEADBUSH),
            simple("red_flower", match(Blocks.RED_FLOWER), Blocks.DEADBUSH),
            simple("yellow_flower", match(Blocks.YELLOW_FLOWER), Blocks.DEADBUSH),
            WandConversion(wandId("log"), match(Blocks.LOG), Blocks.COAL_BLOCK.defaultState, damage(8), effects = sound),
            WandConversion(wandId("log2"), match(Blocks.LOG2), Blocks.COAL_BLOCK.defaultState, damage(8), effects = sound),
            WandConversion(wandId("organa_flower_8"), age(8), grown(9), damage(10)),
            WandConversion(wandId("organa_flower_9"), age(9), grown(10), damage(10)),
            WandConversion(
                wandId("end_magma"), match(ModBlocks.END_MAGMA_BLOCK.get()), ModBlocks.FLUID_END_MAGMA_BLOCK.get().defaultState, damage(15),
            ),
        )
    }

    fun fluidBlockInteractions(): List<FluidBlockInteraction> = listOf(
        FluidBlockInteraction(
            id = ResourceLocation(Tags.MODID, "entropy/entropy_end_stone"),
            fluid = ModFluids.ENTROPY.get(),
            match = match(Blocks.END_STONE),
            result = ModBlocks.ENTROPY_END_STONE.get().defaultState,
            chance = 5,
        ),
    )

    fun fluidEntityInteractions(): List<FluidEntityInteraction> {
        val entropy = ModFluids.ENTROPY.get()
        return listOf(
            FluidEntityInteraction(
                id = ResourceLocation(Tags.MODID, "entropy/organa_flower"),
                fluid = entropy,
                entity = ITEM_ENTITY,
                item = ItemStack(Blocks.CHORUS_FLOWER, 4),
                result = EntityResult.SpawnItem(ItemStack(ModBlocks.ORGANA_FLOWER_BLOCK.get(), 4)),
                chance = 10,
                consumeSource = true,
            ),
            FluidEntityInteraction(
                id = ResourceLocation(Tags.MODID, "entropy/fluid_organa"),
                fluid = entropy,
                entity = ITEM_ENTITY,
                item = ItemStack(ModItems.ORGANA_FRUIT.get()),
                result = EntityResult.SetFluidBlock(ModBlocks.FLUID_ORGANA_BLOCK.get().defaultState),
                chance = 20,
                consumeSource = true,
            ),
            FluidEntityInteraction(
                id = ResourceLocation(Tags.MODID, "entropy/entity_watcher"),
                fluid = entropy,
                entity = ResourceLocation("minecraft", "enderman"),
                removeEntity = true,
                result = EntityResult.SpawnEntity(ResourceLocation(Tags.MODID, "watcher")),
                consumeSource = true,
            ),
        )
    }
}
