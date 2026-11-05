package io.github.srdjanv.endreforked.api.util;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

public interface EntityMatchStrategy<E extends Entity> {

    static <E extends Entity> Builder<E> builder() {
        return new Builder<>();
    }

    Supplier<EntityMatchStrategy<? extends Entity>> memorizedComparingBaseType = Suppliers
            .memoize(EntityMatchStrategy::comparingBaseType);

    static <T extends Entity> EntityMatchStrategy<T> memorizedComparingBaseType() {
        return (EntityMatchStrategy<T>) memorizedComparingBaseType.get();
    }

    static <T extends Entity> EntityMatchStrategy<T> comparingBaseType() {
        return (EntityMatchStrategy<T>) builder().compareType(true)
                .build();
    }

    Supplier<EntityMatchStrategy<EntityItem>> memorizedComparingEntityItemAll = Suppliers
            .memoize(EntityMatchStrategy::comparingEntityItemAll);

    static EntityMatchStrategy<EntityItem> memorizedComparingEntityItemAll() {
        return memorizedComparingEntityItemAll.get();
    }

    static EntityMatchStrategy<EntityItem> comparingEntityItemAll() {
        return new Builder<EntityItem>()
                .compareType(true)
                .setCustomCheck((entityItem, entityItem2) -> {
                    return ItemStackHashStrategy.memorizedComparingAll().equals(entityItem.getItem(),
                            entityItem2.getItem());
                }, ComparingLang.ItemLang.translate(ItemStackHashStrategy.memorizedComparingAll(), null))
                .build();
    }

    Supplier<EntityMatchStrategy<EntityItem>> memorizedComparingEntityItemAllButCount = Suppliers
            .memoize(EntityMatchStrategy::comparingEntityItemAllButCount);

    static EntityMatchStrategy<EntityItem> memorizedComparingEntityItemAllButCount() {
        return memorizedComparingEntityItemAllButCount.get();
    }

    static EntityMatchStrategy<EntityItem> comparingEntityItemAllButCount() {
        return new Builder<EntityItem>()
                .compareType(true)
                .setCustomCheck((entityItem, entityItem2) -> {
                    return ItemStackHashStrategy.memorizedComparingAllButCount().equals(entityItem.getItem(),
                            entityItem2.getItem());
                }, ComparingLang.ItemLang.translate(ItemStackHashStrategy.memorizedComparingAllButCount(), null))
                .build();
    }

    boolean comparingType();

    boolean comparingCustom();

    @Nullable
    String customCheckLangKey();

    boolean match(@Nullable E e1, E e2);

    class Builder<E extends Entity> {

        private boolean type;
        private BiPredicate<E, E> customCheck;
        private String customCheckLangKey;

        private Builder() {}

        public Builder<E> compareType(boolean choice) {
            type = choice;
            return this;
        }

        public Builder<E> setCustomCheck(BiPredicate<E, E> customCheck, String customCheckLangKey) {
            this.customCheck = customCheck;
            this.customCheckLangKey = customCheckLangKey;
            return this;
        }

        public EntityMatchStrategy<E> build() {
            return new EntityMatchStrategy<>() {

                @Override
                public boolean comparingType() {
                    return type;
                }

                @Override
                public boolean comparingCustom() {
                    return customCheck != null;
                }

                @Override
                public @Nullable String customCheckLangKey() {
                    return customCheckLangKey;
                }

                @Override
                public boolean match(@Nullable E a, @Nullable E b) {
                    if (a == null && b == null) return true;
                    if (a == null || b == null) return false;

                    return (!type || a.getClass() == b.getClass()) &&
                            (!Objects.nonNull(customCheck) || customCheck.test(a, b));
                }
            };
        }
    }
}
