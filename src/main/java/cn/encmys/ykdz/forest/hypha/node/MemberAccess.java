package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.utils.ReflectionUtils;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/**
 * Access member of an object.
 * <p>
 * Member could be a nested object, Java object or function etc.
 * <p>
 * Void member refer to a self reference to the target object.
 */
public class MemberAccess extends ASTNode {
    @NotNull
    private final ASTNode target;
    @NotNull
    private final Value member;

    public MemberAccess(@NotNull ASTNode target, @NotNull Value member) {
        this.target = target;
        this.member = member;
    }

    public MemberAccess(@NotNull Value member) {
        this.target = new Literal();
        this.member = member;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        // 对自身的引用
        if (member.isType(Value.Type.VOID)) {
            return target.evaluate(ctx);
        }

        if (!member.isType(Value.Type.STRING))
            throw new ScriptException(this, "Member reference should be casted by string value.");

        Reference targetRef = target.evaluate(ctx);
        String memberName = member.getAsString();

        switch (targetRef.getReferedValue().getType()) {
            case VOID:
                if (!ctx.hasMember(memberName)) throw new ScriptException(this, "Member " + member + " not found");
                return ctx.findMember(memberName);

            case NESTED_OBJECT:
                Map<String, Reference> nestedObject = targetRef.getReferedValue().getAsNestedObject();
                // 不支持隐式向嵌套对象中添加字段
                if (!nestedObject.containsKey(memberName))
                    throw new ScriptException(this, "Member " + member + " not found");
                return nestedObject.get(memberName);

            case JAVA_OBJECT:
                Object targetJavaObject = targetRef.getReferedValue().getValue();
                assert targetJavaObject != null;
                try {
                    // VarHandle 要求在获取时得知字段类型
                    // 不适合 HyphaScript 的无类型
                    Field field = targetJavaObject.getClass().getField(memberName);
                    return new Reference(null, new Value(field));
                } catch (NoSuchFieldException e) {
                    // 为保证封装独立性
                    // 在引用时无法确认具体的方法
                    // 只能通过方法名获取可能的方法
                    // 并在调用时获得参数后筛选出目标方法
                    MethodHandle[] methodHandles;
                    try {
                        methodHandles = ReflectionUtils.getMethodHandlesByName(targetJavaObject.getClass(), memberName);
                    } catch (Throwable ex) {
                        throw new RuntimeException(ex);
                    }

                    if (methodHandles.length == 0) {
                        return new Reference(null, new Value(null));
                    }

                    return new Reference(null, new Value(methodHandles));
                }

            case JAVA_CLASS:
                Class<?> clazz = targetRef.getReferedValue().getAsClass();
                try {
                    Field field = clazz.getField(memberName);
                    return new Reference(null, new Value(field));
                } catch (NoSuchFieldException e) {
                    // 为保证封装独立性
                    // 在引用时无法确认具体的方法
                    // 只能通过方法名获取可能的方法
                    // 并在调用时获得参数后筛选出目标方法
                    MethodHandle[] methodHandles;
                    try {
                        methodHandles = ReflectionUtils.getMethodHandlesByName(clazz, memberName);
                    } catch (Throwable ex) {
                        throw new ScriptException(this, "Member " + member + " not found", ex);
                    }

                    // 如果没有找到方法句柄，返回 null
                    if (methodHandles.length == 0) {
                        return new Reference(null, new Value(null));
                    }

                    return new Reference(null, new Value(methodHandles));
                }

            default:
                throw new ScriptException(this,
                        "Target of member access must be a VOID, NESTED_OBJECT, JAVA_CLASS, or JAVA_OBJECT but was: " + targetRef.getReferedValue().getType());
        }
    }

    public @NotNull ASTNode getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "MemberAccess{" +
                "target=" + target +
                ", memberName='" + member + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberAccess that = (MemberAccess) o;
        return Objects.equals(target, that.target) && Objects.equals(member, that.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, member);
    }
}