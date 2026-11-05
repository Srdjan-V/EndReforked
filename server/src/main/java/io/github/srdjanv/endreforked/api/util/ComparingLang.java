package io.github.srdjanv.endreforked.api.util;

import io.github.srdjanv.endreforked.utils.LangUtil;

public final class ComparingLang {

    public enum FluidLang {

        FLUID("status.fluid.fluid"),
        AMOUNT("status.fluid.amount"),
        TAG("status.fluid.tag");

        private final String langKey;

        FluidLang(String langKey) {
            this.langKey = langKey;
        }

        public static String translate(FluidStackHashStrategy hashStrategy, String customLang) {
            var builder = new Builder("status.fluid.matching");

            if (hashStrategy.comparingCustom()) builder.addEntry(LangUtil.translateToLocal(customLang));
            if (hashStrategy.comparingFluid()) builder.addEntry(LangUtil.translateToLocal(FLUID.langKey));
            if (hashStrategy.comparingAmount()) builder.addEntry(LangUtil.translateToLocal(AMOUNT.langKey));
            if (hashStrategy.comparingTag()) builder.addEntry(LangUtil.translateToLocal(TAG.langKey));

            return builder.getBuilderString();
        }
    }

    public enum ItemLang {

        ITEM("status.item.item"),
        COUNT("status.item.amountModifier"),
        DAMAGE("status.item.damage"),
        TAG("status.item.tag");

        private final String langKey;

        ItemLang(String langKey) {
            this.langKey = langKey;
        }

        public static String translate(ItemStackHashStrategy hashStrategy, String customLang) {
            var builder = new Builder("status.item.matching");

            if (hashStrategy.comparingCustom()) builder.addEntry(LangUtil.translateToLocal(customLang));
            if (hashStrategy.comparingItem()) builder.addEntry(LangUtil.translateToLocal(ITEM.langKey));
            if (hashStrategy.comparingCount()) builder.addEntry(LangUtil.translateToLocal(COUNT.langKey));
            if (hashStrategy.comparingDamage()) builder.addEntry(LangUtil.translateToLocal(DAMAGE.langKey));
            if (hashStrategy.comparingTag()) builder.addEntry(LangUtil.translateToLocal(TAG.langKey));
            return builder.getBuilderString();
        }
    }

    public enum EntityLang {

        TYPE("status.entity.type");

        private final String langKey;

        EntityLang(String langKey) {
            this.langKey = langKey;
        }

        public static String translate(EntityMatchStrategy<?> matchStrategy, String customLang) {
            var builder = new Builder("status.entity.matching");

            if (matchStrategy.comparingCustom()) builder.addEntry(LangUtil.translateToLocal(customLang));
            if (matchStrategy.comparingType()) builder.addEntry(LangUtil.translateToLocal(TYPE.langKey));
            return builder.getBuilderString();
        }
    }

    private static class Builder {

        private final String matchingLangKey;
        private StringBuilder builder;

        private Builder(String matchingLangKey) {
            this.matchingLangKey = matchingLangKey;
        }

        private StringBuilder getBuilder() {
            if (builder == null) builder = new StringBuilder(LangUtil.translateToLocal(matchingLangKey)).append(" ");
            return builder;
        }

        public void addEntry(String entry) {
            getBuilder().append(entry).append(", ");
        }

        public String getBuilderString() {
            var index = builder.lastIndexOf(", ");
            if (index != -1) return builder.substring(0, index);
            return builder.toString();
        }
    }

    private ComparingLang() {}
}
