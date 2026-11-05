package io.github.srdjanv.endreforked.utils;

import java.util.List;

public final class TooltipUtils {

    public static void addBeforeLast(List<String> tooltip, String tip) {
        tooltip.add(tooltip.size() - 1, tip);
    }

    private TooltipUtils() {}
}
