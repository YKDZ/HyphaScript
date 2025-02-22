package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ArrayAccess extends ASTNode {
    @NotNull
    private final MemberAccess target;
    @NotNull
    private final ASTNode from;
    @NotNull
    private final ASTNode to;
    @NotNull
    private final ASTNode step;

    public ArrayAccess(@NotNull MemberAccess target, @NotNull ASTNode from, @NotNull ASTNode to, @NotNull ASTNode step) {
        this.target = target;
        this.from = from;
        this.to = to;
        this.step = step;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference targetRef = target.evaluate(ctx);
        Reference fromRef = from.evaluate(ctx);
        Reference toRef = to.evaluate(ctx);
        Reference stepRef = step.evaluate(ctx);

        return switch (targetRef.getReferedValue().getType()) {
            case ARRAY -> {
                Map<Integer, Reference> array = targetRef.getReferedValue().getAsArray();

                if (stepRef.getReferedValue().isType(Value.Type.VOID)) {
                    yield array.get(fromRef.getReferedValue().getAsBigDecimal().intValue());
                }

                int fromIndex = fromRef.getReferedValue().isType(Value.Type.VOID) ? 0 : fromRef.getReferedValue().getAsBigDecimal().intValue();
                int toIndex = toRef.getReferedValue().isType(Value.Type.VOID) ? array.size() : toRef.getReferedValue().getAsBigDecimal().intValue();

                int stepNum = stepRef.getReferedValue().getAsBigDecimal().intValue();

                List<Reference> result = new ArrayList<>();
                if (stepNum > 0) {
                    for (int i = fromIndex; i < toIndex && i < array.size(); i += stepNum) {
                        result.add(array.get(i));
                    }
                } else {
                    for (int i = fromIndex; i > toIndex && i >= 0; i += stepNum) {
                        result.add(array.get(i));
                    }
                }
                yield new Reference(null, new Value(result.toArray()));
            }
            case STRING -> {
                String str = targetRef.getReferedValue().getAsString();

                if (stepRef.getReferedValue().isType(Value.Type.VOID)) {
                    yield new Reference(null, new Value(str.charAt(fromRef.getReferedValue().getAsBigDecimal().intValue())));
                }

                int stepNum = stepRef.getReferedValue().getAsBigDecimal().intValue();
                int fromIndex = fromRef.getReferedValue().isType(Value.Type.VOID) ? 0 : fromRef.getReferedValue().getAsBigDecimal().intValue();
                int toIndex = toRef.getReferedValue().isType(Value.Type.VOID) ? str.length() : toRef.getReferedValue().getAsBigDecimal().intValue();

                StringBuilder result = new StringBuilder();
                if (stepNum > 0) {
                    for (int i = fromIndex; i < toIndex && i < str.length(); i += stepNum) {
                        result.append(str.charAt(i));
                    }
                } else {
                    for (int i = fromIndex; i > toIndex && i >= 0; i += stepNum) {
                        result.insert(0, str.charAt(i));
                    }
                }
                yield new Reference(null, new Value(result.toString()));
            }
            case NESTED_OBJECT -> {
                Map<String, Reference> nestedObject = targetRef.getReferedValue().getAsNestedObject();

                if (!fromRef.getReferedValue().isType(Value.Type.STRING, Value.Type.NULL))
                    throw new EvaluateException(this, "Access Index of nested object must be string.");

                String index = fromRef.getReferedValue().getAsString();

                // 隐式声明不存在的嵌套对象
                if (!nestedObject.containsKey(index)) {
                    Reference init = new Reference(index, new Value(null));
                    nestedObject.put(index, init);
                    yield init;
                }

                yield nestedObject.get(index);
            }
            default -> throw new EvaluateException(this, "Error when array access");
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayAccess that = (ArrayAccess) o;
        return Objects.equals(target, that.target) && Objects.equals(from, that.from) && Objects.equals(to, that.to) && Objects.equals(step, that.step);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, from, to, step);
    }
}
