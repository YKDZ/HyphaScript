package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.parser.token.Token;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UnaryOperation extends ASTNode {
    @NotNull
    private final Token.Type operator;
    @NotNull
    private final ASTNode target;

    public UnaryOperation(@NotNull Token.Type operator, @NotNull ASTNode target) {
        this.operator = operator;
        this.target = target;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value targetValue = target.evaluate(ctx).getReferedValue();

        return switch (operator) {
            case BANG -> {
                if (!targetValue.isType(Value.Type.BOOLEAN)) throw new IllegalArgumentException("! operator can only be casted in boolean.");
                yield new Reference(null, new Value(!targetValue.getAsBoolean()));
            }
            case MINUS -> {
                if (targetValue.isType(Value.Type.BIG_DECIMAL)) throw new IllegalArgumentException("- operator can only be casted in number.");
                yield new Reference(null, new Value(targetValue.getAsBigDecimal().negate()));
            }
            case TYPEOF -> new Reference(null, new Value(targetValue.getType().name()));
            default -> throw new ScriptException(this, "Unary operator '" + operator + "' is not supported.");
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
