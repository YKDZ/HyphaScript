package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.utils.ReflectionUtils;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;

public class New extends ASTNode {
    @NotNull
    private final ASTNode targetClass;
    @NotNull
    private final List<ASTNode> arguments;

    public New(@NotNull ASTNode targetClass, @NotNull List<ASTNode> arguments) {
        this.targetClass = targetClass;
        this.arguments = arguments;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value targetClassValue = targetClass.evaluate(ctx).getReferedValue();

        return switch (targetClassValue.getType()) {
            case JAVA_CLASS -> {
                // 获取目标类
                Class<?> targetClass = targetClassValue.getAsClass();

                // 解析参数
                Object[] evaluatedArgs = arguments.stream()
                        .map(arg -> arg.evaluate(ctx).getReferedValue().getValue())
                        .toArray();
                Class<?>[] argClasses = Arrays.stream(evaluatedArgs)
                        .map(Object::getClass)
                        .toArray(Class[]::new);

                // 查找匹配的构造方法句柄
                MethodHandle matchingConstructorHandle;
                try {
                    matchingConstructorHandle = ReflectionUtils.selectFirstMatchingConstructor(targetClass, argClasses);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                if (matchingConstructorHandle == null) {
                    throw new ScriptException(this, "No matching constructor found for the provided argument types");
                }

                try {
                    // 调用构造方法句柄
                    Object instance = ReflectionUtils.invokeMethodHandle(matchingConstructorHandle, evaluatedArgs);
                    yield new Reference(null, new Value(instance));
                } catch (Throwable e) {
                    throw new ScriptException(this, "Error creating new instance via constructor handle", e);
                }
            }

            default -> throw new ScriptException(this, "New operator can only be applied to Java class");
        };
    }
}
