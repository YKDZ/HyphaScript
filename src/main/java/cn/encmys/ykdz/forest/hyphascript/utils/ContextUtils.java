package cn.encmys.ykdz.forest.hyphascript.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import org.jetbrains.annotations.NotNull;

public class ContextUtils {
    public static @NotNull Context linkContext(@NotNull Context... contexts) {
        if (contexts == null || contexts.length < 2) {
            throw new IllegalArgumentException("At least two Context is required.");
        }

        for (int i = 1; i < contexts.length; i++) {
            contexts[i].setParent(contexts[i - 1]);
        }

        return contexts[contexts.length - 1];
    }
}
