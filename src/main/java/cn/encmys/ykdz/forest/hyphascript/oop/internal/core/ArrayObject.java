package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.ScriptArray;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

@ObjectName("Array")
public class ArrayObject extends InternalObject {
    @Function("__iterator__")
    public static ScriptObject iterator(@NotNull Context ctx) {
        ScriptArray array = ctx.findMember("this").getReferredValue().getAsArray();
        ScriptObject prototype = InternalObjectManager.ARRAY_ITERATOR_PROTOTYPE;
        ScriptObject iterator = new ScriptObject(prototype);
        iterator.declareMember("array", new Value(array));
        iterator.declareMember("index", new Value(0));
        return iterator;
    }

    @Function("length")
    public static int length(@NotNull Context ctx) {
        try {
            return ctx.findMember("this").getReferredValue().getAsArray().length();
        } catch (Exception e) {
            return 0;
        }
    }

    @Function("sum")
    public static double sum(@NotNull Context ctx) {
        try {
            return ctx.findMember("this").getReferredValue().getAsArray().values().stream()
                    .mapToDouble(ref -> ref.getReferredValue().getAsBigDecimal().doubleValue())
                    .sum();
        } catch (Exception e) {
            return 0d;
        }
    }

    @Function("entries")
    public static ScriptArray entries(@NotNull Context ctx) {
        try {
            final ScriptArray result = new ScriptArray();
            final ScriptArray array = ctx.findMember("this").getReferredValue().getAsArray();

            int i = 0;
            for (Map.Entry<Integer, Reference> entry : array.entrySet()) {
                final ScriptArray newEntry = new ScriptArray();
                newEntry.put(1, entry.getValue());
                newEntry.put(0, new Reference(new Value(i)));
                result.push(new Reference(new Value(newEntry)));
                i += 1;
            }

            return result;
        } catch (Exception e) {
            return new ScriptArray();
        }
    }

    @Function("join")
    @FunctionParas({"delimiter"})
    public static String join(@NotNull Context ctx) {
        return ctx.findMember("this").getReferredValue().getAsArray()
                .values()
                .stream()
                .map(ref -> ref.getReferredValue().getAsString())
                .collect(Collectors.joining(
                        ctx.findMember("delimiter").getReferredValue().getAsString()));
    }

    @Function("forEach")
    @FunctionParas("callback")
    public static void forEach(@NotNull Context ctx) {
        ScriptArray array = ctx.findMember("this").getReferredValue().getAsArray();
        cn.encmys.ykdz.forest.hyphascript.function.Function callback = ctx.findMember("callback").getReferredValue()
                .getAsFunction();
        Value arrayVal = new Value(array);

        for (Map.Entry<Integer, Reference> entry : array.entrySet()) {
            Value element = entry.getValue().getReferredValue();
            Value index = new Value(entry.getKey());
            callback.call(arrayVal, java.util.List.of(element, index, arrayVal), ctx);
        }
    }

    @Function("map")
    @FunctionParas("callback")
    public static ScriptArray map(@NotNull Context ctx) {
        ScriptArray array = ctx.findMember("this").getReferredValue().getAsArray();
        cn.encmys.ykdz.forest.hyphascript.function.Function callback = ctx.findMember("callback").getReferredValue()
                .getAsFunction();
        ScriptArray result = new ScriptArray();
        Value arrayVal = new Value(array);

        for (Map.Entry<Integer, Reference> entry : array.entrySet()) {
            Value element = entry.getValue().getReferredValue();
            Value index = new Value(entry.getKey());
            Value resultValue = callback.call(arrayVal, java.util.List.of(element, index, arrayVal), ctx)
                    .getReferredValue();
            result.put(entry.getKey(), new Reference(resultValue));
        }
        return result;
    }

    @Function("filter")
    @FunctionParas("callback")
    public static ScriptArray filter(@NotNull Context ctx) {
        ScriptArray array = ctx.findMember("this").getReferredValue().getAsArray();
        cn.encmys.ykdz.forest.hyphascript.function.Function callback = ctx.findMember("callback").getReferredValue()
                .getAsFunction();
        ScriptArray result = new ScriptArray();
        Value arrayVal = new Value(array);

        for (Map.Entry<Integer, Reference> entry : array.entrySet()) {
            Value element = entry.getValue().getReferredValue();
            Value index = new Value(entry.getKey());
            Value resultValue = callback.call(arrayVal, java.util.List.of(element, index, arrayVal), ctx)
                    .getReferredValue();
            if (resultValue.getAsBoolean()) {
                result.push(new Reference(element));
            }
        }
        return result;
    }

    @Function("push")
    @FunctionParas({"value"})
    public static void push(@NotNull Context ctx) {
        ScriptArray array = ctx.findMember("this").getReferredValue().getAsArray();
        array.push(ctx.findMember("value"));
    }
}
