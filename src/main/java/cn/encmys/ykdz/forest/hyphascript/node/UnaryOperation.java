package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UnaryOperation extends ASTNode {
    @NotNull
    private final Token.Type operator;
    @NotNull
    private final ASTNode target;

    public UnaryOperation(@NotNull Token.Type operator, @NotNull ASTNode target, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.operator = operator;
        this.target = target;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value targetValue = target.evaluate(ctx).getReferredValue();

        return switch (operator) {
            case BANG -> {
                if (!targetValue.isType(Value.Type.BOOLEAN, Value.Type.NULL))
                    throw new IllegalArgumentException("! operator can only be casted in boolean.");
                yield new Reference(new Value(!targetValue.getAsBoolean()));
            }
            case MINUS -> {
                if (!targetValue.isType(Value.Type.NUMBER, Value.Type.NULL))
                    throw new IllegalArgumentException("- operator can only be casted in number.");
                yield new Reference(new Value(targetValue.getAsBigDecimal().negate()));
            }
            case NOT -> {
                if (!targetValue.isType(Value.Type.NUMBER, Value.Type.NULL))
                    throw new IllegalArgumentException("~ operator can only be casted in number.");
                yield new Reference(new Value(targetValue.getAsBigDecimal().toBigInteger().not()));
            }
            case TYPEOF -> new Reference(new Value(targetValue.getType().name()));
            default -> throw new EvaluateException(this, "Unary operator '" + operator + "' is not supported.");
        };
    }

    @Override
    public String toString() {
        return "UnaryOperation{" +
                "operator=" + operator +
                ", right=" + target +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnaryOperation that = (UnaryOperation) o;
        return operator == that.operator && Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, target);
    }
}
