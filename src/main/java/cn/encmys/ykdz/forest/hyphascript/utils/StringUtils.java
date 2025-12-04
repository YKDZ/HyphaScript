package cn.encmys.ykdz.forest.hyphascript.utils;

import cn.encmys.ykdz.forest.hyphascript.HyphaScript;
import cn.encmys.ykdz.forest.hyphascript.value.ScriptArray;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StringUtils {
    private static final ThreadLocal<Set<Object>> TO_STRING_VISITED = ThreadLocal
            .withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    public static @NotNull String toString(@NotNull Class<?> clazz) {
        return "[Class: " + clazz.getSimpleName() + "]";
    }

    public static @NotNull String toString(@NotNull Number num) {
        if (num instanceof BigDecimal bd) {
            return bd.stripTrailingZeros().toPlainString();
        }
        return num.toString();
    }

    public static @NotNull String toString(@NotNull MethodHandle[] handles) {
        return Arrays.stream(handles)
                .map(mh -> {
                    MethodType type = mh.type();
                    return "[Method: " + type.returnType().getSimpleName() + " " +
                            mh.getClass().getSimpleName() + "()]";
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public static @NotNull String toString(@NotNull ScriptArray array) {
        Set<Object> visited = TO_STRING_VISITED.get();
        if (visited.contains(array)) {
            return "[Circular Reference]";
        }
        visited.add(array);
        try {
            List<String> parts = new ArrayList<>();
            int len = array.length();
            for (int i = 0; i < len; i++) {
                if (array.containsKey(i)) {
                    try {
                        parts.add(array.get(i).getReferredValue().toReadableString());
                    } catch (Exception e) {
                        parts.add("[Error]");
                    }
                } else {
                    parts.add("null");
                }
            }
            return parts.stream().collect(Collectors.joining(", ", "[", "]"));
        } finally {
            visited.remove(array);
            if (visited.isEmpty()) {
                TO_STRING_VISITED.remove();
            }
        }
    }

    public static @NotNull String toString(@NotNull Component component) {
        return HyphaScript.miniMessage.serialize(component);
    }

    public static @NotNull String toString(@NotNull Object obj) {
        return "[Object: " + obj.getClass().getSimpleName() + "@" +
                Integer.toHexString(System.identityHashCode(obj)) + "]";
    }

    public static void formatParameters(@NotNull StringBuilder sb, @NotNull Set<String> args) {
        final List<String> allArgs = new ArrayList<>(args);

        sb.append("(")
                .append(String.join(", ", allArgs))
                .append(")");

        sb.append(" => ")
                .append("{...}")
                .append("]");
    }

    public static @NotNull String formatStackTrace(@NotNull Throwable throwable) {
        StringBuilder sb = new StringBuilder();

        sb.append(throwable).append("\n");

        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }

        Throwable cause = throwable.getCause();
        while (cause != null) {
            sb.append("Caused by: ").append(cause).append("\n");
            for (StackTraceElement element : cause.getStackTrace()) {
                sb.append("\tat ").append(element.toString()).append("\n");
            }
            cause = cause.getCause();
        }

        return sb.toString();
    }
}
