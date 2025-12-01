package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.exception.ReferenceException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.ScriptArray;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Assignment extends ASTNode {
    @NotNull
    private final Token.Type operator;
    @NotNull
    private final ASTNode target;
    @NotNull
    private final ASTNode expression;

    public Assignment(@NotNull Token.Type operator, @NotNull ASTNode target, @NotNull ASTNode expression,
                      @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.operator = operator;
        this.target = target;
        this.expression = expression;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        final Reference targetRef = target.evaluate(ctx);

        if (targetRef.isConst())
            throw new EvaluateException(this, "Tried to re-assign value to const ref: " + targetRef);

        final Reference valueRef = expression.evaluate(ctx);

        return switch (operator) {
            case EQUALS -> {
                Value val = valueRef.getReferredValue();
                if (val.isType(Value.Type.NUMBER, Value.Type.STRING, Value.Type.BOOLEAN, Value.Type.CHAR)) {
                    setValueOfRef(ctx, targetRef, new Value(val.getValue()));
                } else {
                    setValueOfRef(ctx, targetRef, val);
                }
                yield new Reference();
            }
            case COLON_EQUALS -> {
                Value val = valueRef.getReferredValue();
                if (val.isType(Value.Type.NUMBER, Value.Type.STRING, Value.Type.BOOLEAN, Value.Type.CHAR)) {
                    setValueOfRef(ctx, targetRef, new Value(val.getValue()));
                } else {
                    setValueOfRef(ctx, targetRef, val);
                }
                yield valueRef;
            }
            case PLUS_EQUALS -> {
                // += 字符拼接
                if (targetRef.getReferredValue().isType(Value.Type.STRING)
                        || targetRef.getReferredValue().isType(Value.Type.CHAR)) {
                    final String targetString = targetRef.getReferredValue().getAsString();
                    // 视作 String
                    final String valueString = valueRef.getReferredValue().toString();
                    setValueOfRef(ctx, targetRef, new Value(targetString.concat(valueString)));
                    yield new Reference();
                } else if (targetRef.getReferredValue().isType(Value.Type.ARRAY)) {
                    final ScriptArray leftArray = targetRef.getReferredValue().getAsArray();
                    // 数组 + 数组
                    if (valueRef.getReferredValue().isType(Value.Type.ARRAY)) {
                        final ScriptArray rightArray = valueRef.getReferredValue().getAsArray();
                        ScriptArray newArray = new ScriptArray();
                        int index = 0;
                        for (int i = 0; i < leftArray.length(); i++) {
                            if (leftArray.containsKey(i))
                                newArray.put(index, leftArray.get(i));
                            index++;
                        }
                        for (int i = 0; i < rightArray.length(); i++) {
                            if (rightArray.containsKey(i))
                                newArray.put(index, rightArray.get(i));
                            index++;
                        }
                        targetRef.setReferredValue(new Value(newArray), ctx.getConfig().runtimeTypeCheck());
                    }
                    // 数组 + 引用
                    else {
                        ScriptArray newArray = new ScriptArray();
                        int index = 0;
                        for (int i = 0; i < leftArray.length(); i++) {
                            if (leftArray.containsKey(i))
                                newArray.put(index, leftArray.get(i).clone());
                            index++;
                        }
                        newArray.put(index, valueRef.clone());
                        targetRef.setReferredValue(new Value(newArray), ctx.getConfig().runtimeTypeCheck());
                    }
                    yield new Reference();
                }
                if (!targetRef.getReferredValue().isType(Value.Type.NUMBER)
                        || !valueRef.getReferredValue().isType(Value.Type.NUMBER))
                    throw new EvaluateException(this, "Unsupported value type for operator");
                final BigDecimal targetDecimal = targetRef.getReferredValue().getAsBigDecimal();
                final BigDecimal valueDecimal = valueRef.getReferredValue().getAsBigDecimal();
                setValueOfRef(ctx, targetRef, new Value(targetDecimal.add(valueDecimal)));
                yield new Reference();
            }
            case MINUS_EQUALS -> {
                if (!targetRef.getReferredValue().isType(Value.Type.NUMBER)
                        || !valueRef.getReferredValue().isType(Value.Type.NUMBER))
                    throw new EvaluateException(this, "Unsupported value type for operator");
                final BigDecimal targetDecimal = targetRef.getReferredValue().getAsBigDecimal();
                final BigDecimal valueDecimal = valueRef.getReferredValue().getAsBigDecimal();
                setValueOfRef(ctx, targetRef, new Value(targetDecimal.subtract(valueDecimal)));
                yield new Reference();
            }
            case MUL_EQUALS -> {
                if (!targetRef.getReferredValue().isType(Value.Type.NUMBER)
                        || !valueRef.getReferredValue().isType(Value.Type.NUMBER))
                    throw new EvaluateException(this, "Unsupported value type for operator");
                final BigDecimal targetDecimal = targetRef.getReferredValue().getAsBigDecimal();
                final BigDecimal valueDecimal = valueRef.getReferredValue().getAsBigDecimal();
                setValueOfRef(ctx, targetRef, new Value(targetDecimal.multiply(valueDecimal)));
                yield new Reference();
            }
            case DIV_EQUALS -> {
                if (!targetRef.getReferredValue().isType(Value.Type.NUMBER)
                        || !valueRef.getReferredValue().isType(Value.Type.NUMBER))
                    throw new EvaluateException(this, "Unsupported value type for operator");
                final BigDecimal targetDecimal = targetRef.getReferredValue().getAsBigDecimal();
                final BigDecimal valueDecimal = valueRef.getReferredValue().getAsBigDecimal();
                setValueOfRef(ctx, targetRef,
                        new Value(targetDecimal.divide(valueDecimal, RoundingMode.HALF_UP)));
                yield new Reference();
            }
            case MOD_EQUALS -> {
                if (!targetRef.getReferredValue().isType(Value.Type.NUMBER)
                        || !valueRef.getReferredValue().isType(Value.Type.NUMBER))
                    throw new EvaluateException(this, "Unsupported value type for operator");
                final BigDecimal targetDecimal = targetRef.getReferredValue().getAsBigDecimal();
                final BigDecimal valueDecimal = valueRef.getReferredValue().getAsBigDecimal();
                setValueOfRef(ctx, targetRef, new Value(targetDecimal.remainder(valueDecimal)));
                yield new Reference();
            }
            case POWER_EQUALS -> {
                if (!targetRef.getReferredValue().isType(Value.Type.NUMBER)
                        || !valueRef.getReferredValue().isType(Value.Type.NUMBER))
                    throw new EvaluateException(this, "Unsupported value type for operator");
                final BigDecimal targetDecimal = targetRef.getReferredValue().getAsBigDecimal();
                final BigDecimal valueDecimal = valueRef.getReferredValue().getAsBigDecimal();
                setValueOfRef(ctx, targetRef, new Value(targetDecimal.pow(valueDecimal.intValue())));
                yield new Reference();
            }
            default -> throw new EvaluateException(this, "Unsupported operator");
        };
    }

    private void setValueOfRef(@NotNull Context ctx, @NotNull Reference ref, @NotNull Value value) {
        try {
            ref.setReferredValue(value, ctx.getConfig().runtimeTypeCheck());
        } catch (ReferenceException e) {
            throw new EvaluateException(this, e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "operator=" + operator +
                ", target=" + target +
                ", expression=" + expression +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Assignment that = (Assignment) o;
        return operator == that.operator && Objects.equals(target, that.target)
                && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, target, expression);
    }
}
