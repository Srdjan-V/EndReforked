package io.github.srdjanv.endreforked.api.util;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

import it.unimi.dsi.fastutil.Hash;

/**
 * A configurable generator of hashing strategies, allowing for consideration of select properties of ItemStacks when
 * considering equality.
 * This Code is mostly taken from
 * <a href=
 * "https://github.com/GregTechCEu/GregTech/blob/2.8/src/main/java/gregtech/api/util/ItemStackHashStrategy.java">GTCEu</a>
 */
public interface ItemStackHashStrategy extends Hash.Strategy<ItemStack> {

    /**
     * @return a builder object for producing a custom ItemStackHashStrategy.
     */
    static Builder builder() {
        return new Builder();
    }

    Supplier<ItemStackHashStrategy> memorizedComparingAll = Suppliers.memoize(ItemStackHashStrategy::comparingAll);

    static ItemStackHashStrategy memorizedComparingAll() {
        return memorizedComparingAll.get();
    }

    /**
     * Generates an ItemStackHash configured to compare every aspect of ItemStacks.
     *
     * @return the ItemStackHashStrategy as described above.
     */
    static ItemStackHashStrategy comparingAll() {
        return builder().compareItem(true)
                .compareCount(true)
                .compareDamage(true)
                .compareTag(true)
                .build();
    }

    Supplier<ItemStackHashStrategy> memorizedComparingAllButCount = Suppliers
            .memoize(ItemStackHashStrategy::comparingAllButCount);

    static ItemStackHashStrategy memorizedComparingAllButCount() {
        return memorizedComparingAllButCount.get();
    }

    /**
     * Generates an ItemStackHash configured to compare every aspect of ItemStacks except the number
     * of items in the stack.
     *
     * @return the ItemStackHashStrategy as described above.
     */
    static ItemStackHashStrategy comparingAllButCount() {
        return builder().compareItem(true)
                .compareDamage(true)
                .compareTag(true)
                .build();
    }

    Supplier<ItemStackHashStrategy> memorizedComparingItemDamageCount = Suppliers
            .memoize(ItemStackHashStrategy::comparingItemDamageCount);

    static ItemStackHashStrategy memorizedComparingItemDamageCount() {
        return memorizedComparingItemDamageCount.get();
    }

    static ItemStackHashStrategy comparingItemDamageCount() {
        return builder().compareItem(true)
                .compareDamage(true)
                .compareCount(true)
                .build();
    }

    boolean comparingItem();

    boolean comparingCount();

    boolean comparingDamage();

    boolean comparingTag();

    boolean comparingCustom();

    @Nullable
    String customCheckLangKey();

    class Builder {

        private boolean item, count, damage, tag;
        private BiPredicate<ItemStack, ItemStack> customCheck;
        private String customCheckLangKey;

        private Builder() {}

        /**
         * Defines whether the Item type should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public Builder compareItem(boolean choice) {
            item = choice;
            return this;
        }

        /**
         * Defines whether stack size should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public Builder compareCount(boolean choice) {
            count = choice;
            return this;
        }

        /**
         * Defines whether damage values should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public Builder compareDamage(boolean choice) {
            damage = choice;
            return this;
        }

        /**
         * Defines whether NBT Tags should be considered for equality.
         *
         * @param choice {@code true} to consider this property, {@code false} to ignore it.
         * @return {@code this}
         */
        public Builder compareTag(boolean choice) {
            tag = choice;
            return this;
        }

        public Builder setCustomCheck(BiPredicate<ItemStack, ItemStack> customCheck, String customCheckLangKey) {
            this.customCheck = customCheck;
            this.customCheckLangKey = customCheckLangKey;
            return this;
        }

        public ItemStackHashStrategy build() {
            return new ItemStackHashStrategy() {

                @Override
                public boolean comparingItem() {
                    return item;
                }

                @Override
                public boolean comparingCount() {
                    return count;
                }

                @Override
                public boolean comparingDamage() {
                    return damage;
                }

                @Override
                public boolean comparingTag() {
                    return tag;
                }

                @Override
                public boolean comparingCustom() {
                    return Objects.nonNull(customCheck);
                }

                @Override
                public @Nullable String customCheckLangKey() {
                    return customCheckLangKey;
                }

                @Override
                public int hashCode(@Nullable ItemStack o) {
                    return o == null || o.isEmpty() ? 0 : Objects.hash(
                            item ? o.getItem() : null,
                            count ? o.getCount() : null,
                            damage ? o.getItemDamage() : null,
                            tag ? o.getTagCompound() : null);
                }

                @Override
                public boolean equals(@Nullable ItemStack a, @Nullable ItemStack b) {
                    if (a == null || a.isEmpty()) return b == null || b.isEmpty();
                    if (b == null || b.isEmpty()) return false;

                    return (!Objects.nonNull(customCheck) || customCheck.test(a, b)) &&
                            (!item || a.getItem() == b.getItem()) &&
                            (!count || a.getCount() == b.getCount()) &&
                            (!damage || a.getItemDamage() == b.getItemDamage()) &&
                            (!tag || Objects.equals(a.getTagCompound(), b.getTagCompound()));
                }
            };
        }
    }
}
