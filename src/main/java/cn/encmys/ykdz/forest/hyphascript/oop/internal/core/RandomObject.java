package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@ObjectName("Random")
public class RandomObject extends InternalObject {
    private static final @NotNull Random RANDOM = new Random();

    @Static
    @Function("nextInt")
    @FunctionParas({"origin", "bound"})
    public static int nextInt(@NotNull Context ctx) {
        int origin = Integer.MIN_VALUE;
        int bound = Integer.MAX_VALUE;
        try {
            origin = ctx.findMember("origin").getReferredValue().getAsBigDecimal().intValue();
            bound = ctx.findMember("bound").getReferredValue().getAsBigDecimal().intValue();
        } catch (Exception ignored) {
        }
        return RANDOM.nextInt(origin, bound);
    }

    @Static
    @Function("nextDouble")
    @FunctionParas({"origin", "bound"})
    public static double nextDouble(@NotNull Context ctx) {
        double origin = Double.MIN_VALUE;
        double bound = Double.MAX_VALUE;
        try {
            origin = ctx.findMember("origin").getReferredValue().getAsBigDecimal().doubleValue();
            bound = ctx.findMember("bound").getReferredValue().getAsBigDecimal().doubleValue();
        } catch (Exception ignored) {
        }
        return RANDOM.nextDouble(origin, bound);
    }

    @Static
    @Function("nextFloat")
    @FunctionParas({"origin", "bound"})
    public static float nextFloat(@NotNull Context ctx) {
        float origin = Float.MIN_VALUE;
        float bound = Float.MAX_VALUE;
        try {
            origin = ctx.findMember("origin").getReferredValue().getAsBigDecimal().floatValue();
            bound = ctx.findMember("bound").getReferredValue().getAsBigDecimal().floatValue();
        } catch (Exception ignored) {
        }
        return RANDOM.nextFloat(origin, bound);
    }

    @Static
    @Function("nextGaussian")
    @FunctionParas({"mean", "stddev"})
    public static double nextGaussian(@NotNull Context ctx) {
        double mean = 0d;
        double stddev = 1d;
        try {
            mean = ctx.findMember("mean").getReferredValue().getAsBigDecimal().doubleValue();
            stddev = ctx.findMember("stddev").getReferredValue().getAsBigDecimal().doubleValue();
        } catch (Exception ignored) {
        }
        return RANDOM.nextGaussian(mean, stddev);
    }

    @Static
    @Function("nextUniform")
    @FunctionParas({"origin", "bound"})
    public static double nextUniform(@NotNull Context ctx) {
        double origin = 0d;
        double bound = 1d;
        try {
            origin = ctx.findMember("origin").getReferredValue().getAsBigDecimal().doubleValue();
            bound = ctx.findMember("bound").getReferredValue().getAsBigDecimal().doubleValue();
        } catch (Exception ignored) {
        }
        return origin + (bound - origin) * Math.random();
    }

    @Static
    @Function("nextBoolean")
    public static boolean nextBoolean(@NotNull Context ctx) {
        return RANDOM.nextBoolean();
    }
}
