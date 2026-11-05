package io.github.srdjanv.endreforked.common.items.base;

import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.util.NonNullList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class BaseMetaItemBlock extends ItemBlock implements InventoryItemModel {

    @FunctionalInterface
    public interface Mapper {

        @Nullable
        String apply(int meta);
    }

    protected final Mapper nameFunction;

    public BaseMetaItemBlock(Block block, Mapper nameFunction) {
        super(block);
        this.nameFunction = nameFunction;
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public @NotNull String getTranslationKey(@NotNull ItemStack stack) {
        var post = nameFunction.apply(stack.getItemDamage());
        return super.getTranslationKey() + (post != null ? "." + post.replace('$', '.') : "");
    }

    @Override
    public void handleAssets() {
        NonNullList<ItemStack> items = NonNullList.create();
        this.getSubItems(EndReforked.ENDERTAB, items);
        for (var item : items) {
            var meta = item.getMetadata();
            var postfix = nameFunction.apply(meta);
            EndReforked.getProxy().registerItemRenderer(
                    this, meta,
                    (postfix != null ? "_" + postfix.replace('$', '_') : ""),
                    "inventory");
        }
    }
}
