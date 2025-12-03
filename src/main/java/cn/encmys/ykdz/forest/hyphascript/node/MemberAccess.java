package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.utils.ReflectionUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;

/**
 * Access member of an object.
 * <p>
 * Member could be a nested object, Java object or function etc.
 * <p>
 * Void member refer to a self reference to the target object.
 */
public class MemberAccess extends ASTNode {
    private final @NotNull ASTNode target;
    private final @NotNull String member;
    private final boolean isRead;

    public MemberAccess(@NotNull ASTNode target, @NotNull String member, boolean isRead, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.target = target;
        this.member = member;
        this.isRead = isRead;
    }

    public static @NotNull Reference findMemberFromTarget(@NotNull Value target, @NotNull String member, boolean isRead, @NotNull ASTNode node) {
        return switch (target.type()) {
            case SCRIPT_OBJECT, FUNCTION:
                try {
                    ScriptObject object = target.getAsScriptObject();
                    if (isRead) {
                        yield object.findMember(member);
                    } else {
                        yield object.findLocalMemberOrCreateOne(member);
                    }
                } catch (Exception e) {
                    throw new EvaluateException(node, e.getMessage(), e);
                }

            case ARRAY:
                yield InternalObjectManager.ARRAY_PROTOTYPE.findMember(member);

            case NUMBER:
                yield InternalObjectManager.NUMBER_PROTOTYPE.findMember(member);

            case STRING:
                yield InternalObjectManager.STRING_PROTOTYPE.findMember(member);

            case JAVA_CLASS:
                final Class<?> clazz = target.getAsClass();
                try {
                    Object field = clazz.getField(member).get(clazz);
                    yield new Reference(new Value(field));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // 为保证封装独立性
                    // 在引用时无法确认具体的方法
                    // 只能通过方法名获取可能的方法
                    // 并在调用时获得参数后筛选出目标方法
                    MethodHandle[] methodHandles;
                    try {
                        methodHandles = ReflectionUtils.getMethodHandlesByName(clazz, member);
                    } catch (Throwable ex) {
                        yield new Reference(new Value(null));
                    }

                    // 如果没有找到方法句柄，返回 null
                    if (methodHandles.length == 0) {
                        yield new Reference(new Value(null));
                    }

                    yield new Reference(new Value(methodHandles));
                }

            case NULL:
                yield new Reference(new Value(null));

            default:
                final Object targetJavaObject = target.value();
                assert targetJavaObject != null;
                try {
                    // .class 的实现
                    if (member.equals("class"))
                        yield new Reference(new Value(targetJavaObject.getClass()));

                    // VarHandle 要求在获取时得知字段类型
                    // 不适合 HyphaScript 的无类型
                    final Object field = targetJavaObject.getClass().getField(member).get(targetJavaObject);
                    yield new Reference(new Value(field));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // 为保证封装独立性
                    // 在引用时无法确认具体的方法
                    // 只能通过方法名获取可能的方法
                    // 并在调用时获得参数后筛选出目标方法
                    final MethodHandle[] methodHandles;
                    try {
                        methodHandles = ReflectionUtils.getMethodHandlesByName(targetJavaObject.getClass(), member);
                    } catch (Throwable ex) {
                        throw new EvaluateException(node, "Error when accessing member", ex);
                    }

                    if (methodHandles.length == 0) {
                        yield new Reference(new Value(null));
                    }

                    yield new Reference(new Value(methodHandles));
                }
        };
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        return findMemberFromTarget(target.evaluate(ctx).getReferredValue(), member, isRead, this);
    }

    public @NotNull ASTNode getTarget() {
        return target;
    }

    public @NotNull String getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "MemberAccess{" +
                "target=" + target +
                ", member='" + member + '\'' +
                '}';
    }
}