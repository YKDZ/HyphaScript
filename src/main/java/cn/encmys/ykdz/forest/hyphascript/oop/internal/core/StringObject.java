package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import org.jetbrains.annotations.NotNull;

@ObjectName("String")
public class StringObject extends InternalObject {
    @Function("lower")
    public static String lower(@NotNull Context ctx) {
        final String str = ctx.findMember("this").getReferredValue().getAsString();
        return str.toLowerCase();
    }

    @Function("upper")
    public static String upper(@NotNull Context ctx) {
        final String str = ctx.findMember("this").getReferredValue().getAsString();
        return str.toUpperCase();
    }

    @Function("replace")
    @FunctionParas({"target", "replacement"})
    public static String replace(@NotNull Context ctx) {
        final String str = ctx.findMember("this").getReferredValue().getAsString();
        final String target = ctx.findMember("target").getReferredValue().getAsString();
        final String replacement = ctx.findMember("replacement").getReferredValue().getAsString();

        return str.replace(target, replacement);
    }
}
