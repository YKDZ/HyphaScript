package cn.encmys.ykdz.forest.hyphascript.utils;

import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StringUtils {
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

    public static @NotNull String toString(@NotNull Reference[] array) {
        return Arrays.stream(array).map(reference -> {
                    try {
                        Value element = reference.getReferredValue();
                        return element.toReadableString();
                    } catch (Exception e) {
                        return "[Error]";
                    }
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public static @NotNull String toString(@NotNull Component component) {
        return "[Component: " + component.getClass().getSimpleName() + "]";
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
}
