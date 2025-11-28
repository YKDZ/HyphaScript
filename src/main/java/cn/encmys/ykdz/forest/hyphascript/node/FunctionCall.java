package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.function.Function;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.utils.ReflectionUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FunctionCall extends ASTNode {
    private final @NotNull ASTNode target;
    private final @NotNull String functionName;
    private final boolean isMemberAccess;
    private @Nullable List<ASTNode> argList;
    private @Nullable Map<String, ASTNode> argMap;

    public FunctionCall(@NotNull ASTNode target, @NotNull String functionName, @NotNull List<ASTNode> argList, @NotNull Token startToken, @NotNull Token endToken, boolean isMemberAccess) {
        super(startToken, endToken);
        this.target = target;
        this.functionName = functionName;
        this.argList = argList;
        this.isMemberAccess = isMemberAccess;
    }

    public FunctionCall(@NotNull ASTNode target, @NotNull String functionName, @NotNull Map<String, ASTNode> argMap, @NotNull Token startToken, @NotNull Token endToken, boolean isMemberAccess) {
        super(startToken, endToken);
        this.target = target;
        this.functionName = functionName;
        this.argMap = argMap;
        this.isMemberAccess = isMemberAccess;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value targetValue = new Value(InternalObjectManager.OBJECT_PROTOTYPE);
        Value functionValue;

        if (isMemberAccess) {
            targetValue = target.evaluate(ctx).getReferredValue();
            functionValue = MemberAccess.findMemberFromTarget(targetValue, functionName, true, this).getReferredValue();
        } else {
            functionValue = target.evaluate(ctx).getReferredValue();
        }

        return switch (functionValue.getType()) {
            case SCRIPT_OBJECT -> {
                Function function;
                try {
                    function = functionValue.getAsScriptObject()
                            .findMember("prototype").getReferredValue().getAsScriptObject()
                            .findMember("constructor").getReferredValue().getAsFunction();
                } catch (Exception e) {
                    throw new EvaluateException(this, e.getMessage(), e);
                }
                yield callFunction(targetValue, function, ctx);
            }
            case FUNCTION -> {
                final Function function = functionValue.getAsFunction();
                yield callFunction(targetValue, function, ctx);
            }
            case JAVA_METHOD_HANDLES -> {
                if (argList == null)
                    throw new EvaluateException(this, "Java method can only be called with list parameters");

                // MethodHandle 的参数列表的第一个参数是实例本身（如果是实例方法）
                final Object[] evaluatedArgs = Stream.concat(Stream.of(targetValue.getValue()), argList.stream()
                                .map(arg -> arg.evaluate(ctx).getReferredValue().getValue()))
                        .toArray();

                MethodHandle[] methodHandles = functionValue.getAsMethodHandles();

                MethodHandle matchingHandle;
                try {
                    matchingHandle = ReflectionUtils.selectFirstMatchingMethodHandle(methodHandles, evaluatedArgs);
                } catch (Throwable e) {
                    throw new EvaluateException(this, "Error when searching matched method handle with arguments: " + Arrays.toString(evaluatedArgs), e);
                }
                if (matchingHandle == null) {
                    throw new EvaluateException(this, "No matching MethodHandle found for the provided arguments: " + Arrays.toString(evaluatedArgs));
                }

                try {
                    Object result = ReflectionUtils.invokeMethodHandle(matchingHandle, evaluatedArgs);
                    yield new Reference(new Value(result));
                } catch (Throwable e) {
                    throw new EvaluateException(this, "Error invoking Java method", e);
                }
            }

            default ->
                    throw new EvaluateException(this, "\"" + functionName + "\"" + " is not a function or java method but " + functionValue.getType());
        };
    }

    private @NotNull Reference callFunction(@NotNull Value target, @NotNull Function function, @NotNull Context ctx) {
        if (argList == null && argMap != null) {
            return callFunctionWithParaMap(target, function, argMap, ctx);
        } else if (argMap == null && argList != null) {
            return callFunctionWithParaList(target, function, argList, ctx);
        }
        throw new EvaluateException(this, "Cannot call function \"" + function.getName() + "\" with both para list and para map is null");
    }

    private @NotNull Reference callFunctionWithParaMap(@NotNull Value target, @NotNull Function function, @NotNull Map<String, ASTNode> paras, @NotNull Context ctx) {
        final Map<String, Value> evaluatedArgs = paras.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().evaluate(ctx).getReferredValue()
                ));
        return callFunction(target, function, evaluatedArgs, ctx);
    }

    private @NotNull Reference callFunctionWithParaList(@NotNull Value target, @NotNull Function function, @NotNull List<ASTNode> paras, @NotNull Context ctx) {
        final List<@NotNull Value> evaluatedArgs = paras.stream()
                .map(arg -> arg.evaluate(ctx).getReferredValue())
                .toList();
        return callFunction(target, function, evaluatedArgs, ctx);
    }

    private @NotNull Reference callFunction(@NotNull Value target, @NotNull Function function, @NotNull List<Value> args, @NotNull Context ctx) {
        try {
            return function.call(target, args, ctx);
        }
        // 不能拦截内部的 EvaluateException
        // 否则将导致函数内出现的错误在报错信息中
        // 都被指示为函数体本身
        catch (EvaluateException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluateException(this, e.getMessage());
        }
    }

    private @NotNull Reference callFunction(@NotNull Value target, @NotNull Function function, @NotNull Map<String, Value> args, @NotNull Context ctx) {
        try {
            return function.call(target, args, ctx);
        }
        // 不能拦截内部的 EvaluateException
        // 否则将导致函数内出现的错误在报错信息中
        // 都被指示为函数体本身
        catch (EvaluateException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluateException(this, e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "FunctionCall{" +
                "target=" + target +
                ", functionName='" + functionName + '\'' +
                ", argList=" + argList +
                ", argMap=" + argMap +
                '}';
    }
}