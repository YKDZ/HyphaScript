package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.function.Function;
import cn.encmys.ykdz.forest.hypha.script.ScriptManager;
import cn.encmys.ykdz.forest.hypha.utils.ReflectionUtils;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FunctionCall extends ASTNode {
    @NotNull
    private final ASTNode target;
    @NotNull
    private final MemberAccess function;
    @NotNull
    private final List<ASTNode> arguments;

    public FunctionCall(@NotNull ASTNode target, @NotNull MemberAccess function, @NotNull List<ASTNode> arguments) {
        this.target = target;
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value functionValue = function.evaluate(ctx).getReferedValue();

        return switch (functionValue.getType()) {
            case FUNCTION -> {
                Function function = functionValue.getAsFunction();
                List<Value> evaluatedArgs = arguments.stream()
                        .map(arg -> arg.evaluate(ctx).getReferedValue())
                        .toList();
                // 从原始上下文寻找所需的 receiver
                // TODO 错误处理
                List<Value> receiverArgs = function.getReceivers().stream()
                        .map(name -> ctx.findMember(name).getReferedValue())
                        .toList();
                int functionHash = function.hashCode();
                Context calledCtx = ctx;
                if (ctx.isImportedMember(functionHash)) {
                    calledCtx = ScriptManager.getScript(ctx.getImportMemberOrigin(functionHash)).getContext();
                }
                yield function.call(calledCtx, receiverArgs, evaluatedArgs);
            }
            case JAVA_METHOD_HANDLES -> {
                Value targetValue = target.evaluate(ctx).getReferedValue();

                if (targetValue.getValue() == null) {
                    throw new ScriptException(this, "Target object is null");
                }

                // MethodHandle 的参数列表的第一个参数是实例本身（如果是实例方法）
                Object[] evaluatedArgs = Stream.concat(
                                Stream.of(targetValue.getValue()),
                                arguments.stream()
                                        .map(arg -> arg.evaluate(ctx).getReferedValue().getValue())
                        )
                        .toArray();
                Class<?>[] argClasses = Arrays.stream(evaluatedArgs)
                        .map(Object::getClass)
                        .toArray(Class[]::new);

                MethodHandle[] methodHandles = functionValue.getAsMethodHandles();

                MethodHandle matchingHandle;
                try {
                    matchingHandle = ReflectionUtils.selectFirstMatchingMethodHandle(methodHandles, argClasses);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                if (matchingHandle == null) {
                    throw new ScriptException(this, "No matching MethodHandle found for the provided argument types");
                }

                try {
                    Object result = ReflectionUtils.invokeMethodHandle(matchingHandle, evaluatedArgs);
                    yield new Reference(null, new Value(result));
                } catch (Throwable e) {
                    throw new ScriptException(this, "Error invoking Java method handle", e);
                }
            }
            default -> throw new ScriptException(this, "Member " + functionValue + " is not a function or java method.");
        };
    }

    @Override
    public String toString() {
        return "FunctionCall{" +
                "function=" + function +
                ", arguments=" + arguments +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionCall that = (FunctionCall) o;
        return Objects.equals(target, that.target) && Objects.equals(function, that.function) && Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, function, arguments);
    }
}