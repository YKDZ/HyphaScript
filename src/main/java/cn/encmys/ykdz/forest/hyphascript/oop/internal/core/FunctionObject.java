package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

@ObjectName("Function")
public class FunctionObject extends InternalObject {
    @Function("call")
    @FunctionParas({"target", "paras"})
    public static @NotNull ScriptObject call(@NotNull Context ctx) {
        try {
            cn.encmys.ykdz.forest.hyphascript.function.Function function = ctx.findMember("this").getReferredValue().getAsFunction();
            Value target = ctx.findMember("target").getReferredValue();
            Reference[] paras = ctx.findMember("paras").getReferredValue().getAsArray();
            return function.call(target, Arrays.stream(paras)
                    .map(Reference::getReferredValue)
                    .collect(Collectors.toList()), ctx).getReferredValue().getAsScriptObject();
        } catch (Exception e) {
            return new ScriptObject();
        }
    }

    @Function("toString")
    public static @NotNull String toString(@NotNull Context ctx) {
        try {
            cn.encmys.ykdz.forest.hyphascript.function.Function function = ctx.findMember("this").getReferredValue().getAsFunction();
            return StringUtils.toString(function);
        } catch (Exception e) {
            return "";
        }
    }
}
