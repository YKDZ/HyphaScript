package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.function.Function;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.utils.ReflectionUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class New extends ASTNode {
    @NotNull
    private final ASTNode function;
    @NotNull
    private final List<ASTNode> arguments;

    public New(@NotNull ASTNode function, @NotNull List<ASTNode> arguments, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value functionValue = function.evaluate(ctx).getReferredValue();

        return switch (functionValue.getType()) {
            case FUNCTION, SCRIPT_OBJECT -> {
                ScriptObject functionObj;
                Function functionConstructor;
                try {
                    functionObj = functionValue.getAsScriptObject();
                    functionConstructor = functionValue.getAsFunction();
                } catch (Exception e) {
                    throw new EvaluateException(this, e.getMessage(), e);
                }
                List<Value> evaluatedArgs = arguments.stream()
                        .map(arg -> arg.evaluate(ctx).getReferredValue())
                        .toList();
                try {
                    // 若用 new 调用一个函数
                    // 则会自动创建一个以函数 prototype 为原型的对象（即该函数的实例）作为 target
                    // 以供函数上下文注册为 this
                    // 若函数本身的返回值不是一个对象
                    // 则沿用返回值
                    // 否则返回这个对象
                    Value autoObject = new Value(functionObj.newInstance());
                    Reference result = functionConstructor.call(autoObject, evaluatedArgs, ctx);
                    if (!result.getReferredValue().isType(Value.Type.SCRIPT_OBJECT)) {
                        yield new Reference(autoObject);
                    } else {
                        yield result;
                    }
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
            case JAVA_CLASS -> {
                // 获取目标类
                Class<?> targetClass = functionValue.getAsClass();

                // 解析参数
                Object[] evaluatedArgs = arguments.stream()
                        .map(arg -> arg.evaluate(ctx).getReferredValue().getValue())
                        .toArray();

                // 查找匹配的构造方法句柄
                MethodHandle matchingConstructorHandle;
                try {
                    matchingConstructorHandle = ReflectionUtils.selectFirstMatchingConstructor(targetClass, evaluatedArgs);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                if (matchingConstructorHandle == null) {
                    throw new EvaluateException(this, "No matching constructor found for the provided argument types");
                }

                try {
                    // 调用构造方法句柄
                    Object instance = ReflectionUtils.invokeMethodHandle(matchingConstructorHandle, evaluatedArgs);
                    yield new Reference(new Value(instance));
                } catch (Throwable e) {
                    throw new EvaluateException(this, "Error creating new instance via constructor handle: " + matchingConstructorHandle, e);
                }
            }

            default -> throw new EvaluateException(this, "New operator can only be applied to function or Java class");
        };
    }
}
