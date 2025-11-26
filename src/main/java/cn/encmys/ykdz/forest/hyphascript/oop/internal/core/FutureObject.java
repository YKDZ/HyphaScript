package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ObjectName("Future")
public class FutureObject extends InternalObject {
    @Static
    @Function("supply")
    @FunctionParas("function")
    public static ScriptObject supply(@NotNull Context ctx) {
        try {
            ScriptObject wrapper = InternalObjectManager.FUTURE.newInstance();
            cn.encmys.ykdz.forest.hyphascript.function.Function func = ctx.findMember("function").getReferredValue().getAsFunction();
            CompletableFuture<Reference> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return func.call(new Value(wrapper), Collections.emptyList(), ctx);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
            wrapper.declareMember("future", new Value(future));
            return wrapper;
        } catch (Exception e) {
            e.printStackTrace();
            return InternalObjectManager.FUTURE.newInstance();
        }
    }

    @Function("then")
    @FunctionParas("function")
    @SuppressWarnings("unchecked")
    public static ScriptObject then(@NotNull Context ctx) {
        try {
            ScriptObject wrapper = ctx.findMember("this").getReferredValue().getAsScriptObject();
            cn.encmys.ykdz.forest.hyphascript.function.Function func = ctx.findMember("function").getReferredValue().getAsFunction();
            CompletableFuture<Reference> future = (CompletableFuture<Reference>) wrapper.findMember("future").getReferredValue().getValue();
            if (future == null) {
                return InternalObjectManager.FUTURE.newInstance();
            }
            future = future.thenApplyAsync((result) -> {
                return func.call(new Value(wrapper), List.of(result.getReferredValue()), ctx);
            });
            wrapper.forceSetLocalMember("future", new Reference(new Value(future)));
            return wrapper;
        } catch (Exception e) {
            e.printStackTrace();
            return InternalObjectManager.FUTURE.newInstance();
        }
    }

    @Function("whenComplete")
    @FunctionParas("function")
    @SuppressWarnings("unchecked")
    public static ScriptObject whenComplete(@NotNull Context ctx) {
        try {
            ScriptObject wrapper = ctx.findMember("this").getReferredValue().getAsScriptObject();
            cn.encmys.ykdz.forest.hyphascript.function.Function func = ctx.findMember("function").getReferredValue().getAsFunction();
            CompletableFuture<Reference> future = (CompletableFuture<Reference>) wrapper.findMember("future").getReferredValue().getValue();
            if (future == null) return InternalObjectManager.FUTURE.newInstance();
            future = future.whenCompleteAsync((result, throwable) -> func.call(new Value(wrapper), List.of(result.getReferredValue(), new Value(throwable)), ctx));
            wrapper.forceSetLocalMember("future", new Reference(new Value(future)));
            return wrapper;
        } catch (Exception e) {
            return InternalObjectManager.FUTURE.newInstance();
        }
    }

    @Function("get")
    @SuppressWarnings("unchecked")
    public static Object get(@NotNull Context ctx) {
        try {
            ScriptObject wrapper = ctx.findMember("this").getReferredValue().getAsScriptObject();
            CompletableFuture<Reference> future = (CompletableFuture<Reference>) wrapper.findMember("future").getReferredValue().getValue();
            if (future == null) return null;
            return future.get().getReferredValue().getValue();
        } catch (Exception e) {
            return null;
        }
    }
}