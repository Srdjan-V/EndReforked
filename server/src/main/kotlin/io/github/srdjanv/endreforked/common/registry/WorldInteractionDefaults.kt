package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.machine.BlockSpec
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.registry.Id
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
import io.github.srdjanv.endreforked.common.registry.GameAdapter.id
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.Item

/** The wand conversions and fluid interactions that ship with the mod. Called when the registries are built, after the blocks and items exist. */
object WorldInteractionDefaults {
    private fun block(b: Block, vararg properties: Pair<String, String>) = BlockSpec(b.id(), mapOf(*properties))

    private fun wandId(name: String) = Id(Tags.MODID, "wand/$name")

    /** The damage of the wand config plus [extra]. The config is read when the defaults are built, like before. */
    private fun damage(extra: Int) = Configs.SERVER_SIDE_CONFIGS.entropyWandUseDamage + extra

    /** The old default conversions matched the default state of the block only. */
    fun wandConversions(): List<WandConversion> {
        val sound = listOf(WorldEffect.FLINT_SOUND)
        fun simple(name: String, from: BlockSpec, to: Block) = WandConversion(wandId(name), from, block(to), effects = sound)
        val flower = ModBlocks.ORGANA_FLOWER_BLOCK.get()
        fun age(from: Int) = block(flower, BlockOrganaFlower.AGE.name to "$from")
        return listOf(
            simple("end_stone", block(Blocks.END_STONE), ModBlocks.ENTROPY_END_STONE.get()),
            simple("stone", block(Blocks.STONE, "variant" to "stone"), Blocks.COBBLESTONE),
            simple("cobblestone", block(Blocks.COBBLESTONE), Blocks.GRAVEL),
            simple("gravel", block(Blocks.GRAVEL), Blocks.SAND),
            simple("sandstone", block(Blocks.SANDSTONE, "type" to "sandstone"), Blocks.SAND),
            simple("tallgrass", block(Blocks.TALLGRASS), Blocks.DEADBUSH),
            simple("red_flower", block(Blocks.RED_FLOWER), Blocks.DEADBUSH),
            simple("yellow_flower", block(Blocks.YELLOW_FLOWER), Blocks.DEADBUSH),
            WandConversion(wandId("log"), block(Blocks.LOG), block(Blocks.COAL_BLOCK), damage(8), effects = sound),
            WandConversion(wandId("log2"), block(Blocks.LOG2), block(Blocks.COAL_BLOCK), damage(8), effects = sound),
            WandConversion(wandId("organa_flower_8"), age(8), age(9), damage(10)),
            WandConversion(wandId("organa_flower_9"), age(9), age(10), damage(10)),
            WandConversion(wandId("end_magma"), block(ModBlocks.END_MAGMA_BLOCK.get()), block(ModBlocks.FLUID_END_MAGMA_BLOCK.get()), damage(15)),
        )
    }

    fun fluidBlockInteractions(): List<FluidBlockInteraction> = listOf(
        FluidBlockInteraction(
            id = Id(Tags.MODID, "entropy/entropy_end_stone"),
            fluid = ModFluids.ENTROPY.get().id(),
            match = block(Blocks.END_STONE),
            result = block(ModBlocks.ENTROPY_END_STONE.get()),
            chance = 5,
        ),
    )

    fun fluidEntityInteractions(): List<FluidEntityInteraction> {
        val entropy = ModFluids.ENTROPY.get().id()
        val organaFlower = Item.getItemFromBlock(ModBlocks.ORGANA_FLOWER_BLOCK.get())
        return listOf(
            FluidEntityInteraction(
                id = Id(Tags.MODID, "entropy/organa_flower"),
                fluid = entropy,
                entity = ITEM_ENTITY,
                item = ItemIngredient.Item(Item.getItemFromBlock(Blocks.CHORUS_FLOWER).id(), 0, 4),
                result = EntityResult.SpawnItem(ItemOut(organaFlower.id(), 0, 4)),
                chance = 10,
                consumeSource = true,
            ),
            FluidEntityInteraction(
                id = Id(Tags.MODID, "entropy/fluid_organa"),
                fluid = entropy,
                entity = ITEM_ENTITY,
                item = ItemIngredient.Item(ModItems.ORGANA_FRUIT.get().id(), 0, 1),
                result = EntityResult.SetFluidBlock(block(ModBlocks.FLUID_ORGANA_BLOCK.get())),
                chance = 20,
                consumeSource = true,
            ),
            FluidEntityInteraction(
                id = Id(Tags.MODID, "entropy/entity_watcher"),
                fluid = entropy,
                entity = Id("minecraft", "enderman"),
                removeEntity = true,
                result = EntityResult.SpawnEntity(Id(Tags.MODID, "watcher")),
                consumeSource = true,
            ),
        )
    }
}
