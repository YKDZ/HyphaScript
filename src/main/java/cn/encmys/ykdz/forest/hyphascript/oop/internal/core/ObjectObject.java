package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.*;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

@ObjectName("Object")
public class ObjectObject extends InternalObject {
    @Static
    @Function("create")
    @FunctionParas({"object"})
    public static @NotNull ScriptObject create(@NotNull Context ctx) {
        try {
            ScriptObject prototype = ctx.findMember("object").getReferredValue().getAsScriptObject();
            return new ScriptObject(prototype);
        } catch (Exception e) {
            return new ScriptObject();
        }
    }

    @Static
    @Function("call")
    @FunctionParas({"func", "target"})
    @FunctionUncertainPara("paras")
    public static @NotNull ScriptObject call(@NotNull Context ctx) {
        try {
            cn.encmys.ykdz.forest.hyphascript.function.Function function = ctx.findMember("func").getReferredValue().getAsFunction();
            Value target = ctx.findMember("target").getReferredValue();
            Reference[] paras = ctx.findMember("paras").getReferredValue().getAsArray();
            return function.call(target, Arrays.stream(paras)
                    .map(Reference::getReferredValue)
                    .collect(Collectors.toList()), ctx).getReferredValue().getAsScriptObject();
        } catch (Exception e) {
            return new ScriptObject();
        }
    }

    @Function("keys")
    public static @NotNull Reference[] keys(@NotNull Context ctx) {
        try {
            ScriptObject object = ctx.findMember("this").getReferredValue().getAsScriptObject();
            return object.getLocalMembers().keySet().stream()
                    .map(key -> new Reference(new Value(key), true))
                    .toArray(Reference[]::new);
        } catch (Exception e) {
            return new Reference[0];
        }
    }

    @Function("values")
    public static @NotNull Reference[] values(@NotNull Context ctx) {
        try {
            ScriptObject object = ctx.findMember("this").getReferredValue().getAsScriptObject();
            return object.getLocalMembers().values().toArray(Reference[]::new);
        } catch (Exception e) {
            return new Reference[0];
        }
    }

    @Function("containsKey")
    @FunctionParas("key")
    public static boolean containsKey(@NotNull Context ctx) {
        try {
            ScriptObject object = ctx.findMember("this").getReferredValue().getAsScriptObject();
            String key = ctx.findMember("key").getReferredValue().getAsString();
            return object.getLocalMembers().containsKey(key);
        } catch (Exception e) {
            return false;
        }
    }
}
