package endreborn.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import endreborn.EndReborn;
import endreborn.common.ModBlocks;
import endreborn.common.ModItems;
import endreborn.utils.IHasModel;

public class LampBase extends Block implements IHasModel {

    public LampBase(String name, Material material) {
        super(material);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReborn.endertab);
        setLightLevel(1.0F);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(this.getRegistryName()));
    }

    @Override
    public void registerModels() {
        EndReborn.proxy.registerItemRenderer(Item.getItemFromBlock(this), 0, "inventory");
    }
}