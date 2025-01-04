package cn.encmys.ykdz.forest.hypha.function;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ReturnNotificationException;
import cn.encmys.ykdz.forest.hypha.node.ASTNode;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ScriptFunction implements Function {
    @NotNull
    private final List<String> receivers;
    @NotNull
    private final List<String> parameters;
    @NotNull
    private final ASTNode body;
    @Nullable
    private Context capturedContext;

    public ScriptFunction(@NotNull List<String> receivers, @NotNull List<String> parameters, @NotNull ASTNode body) {
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

        Reference result;
        try {
            result = body.evaluate(localContext);
        } catch (ReturnNotificationException e) {
            result = e.getReturnedReference();
        }
        // 若函数的返回值是函数
        // 则当前上下文链需要被储存到返回的函数中
        if (!result.getReferedValue().isType(Value.Type.VOID) && result.getReferedValue().getValue() instanceof ScriptFunction) {
           ((ScriptFunction) result.getReferedValue().getValue()).capturedContext = localContext;
        }

        return result;
    }

    @Override
    public @NotNull List<String> getReceivers() {
        return receivers;
    }

    @Override
    public @Nullable Context getCapturedContext() {
        return capturedContext;
    }

    @Override
    public void setCapturedContext(@Nullable Context capturedContext) {
        this.capturedContext = capturedContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptFunction that = (ScriptFunction) o;
        return Objects.equals(parameters, that.parameters) && Objects.equals(body, that.body) && Objects.equals(capturedContext, that.capturedContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, body, capturedContext);
    }
}
