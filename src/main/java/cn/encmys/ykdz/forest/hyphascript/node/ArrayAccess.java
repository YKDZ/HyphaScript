package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.ScriptArray;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArrayAccess extends ASTNode {
    @NotNull
    private final ASTNode target;
    @NotNull
    private final ASTNode from;
    @NotNull
    private final ASTNode to;
    @NotNull
    private final ASTNode step;
    private final boolean isSlice;

    public ArrayAccess(@NotNull ASTNode target, @NotNull ASTNode from, @NotNull ASTNode to, @NotNull ASTNode step,
                       @NotNull Token startToken, @NotNull Token endToken, boolean isSlice) {
        super(startToken, endToken);
        this.target = target;
        this.from = from;
        this.to = to;
        this.step = step;
        this.isSlice = isSlice;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference targetRef = target.evaluate(ctx);
        Reference fromRef = from.evaluate(ctx);
        Reference toRef = to.evaluate(ctx);
        Reference stepRef = step.evaluate(ctx);

        // 字符串和数组的切片访问索引都需要是数字
        if (targetRef.getReferredValue().isType(Value.Type.ARRAY, Value.Type.STRING)) {
            if (!fromRef.getReferredValue().isType(Value.Type.NUMBER, Value.Type.NULL, Value.Type.VOID)) {
                throw new EvaluateException(this, "From index of array access must be decimal >= 0, but given " + fromRef.getReferredValue().toReadableString());
            }
            if (!stepRef.getReferredValue().isType(Value.Type.NUMBER, Value.Type.NULL, Value.Type.VOID)) {
                throw new EvaluateException(this, "Step index of array access must be big decimal >= 0, but given " + stepRef.getReferredValue().toReadableString());
            }
            if (!toRef.getReferredValue().isType(Value.Type.NUMBER, Value.Type.NULL, Value.Type.VOID)) {
                throw new EvaluateException(this, "To index of array access must be big decimal >= 0, but given " + toRef.getReferredValue().toReadableString());
            }
        }

        return switch (targetRef.getReferredValue().getType()) {
            case ARRAY -> {
                ScriptArray array = targetRef.getReferredValue().getAsArray();

                if (!isSlice) {
                    // 处理索引访问（需确保 from 存在）
                    if (fromRef.getReferredValue().isType(Value.Type.VOID)) {
                        throw new EvaluateException(this, "Index cannot be omitted");
                    }
                    try {
                        int index = fromRef.getReferredValue().getAsBigDecimal().intValue();
                        // 处理负数索引
                        if (index < 0)
                            index += array.length();

                        Reference ref = array.get(index);
                        if (ref == null) {
                            ref = new Reference(new Value(null));
                            array.put(index, ref);
                        }
                        yield ref;
                    } catch (Exception e) {
                        throw new EvaluateException(this, "Error during array index access", e);
                    }
                }

                // 若为 VOID，默认步长为 1
                int stepNum;
                if (stepRef.getReferredValue().isType(Value.Type.VOID)) {
                    stepNum = 1;
                } else {
                    stepNum = stepRef.getReferredValue().getAsBigDecimal().intValue();
                    if (stepNum == 0) {
                        throw new EvaluateException(this, "Slice step cannot be zero");
                    }
                }

                // 若为 VOID，根据步长正负设置默认值
                int fromIndex;
                if (fromRef.getReferredValue().isType(Value.Type.VOID)) {
                    fromIndex = (stepNum > 0) ? 0 : array.length() - 1;
                } else {
                    fromIndex = fromRef.getReferredValue().getAsBigDecimal().intValue();
                    // 处理负数索引
                    if (fromIndex < 0)
                        fromIndex += array.length();
                }

                // 若为 VOID，根据步长正负设置默认值
                int toIndex;
                if (toRef.getReferredValue().isType(Value.Type.VOID)) {
                    toIndex = (stepNum > 0) ? array.length() : -1;
                } else {
                    toIndex = toRef.getReferredValue().getAsBigDecimal().intValue();
                    // 处理负数索引
                    if (toIndex < 0)
                        toIndex += array.length();
                }

                List<Reference> resultList = new ArrayList<>();
                if (stepNum > 0) {
                    // 正向切片：start <= i < end
                    for (int i = fromIndex; i < toIndex; i += stepNum) {
                        if (i < 0 || i >= array.length())
                            break;
                        Reference ref = array.get(i);
                        resultList.add(ref != null ? ref : new Reference(new Value(null)));
                    }
                } else {
                    // 逆向切片：start >= i > end
                    for (int i = fromIndex; i > toIndex; i += stepNum) {
                        if (i < 0 || i >= array.length())
                            break;
                        Reference ref = array.get(i);
                        resultList.add(ref != null ? ref : new Reference(new Value(null)));
                    }
                }

                Reference[] result = resultList.toArray(new Reference[0]);
                yield new Reference(new Value(result));
            }
            case STRING -> {
                String str = targetRef.getReferredValue().getAsString();

                if (!isSlice) {
                    // 处理索引访问（需确保 from 存在）
                    if (fromRef.getReferredValue().isType(Value.Type.VOID)) {
                        throw new EvaluateException(this, "Index cannot be omitted");
                    }
                    try {
                        int index = fromRef.getReferredValue().getAsBigDecimal().intValue();
                        // 处理负数索引
                        if (index < 0)
                            index += str.length();
                        yield new Reference(new Value(String.valueOf(str.charAt(index))));
                    } catch (Exception e) {
                        throw new EvaluateException(this, "Error during string index access", e);
                    }
                }

                // 若为 VOID，默认步长为 1
                int stepNum;
                if (stepRef.getReferredValue().isType(Value.Type.VOID)) {
                    stepNum = 1;
                } else {
                    stepNum = stepRef.getReferredValue().getAsBigDecimal().intValue();
                    if (stepNum == 0) {
                        throw new EvaluateException(this, "Slice step cannot be zero");
                    }
                }

                // 若为 VOID，根据步长正负设置默认值
                int fromIndex;
                if (fromRef.getReferredValue().isType(Value.Type.VOID)) {
                    fromIndex = (stepNum > 0) ? 0 : str.length() - 1;
                } else {
                    fromIndex = fromRef.getReferredValue().getAsBigDecimal().intValue();
                    // 处理负数索引
                    if (fromIndex < 0)
                        fromIndex += str.length();
                }

                // 若为 VOID，根据步长正负设置默认值
                int toIndex;
                if (toRef.getReferredValue().isType(Value.Type.VOID)) {
                    toIndex = (stepNum > 0) ? str.length() : -1;
                } else {
                    toIndex = toRef.getReferredValue().getAsBigDecimal().intValue();
                    // 处理负数索引
                    if (toIndex < 0)
                        toIndex += str.length();
                }

                StringBuilder result = new StringBuilder();
                if (stepNum > 0) {
                    // 正向切片：start <= i < end
                    for (int i = fromIndex; i < toIndex; i += stepNum) {
                        if (i < 0 || i >= str.length())
                            break;
                        result.append(str.charAt(i));
                    }
                } else {
                    // 逆向切片：start >= i > end
                    for (int i = fromIndex; i > toIndex; i += stepNum) {
                        if (i < 0 || i >= str.length())
                            break;
                        result.append(str.charAt(i));
                    }
                }
                yield new Reference(new Value(result.toString()));
            }
            case SCRIPT_OBJECT, FUNCTION -> {
                ScriptObject object = targetRef.getReferredValue().getAsScriptObject();

                if (!fromRef.getReferredValue().isType(Value.Type.STRING, Value.Type.NULL))
                    throw new EvaluateException(this, "Access Index of nested object must be string.");

                String index = fromRef.getReferredValue().getAsString();

                // 隐式声明不存在的嵌套对象
                if (!object.hasMember(index)) {
                    Reference init = new Reference(new Value(null));
                    object.declareMember(index, init);
                    yield init;
                }

                yield object.findMember(index);
            }
            default -> throw new EvaluateException(this,
                    "Array access can only be cast on nested objects, string and array. But given: "
                            + targetRef.getReferredValue().getType());
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ArrayAccess that = (ArrayAccess) o;
        return isSlice == that.isSlice && Objects.equals(target, that.target) && Objects.equals(from, that.from)
                && Objects.equals(to, that.to) && Objects.equals(step, that.step);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, from, to, step, isSlice);
    }
}
