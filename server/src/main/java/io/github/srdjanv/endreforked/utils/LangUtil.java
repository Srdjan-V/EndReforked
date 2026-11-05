package io.github.srdjanv.endreforked.utils;

import java.util.IllegalFormatException;

import net.minecraft.util.text.translation.I18n;

public final class LangUtil {

    @SuppressWarnings("deprecation")
    public static String translateToLocal(String key) {
        if (I18n.canTranslate(key)) {
            return I18n.translateToLocal(key);
        }
        return I18n.translateToFallback(key);
    }

    public static String translateToLocalFormatted(String key, Object... format) {
        String string = translateToLocal(key);
        try {
            return String.format(string, format);
        } catch (IllegalFormatException exception) {
            return "FormatError: " + string;
        }
    }

    private LangUtil() {}
}
