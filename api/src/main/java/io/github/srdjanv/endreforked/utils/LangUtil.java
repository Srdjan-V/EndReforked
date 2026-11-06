package io.github.srdjanv.endreforked.utils;

import net.minecraft.util.text.translation.I18n;

public final class LangUtil {

    @SuppressWarnings("deprecation")
    public static String translateToLocal(String key) {
        if (I18n.canTranslate(key)) {
            return I18n.translateToLocal(key);
        }
        return I18n.translateToFallback(key);
    }

    private LangUtil() {}
}
