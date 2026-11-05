package io.github.srdjanv.endreforked.common.widgets;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.cleanroommc.modularui.drawable.keys.LangKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;

public class BasicTextWidget extends Widget<BasicTextWidget> {

    private Supplier<String> key;
    private Supplier<Object[]> args;
    private Supplier<Pair<String, Object[]>> keyArg;

    public void setKey(Supplier<String> key) {
        this.key = key;
    }

    public void setArgs(Supplier<Object[]> args) {
        this.args = args;
    }

    public void setKeyArg(Supplier<Pair<String, Object[]>> keyArg) {
        this.keyArg = keyArg;
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        if (Objects.isNull(key) && Objects.isNull(keyArg)) return;

        String lambdaKey = null;
        Object[] lambdaArgs = null;

        if (Objects.nonNull(keyArg)) {
            var lambdaKeyArg = keyArg.get();
            if (Objects.nonNull(lambdaKeyArg)) {
                if (Objects.nonNull(lambdaKeyArg.getKey())) lambdaKey = lambdaKeyArg.getKey();
                if (Objects.nonNull(lambdaKeyArg.getValue())) lambdaArgs = lambdaKeyArg.getValue();
            }
        }

        if (Objects.nonNull(key)) {
            var newLambdaKey = key.get();
            if (Objects.isNull(lambdaKey) && Objects.nonNull(newLambdaKey)) lambdaKey = newLambdaKey;
        }
        if (Objects.nonNull(args)) {
            var newLambdaArgs = args.get();
            if (Objects.isNull(lambdaArgs) && Objects.nonNull(newLambdaArgs)) lambdaArgs = newLambdaArgs;
        }

        if (Objects.isNull(lambdaKey)) return;
        final LangKey lang;
        if (Objects.nonNull(lambdaArgs)) {
            lang = new LangKey(lambdaKey, lambdaArgs);
        } else lang = new LangKey(lambdaKey);

        lang.drawAtZero(context, getArea(), widgetTheme);
    }
}
