package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@ObjectName("Array")
public class ArrayObject extends InternalObject {
    @Function("length")
    public static int length(@NotNull Context ctx) {
        try {
            return ctx.findMember("this").getReferredValue().getAsArray().length;
        } catch (Exception e) {
            return 0;
        }
    }

    @Function("sum")
    public static double sum(@NotNull Context ctx) {
        try {
            return Arrays.stream(ctx.findMember("this").getReferredValue().getAsArray())
                    .mapToDouble(ref -> ref.getReferredValue().getAsBigDecimal().doubleValue())
                    .sum();
        } catch (Exception e) {
            return 0d;
        }
    }
}
