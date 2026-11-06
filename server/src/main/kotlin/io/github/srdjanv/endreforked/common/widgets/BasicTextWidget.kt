package io.github.srdjanv.endreforked.common.widgets

import com.cleanroommc.modularui.drawable.keys.LangKey
import com.cleanroommc.modularui.screen.viewport.GuiContext
import com.cleanroommc.modularui.theme.WidgetTheme
import com.cleanroommc.modularui.widget.Widget
import org.apache.commons.lang3.tuple.Pair
import java.util.function.Supplier

class BasicTextWidget : Widget<BasicTextWidget>() {
    private var key: Supplier<String?>? = null
    private var args: Supplier<Array<Any>?>? = null
    private var keyArg: Supplier<Pair<String?, Array<Any>?>?>? = null

    fun setKey(key: Supplier<String?>) {
        this.key = key
    }

    fun setArgs(args: Supplier<Array<Any>?>) {
        this.args = args
    }

    fun setKeyArg(keyArg: Supplier<Pair<String?, Array<Any>?>?>) {
        this.keyArg = keyArg
    }

    override fun draw(context: GuiContext, widgetTheme: WidgetTheme) {
        val fromPair = keyArg?.get()
        val lambdaKey = fromPair?.key ?: key?.get() ?: return
        val lambdaArgs = fromPair?.value ?: args?.get()
        val lang = if (lambdaArgs != null) LangKey(lambdaKey, lambdaArgs) else LangKey(lambdaKey)
        lang.drawAtZero(context, area, widgetTheme)
    }
}
