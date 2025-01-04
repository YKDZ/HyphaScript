package cn.encmys.ykdz.forest.hypha.function;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.FunctionException;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

public class PackFunction implements Function {
    @NotNull
    private final List<String> receivers;
    @NotNull
    private final List<String> parameters;
    @NotNull
    private final MethodHandle body;
    @Nullable
    private Context capturedContext;

    public PackFunction(@NotNull List<String> receivers, @NotNull List<String> parameters, @NotNull MethodHandle body) {
        this.receivers = receivers;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public @NotNull Reference call(@NotNull Context ctx, @NotNull List<Value> receiversArgs, @NotNull List<Value> args) {
        if (args.size() != parameters.size())
            throw new RuntimeException("Argument amount of function mismatch");

        Context localContext = new Context(Context.Type.FUNCTION, Objects.requireNonNullElse(capturedContext, ctx));
        for (int i = 0; i < parameters.size(); i++) {
            localContext.declareReference(parameters.get(i), args.get(i));
        }
        for (int i = 0; i < receivers.size(); i++) {
            localContext.declareReference(receivers.get(i), receiversArgs.get(i));
        }

        Object result;
        try {
            result = body.invokeWithArguments(localContext);
        } catch (Throwable e) {
            throw new FunctionException(this, "Error when invoke pack function", e);
        }
        // 若函数的返回值是函数
        // 则当前上下文链需要被储存到返回的函数中
        if (result instanceof ScriptFunction) {
            ((ScriptFunction) result).setCapturedContext(localContext);
        }

        return new Reference(null, new Value(result));
    }

    @Override
    public @NotNull List<String> getReceivers() {
        return receivers;
    }

    @Override
    public Context getCapturedContext() {
        return null;
    }

    @Override
    public void setCapturedContext(@Nullable Context capturedContext) {
        this.capturedContext = capturedContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackFunction that = (PackFunction) o;
        return Objects.equals(parameters, that.parameters) && Objects.equals(body, that.body) && Objects.equals(capturedContext, that.capturedContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, body, capturedContext);
    }
}
