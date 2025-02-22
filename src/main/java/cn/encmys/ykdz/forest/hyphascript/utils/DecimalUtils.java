package cn.encmys.ykdz.forest.hyphascript.utils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalUtils {
    public static boolean isEquals(@NotNull BigDecimal a, @NotNull BigDecimal b, @NotNull RoundingMode roundingMode) {
        return a.compareTo(b.setScale(a.scale(), roundingMode)) == 0;
    }
}
