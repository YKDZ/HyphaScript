package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.parser.token.Token;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Assignment extends ASTNode {
    @NotNull
    private final Token.Type operator;
    @NotNull
    private final MemberAccess target;
    @NotNull
    private final ASTNode expression;

    public Assignment(@NotNull Token.Type operator, @NotNull MemberAccess target, @NotNull ASTNode expression) {
        this.operator = operator;
        this.target = target;
        this.expression = expression;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference targetRef = target.evaluate(ctx);
        Reference valueRef = expression.evaluate(ctx);

        if (targetRef.getReferedValue().isType(Value.Type.JAVA_FIELD))
            throw new ScriptException(this, "Do not support to modify java field directly.");

        if (targetRef.isConst())
            throw new ScriptException(this, "Tried to re-assign value to const value " + valueRef);

        return switch (operator) {
            case EQUALS -> {
                targetRef.setReferredValue(valueRef.getReferedValue());
                yield new Reference();
            }
            case COLON_EQUALS -> {
                targetRef.setReferredValue(valueRef.getReferedValue());
                yield valueRef;
            }
            case PLUS_EQUALS -> {
                // += 字符拼接
                if (targetRef.getReferedValue().isType(Value.Type.STRING) || targetRef.getReferedValue().isType(Value.Type.CHAR)) {
                    String targetString = targetRef.getReferedValue().getAsString();
                    String valueString = valueRef.getReferedValue().toString();
                    targetRef.getReferedValue().setValue(targetString.concat(valueString));
                    yield new Reference();
                }
                if (!targetRef.getReferedValue().isType(Value.Type.BIG_DECIMAL) || !valueRef.getReferedValue().isType(Value.Type.BIG_DECIMAL)) throw new ScriptException(this, "Unsupported value type for operator");
                BigDecimal targetDecimal = targetRef.getReferedValue().getAsBigDecimal();
                BigDecimal valueDecimal = valueRef.getReferedValue().getAsBigDecimal();
                targetRef.getReferedValue().setValue(valueDecimal.add(targetDecimal));
                yield new Reference();
            }
            case MINUS_EQUALS -> {
                if (!targetRef.getReferedValue().isType(Value.Type.BIG_DECIMAL) || !valueRef.getReferedValue().isType(Value.Type.BIG_DECIMAL)) throw new ScriptException(this, "Unsupported value type for operator");
                BigDecimal targetDecimal = targetRef.getReferedValue().getAsBigDecimal();
                BigDecimal valueDecimal = valueRef.getReferedValue().getAsBigDecimal();
                targetRef.getReferedValue().setValue(valueDecimal.subtract(targetDecimal));
                yield new Reference();
            }
            case MUL_EQUALS -> {
                if (!targetRef.getReferedValue().isType(Value.Type.BIG_DECIMAL) || !valueRef.getReferedValue().isType(Value.Type.BIG_DECIMAL)) throw new ScriptException(this, "Unsupported value type for operator");
                BigDecimal targetDecimal = targetRef.getReferedValue().getAsBigDecimal();
                BigDecimal valueDecimal = valueRef.getReferedValue().getAsBigDecimal();
                targetRef.getReferedValue().setValue(valueDecimal.multiply(targetDecimal));
                yield new Reference();
            }
            case DIV_EQUALS -> {
                if (!targetRef.getReferedValue().isType(Value.Type.BIG_DECIMAL) || !valueRef.getReferedValue().isType(Value.Type.BIG_DECIMAL)) throw new ScriptException(this, "Unsupported value type for operator");
                BigDecimal targetDecimal = targetRef.getReferedValue().getAsBigDecimal();
                BigDecimal valueDecimal = valueRef.getReferedValue().getAsBigDecimal();
                targetRef.getReferedValue().setValue(valueDecimal.divide(targetDecimal, RoundingMode.HALF_UP));
                yield new Reference();
            }
            case MOD_EQUALS -> {
                if (!targetRef.getReferedValue().isType(Value.Type.BIG_DECIMAL) || !valueRef.getReferedValue().isType(Value.Type.BIG_DECIMAL)) throw new ScriptException(this, "Unsupported value type for operator");
                BigDecimal targetDecimal = targetRef.getReferedValue().getAsBigDecimal();
                BigDecimal valueDecimal = valueRef.getReferedValue().getAsBigDecimal();
                targetRef.getReferedValue().setValue(valueDecimal.remainder(targetDecimal));
                yield new Reference();
            }
            default -> throw new ScriptException(this, "Unsupported operator");
        };
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assignment that = (Assignment) o;
        return operator == that.operator && Objects.equals(target, that.target) && Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, target, expression);
    }
}
