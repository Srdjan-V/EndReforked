package io.github.srdjanv.endreforked.common.items.base;

import java.util.function.Supplier;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CustomModelArmor extends ArmourBase {

    private final Supplier<ModelBiped> customModel;

    public CustomModelArmor(String name, ArmorMaterial materialIn, int renderIndexIn,
                            EntityEquipmentSlot equipmentSlotIn, Supplier<ModelBiped> customModel) {
        super(name, materialIn, renderIndexIn, equipmentSlotIn);
        this.customModel = customModel;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot armorSlot,
                                    ModelBiped _default) {
        if (armorSlot != armorType) return null;
        if (!(stack.getItem() instanceof CustomModelArmor customModelArmor)) return null;
        var model = customModelArmor.customModel.get();
        model.isSneak = _default.isSneak;
        model.isRiding = _default.isRiding;
        model.isChild = _default.isChild;
        model.rightArmPose = _default.rightArmPose;
        model.leftArmPose = _default.leftArmPose;

        return model;
    }
}
