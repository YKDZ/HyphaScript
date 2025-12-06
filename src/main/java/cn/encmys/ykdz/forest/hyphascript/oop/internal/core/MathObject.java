package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@ObjectName("Math")
public class MathObject extends InternalObject {
    @Static
    @Function("floor")
    @FunctionParas("target")
    public static BigDecimal floor(@NotNull Context ctx) {
        final BigDecimal target = ctx.findMember("target").getReferredValue().getAsBigDecimal();
        return target.setScale(0, RoundingMode.FLOOR);
    }

    @Static
    @Function("ceil")
    @FunctionParas("target")
    public static BigDecimal ceil(@NotNull Context ctx) {
        final BigDecimal target = ctx.findMember("target").getReferredValue().getAsBigDecimal();
        return target.setScale(0, RoundingMode.CEILING);
    }

    @Static
    @Function("round")
    public static BigDecimal round(@NotNull Context ctx) {
        final BigDecimal target = ctx.findMember("target").getReferredValue().getAsBigDecimal();
        return target.round(MathContext.UNLIMITED);
    }

    @Static
    @Function("random")
    public static double random(@NotNull Context ctx) {
        return Math.random();
    }

    @Static
    @Function("min")
    @FunctionParas({"a", "b"})
    public static BigDecimal min(@NotNull Context ctx) {
        final BigDecimal a = ctx.findMember("a").getReferredValue().getAsBigDecimal();
        final BigDecimal b = ctx.findMember("b").getReferredValue().getAsBigDecimal();

        return a.compareTo(b) <= 0 ? a : b;
    }

    @Static
    @Function("max")
    @FunctionParas({"a", "b"})
    public static BigDecimal max(@NotNull Context ctx) {
        final BigDecimal a = ctx.findMember("a").getReferredValue().getAsBigDecimal();
        final BigDecimal b = ctx.findMember("b").getReferredValue().getAsBigDecimal();

        return a.compareTo(b) >= 0 ? a : b;
    }

    @Static
    @Function("log10")
    @FunctionParas({"a"})
    public static BigDecimal log10(@NotNull Context ctx) {
        final BigDecimal a = ctx.findMember("a").getReferredValue().getAsBigDecimal();
        return BigDecimal.valueOf(Math.log10(a.doubleValue()));
    }
}
