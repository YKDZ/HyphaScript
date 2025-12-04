package cn.encmys.ykdz.forest.hyphascript.function;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.ReturnNotificationException;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphascript.utils.FunctionUtils;
import cn.encmys.ykdz.forest.hyphascript.utils.StringUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ScriptFunction extends ScriptObject implements Function, Cloneable {
    private static final ThreadLocal<Set<ScriptFunction>> HASH_CODE_VISITED = ThreadLocal.withInitial(HashSet::new);

    private @NotNull String name;
    private @NotNull LinkedHashMap<String, ASTNode> parameters;
    private @NotNull String uncertainParameter;
    private @NotNull ASTNode body;
    private @NotNull Context capturedContext;
    private @Nullable Value manualTarget;

    public ScriptFunction(@NotNull String name, @NotNull LinkedHashMap<String, ASTNode> parameters,
            @NotNull String uncertainParameter, @NotNull ASTNode body, @NotNull Context capturedContext) {
        super(InternalObjectManager.FUNCTION_PROTOTYPE);
        super.declareMember("prototype", new Reference(new Value(ScriptObject.Builder.create()
                .with("constructor", new Value(this))
                .build())));
        this.name = name;
        this.parameters = parameters;
        this.uncertainParameter = uncertainParameter;
        this.body = body;
        this.capturedContext = capturedContext;
    }

    @Override
    public @NotNull Reference call(@NotNull Value target, @NotNull List<Value> arguments, @NotNull Context ctx) {
        return executeCall(target, ctx, localContext -> FunctionUtils.injectArguments(localContext, this.parameters,
                arguments, uncertainParameter));
    }

    @Override
    public @NotNull Reference call(@NotNull Value target, @NotNull Map<String, Value> arguments, @NotNull Context ctx) {
        return executeCall(target, ctx, localContext -> FunctionUtils.injectArguments(localContext, this.parameters,
                arguments, uncertainParameter));
    }

    public @NotNull Reference executeCall(
            @NotNull Value targetValue,
            @NotNull Context ctx,
            @NotNull Consumer<@NotNull Context> argumentInjector) {
        Value target = manualTarget == null ? targetValue : manualTarget;

        Context localContext = new Context(ctx.equals(capturedContext) ? ctx
                : ContextUtils.linkContext(
                        ctx.clone(),
                        capturedContext.clone()));
        localContext.declareMember("this", target);

        try {
            Function superConstructor = super.findMember("__home_object__").getReferredValue().getAsScriptObject()
                    .getProto().getAsScriptObject()
                    .findMember("constructor").getReferredValue().getAsFunction();
            superConstructor.setManualTarget(target);
            localContext.declareMember("super", new Value(superConstructor));
        } catch (Exception ignored) {
        }

        argumentInjector.accept(localContext);

        Reference result = new Reference();
        try {
            // 若没有显式指定 return
            // 则什么都不会返回
            body.evaluate(localContext);
        } catch (ReturnNotificationException e) {
            result = e.getReturnedReference();
        }
        // 若函数的返回值是函数
        // 则当前上下文链需要被储存到返回的函数中
        if (!result.getReferredValue().isType(Value.Type.VOID)
                && result.getReferredValue().value() instanceof ScriptFunction function) {
            function.capturedContext = localContext;
        }

        return result;
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
        return capturedContext;
    }

    @Override
    public void setCapturedContext(@NotNull Context capturedContext) {
        this.capturedContext = capturedContext;
    }

    @Override
    public @NotNull ScriptObject getPrototype() {
        return super.findMember("prototype").getReferredValue().getAsScriptObject();
    }

    public @NotNull ASTNode getBody() {
        return body;
    }

    @Override
    public void setManualTarget(@Nullable Value manualTarget) {
        this.manualTarget = manualTarget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ScriptFunction that = (ScriptFunction) o;
        return Objects.equals(parameters, that.parameters) && Objects.equals(body, that.body)
                && Objects.equals(capturedContext, that.capturedContext);
    }

    @Override
    public int hashCode() {
        Set<ScriptFunction> visited = HASH_CODE_VISITED.get();
        if (visited.contains(this)) {
            return 0;
        }
        visited.add(this);
        try {
            return Objects.hash(parameters, body, capturedContext);
        } finally {
            visited.remove(this);
            if (visited.isEmpty()) {
                HASH_CODE_VISITED.remove();
            }
        }
    }

    @Override
    public ScriptObject clone() {
        ScriptFunction cloned = (ScriptFunction) super.clone();

        cloned.name = this.name;
        cloned.parameters = new LinkedHashMap<>(this.parameters);
        cloned.uncertainParameter = this.uncertainParameter;

        cloned.body = this.body;

        cloned.capturedContext = this.capturedContext;

        cloned.manualTarget = this.manualTarget != null ? this.manualTarget : null;

        return cloned;
    }

    @Override
    public @NotNull String toString() {
        final StringBuilder sb = new StringBuilder();

        final String funcName = this.getName();
        final String funcType = funcName.isEmpty() ? "ArrowFunction" : "Function";

        sb.append("[")
                .append(funcType);

        if (funcType.equals("Function")) {
            sb.append(" ").append(funcName);
        }

        StringUtils.formatParameters(sb, this.getParameters().keySet());

        return sb.toString();
    }
}
