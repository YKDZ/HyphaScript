package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@ObjectName("Number")
public class NumberObject extends InternalObject {
    @Static
    @Function("number")
    @FunctionParas("str")
    public static BigDecimal number(@NotNull Context ctx) {
        try {
            return new BigDecimal(ctx.findMember("str").getReferredValue().getAsString());
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }

    @Static
    @Function("isNumber")
    @FunctionParas("str")
    public static boolean isNumber(@NotNull Context ctx) {
        try {
            new BigDecimal(ctx.findMember("str").getReferredValue().getAsString());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Function("intValue")
    public static int intValue(@NotNull Context ctx) {
        try {
            BigDecimal decimal = ctx.findMember("this").getReferredValue().getAsBigDecimal();
            return decimal.intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    @Function("floatValue")
    public static float floatValue(@NotNull Context ctx) {
        try {
            BigDecimal decimal = ctx.findMember("this").getReferredValue().getAsBigDecimal();
            return decimal.floatValue();
        } catch (Exception e) {
            return 0f;
        }
    }

    @Function("doubleValue")
    public static double doubleValue(@NotNull Context ctx) {
        try {
            BigDecimal decimal = ctx.findMember("this").getReferredValue().getAsBigDecimal();
            return decimal.doubleValue();
        } catch (Exception e) {
            return 0d;
        }
    }

    @Function("abs")
    public static BigDecimal abs(@NotNull Context ctx) {
        try {
            BigDecimal decimal = ctx.findMember("this").getReferredValue().getAsBigDecimal();
            return decimal.abs();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
