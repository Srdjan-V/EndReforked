package io.github.srdjanv.endreforked.utils

import net.minecraft.util.text.translation.I18n

@Suppress("DEPRECATION")
object LangUtil {
    @JvmStatic
    fun translateToLocal(key: String): String =
        if (I18n.canTranslate(key)) I18n.translateToLocal(key) else I18n.translateToFallback(key)
}
