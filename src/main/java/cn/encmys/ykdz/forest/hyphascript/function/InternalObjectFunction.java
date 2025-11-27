package cn.encmys.ykdz.forest.hyphascript.function;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.FunctionException;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.utils.FunctionUtils;
import cn.encmys.ykdz.forest.hyphascript.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static cn.encmys.ykdz.forest.hyphascript.utils.StringUtils.formatStackTrace;

public class InternalObjectFunction extends ScriptObject implements Function {
    private @NotNull String name;
    private @NotNull LinkedHashMap<String, ASTNode> parameters;
    private @NotNull String uncertainParameter;
    private @NotNull MethodHandle body;
    private @Nullable Value manualTarget;

    public InternalObjectFunction(@NotNull String name, @NotNull LinkedHashMap<String, ASTNode> parameters, @NotNull String uncertainParameter, @NotNull MethodHandle body) {
        super(InternalObjectManager.FUNCTION_PROTOTYPE);
        super.declareMember("prototype", new Reference(new Value(new ScriptObject.Builder()
                .withMember("constructor", new Reference(new Value(this)))
                .build())));
        this.name = name;
        this.parameters = parameters;
        this.uncertainParameter = uncertainParameter;
        this.body = body;
    }

    @Override
    public @NotNull Reference call(@NotNull Value target, @NotNull List<Value> arguments, @NotNull Context ctx) {
        return executeCall(target, ctx, localContext ->
                FunctionUtils.injectArguments(localContext, this.parameters, arguments, uncertainParameter)
        );
    }

    @Override
    public @NotNull Reference call(@NotNull Value target, @NotNull Map<String, Value> arguments, @NotNull Context ctx) {
        return executeCall(target, ctx, localContext ->
                FunctionUtils.injectArguments(localContext, this.parameters, arguments, uncertainParameter)
        );
    }

    private @NotNull Reference executeCall(
            @NotNull Value targetValue,
            @NotNull Context ctx,
            @NotNull Consumer<@NotNull Context> argumentInjector
    ) {
        Value target = manualTarget == null ? targetValue : manualTarget;
        Context localContext = new Context(ctx.clone());
        localContext.declareMember("this", target);

        try {
            Function superConstructor = super
                    .findMember("__home_object__").getReferredValue().getAsScriptObject()
                    .getProto().getAsScriptObject()
                    .findMember("constructor").getReferredValue().getAsFunction();
            superConstructor.setManualTarget(target);
            localContext.declareMember("super", new Value(superConstructor));
        } catch (Exception ignored) {
        }

        argumentInjector.accept(localContext);

        try {
            Object result = body.invokeWithArguments(localContext);
            if (result instanceof Value) {
                return new Reference((Value) result);
            }
            return new Reference(new Value(result));
        } catch (Throwable e) {
            throw new FunctionException(
                    this,
                    "Error when call internal object function \"" + name + "\"\n" + formatStackTrace(e),
                    e
            );
        }
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull LinkedHashMap<String, ASTNode> getParameters() {
        return parameters;
    }

    @Override
    public @NotNull Context getCapturedContext() {
        return (Context) InternalObjectManager.OBJECT_PROTOTYPE;
    }

    @Override
    public void setCapturedContext(@Nullable Context capturedContext) {
    }

    @Override
    public @NotNull ScriptObject getPrototype() {
        return super.findMember("prototype").getReferredValue().getAsScriptObject();
    }

    public @NotNull MethodHandle getBody() {
        return body;
    }

    @Override
    public void setManualTarget(@Nullable Value manualTarget) {
        this.manualTarget = manualTarget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InternalObjectFunction that = (InternalObjectFunction) o;
        return Objects.equals(parameters, that.parameters) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, body);
    }

    @Override
    public ScriptObject clone() {
        try {
            InternalObjectFunction cloned = (InternalObjectFunction) super.clone();

            cloned.name = this.name;
            cloned.parameters = new LinkedHashMap<>(this.parameters);
            cloned.uncertainParameter = this.uncertainParameter;

            cloned.body = this.body;

            cloned.manualTarget = this.manualTarget != null ? this.manualTarget : null;

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public @NotNull String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("[")
                .append("IFunction")
                .append(" ")
                .append(this.name);

        StringUtils.formatParameters(sb, this.getParameters().keySet());

        return sb.toString();
    }
}
