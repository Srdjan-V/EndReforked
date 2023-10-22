package endreborn.common.items.tools;

import net.minecraft.item.ItemSpade;

import endreborn.EndReborn;
import endreborn.utils.IHasModel;

public class ToolShovel extends ItemSpade implements IHasModel {

    public ToolShovel(String name, ToolMaterial material) {
        super(material);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReborn.endertab);
    }

    @Override
    public void registerModels() {
        EndReborn.proxy.registerItemRenderer(this, 0, "inventory");
    }
}
