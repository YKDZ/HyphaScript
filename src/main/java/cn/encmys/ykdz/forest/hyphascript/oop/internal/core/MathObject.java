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
        BigDecimal target = ctx.findMember("target").getReferredValue().getAsBigDecimal();
        return target.setScale(0, RoundingMode.FLOOR);
    }

    @Static
    @Function("ceil")
    @FunctionParas("target")
    public static BigDecimal ceil(@NotNull Context ctx) {
        BigDecimal target = ctx.findMember("target").getReferredValue().getAsBigDecimal();
        return target.setScale(0, RoundingMode.CEILING);
    }

    @Static
    @Function("abs")
    public static BigDecimal abs(@NotNull Context ctx) {
        BigDecimal target = ctx.findMember("target").getReferredValue().getAsBigDecimal();
        return target.abs();
    }

    @Static
    @Function("round")
    public static BigDecimal round(@NotNull Context ctx) {
        BigDecimal target = ctx.findMember("target").getReferredValue().getAsBigDecimal();
        return target.round(MathContext.UNLIMITED);
    }

    @Static
    @Function("random")
    public static double random(@NotNull Context ctx) {
        return Math.random();
    }
}
