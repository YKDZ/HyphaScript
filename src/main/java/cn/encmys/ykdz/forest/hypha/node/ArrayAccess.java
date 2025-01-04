package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
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
                Reference[] array = targetRef.getReferedValue().getAsArray();

                if (stepRef.getReferedValue().isType(Value.Type.VOID)) {
                    yield array[fromRef.getReferedValue().getAsBigDecimal().intValue()];
                }

                int fromIndex = fromRef.getReferedValue().isType(Value.Type.VOID) ? 0 : fromRef.getReferedValue().getAsBigDecimal().intValue();
                int toIndex = toRef.getReferedValue().isType(Value.Type.VOID) ? array.length : toRef.getReferedValue().getAsBigDecimal().intValue();

                int stepNum = stepRef.getReferedValue().getAsBigDecimal().intValue();

                List<Reference> result = new ArrayList<>();
                if (stepNum > 0) {
                    for (int i = fromIndex; i < toIndex && i < array.length; i += stepNum) {
                        result.add(array[i]);
                    }
                } else {
                    for (int i = fromIndex; i > toIndex && i >= 0; i += stepNum) {
                        result.add(array[i]);
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
                if (fromRef.getReferedValue().isType(Value.Type.VOID) || fromRef.getReferedValue().isType(Value.Type.NULL))
                    throw new ScriptException(this, "Access Index of nested object must be string.");
                String index = fromRef.getReferedValue().toString();
                if (!nestedObject.containsKey(index)) yield new Reference(null, new Value(null));
                yield new Reference(null, nestedObject.get(index).getReferedValue());
            }
            default -> throw new ScriptException(this, "Error when array access");
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
