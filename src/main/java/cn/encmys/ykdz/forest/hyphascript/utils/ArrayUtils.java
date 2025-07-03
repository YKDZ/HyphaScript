package cn.encmys.ykdz.forest.hyphascript.utils;

import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ArrayUtils {
    public static @NotNull Reference @NotNull [] toReferences(@NotNull Object[] array) {
        return Arrays.stream(array)
                .map(value -> new Reference(new Value(value)))
                .toArray(Reference[]::new);
    }
}
