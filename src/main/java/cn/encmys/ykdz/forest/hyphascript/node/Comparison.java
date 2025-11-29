package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.utils.DecimalUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

public class Comparison extends ASTNode {
    private final @NotNull Token.Type operator;
    private final @NotNull ASTNode left;
    private final @NotNull ASTNode right;

    public Comparison(@NotNull Token.Type operator, @NotNull ASTNode left, @NotNull ASTNode right, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference leftRef = left.evaluate(ctx);
        Reference rightRef = right.evaluate(ctx);

        if (leftRef.getReferredValue().isType(Value.Type.NUMBER) && rightRef.getReferredValue().isType(Value.Type.NUMBER)) {
            BigDecimal lComp = leftRef.getReferredValue().getAsBigDecimal();
            BigDecimal rComp = rightRef.getReferredValue().getAsBigDecimal();
            return switch (operator) {
                case GREATER -> new Reference(new Value(lComp.compareTo(rComp) > 0));
                case LESS -> new Reference(new Value(lComp.compareTo(rComp) < 0));
                case GREATER_EQUAL -> new Reference(new Value(lComp.compareTo(rComp) >= 0));
                case LESS_EQUAL -> new Reference(new Value(lComp.compareTo(rComp) <= 0));
                case EQUAL_EQUAL ->
                        new Reference(new Value(DecimalUtils.isEquals(lComp, rComp, ctx.getConfig().equalRoundingMode())));
                case BANG_EQUALS ->
                        new Reference(new Value(!DecimalUtils.isEquals(lComp, rComp, ctx.getConfig().equalRoundingMode())));
                default -> throw new EvaluateException(this, "Unsupported operator: " + operator);
            };
        } else if (leftRef.getReferredValue().isType(Value.Type.BOOLEAN, Value.Type.NULL) && rightRef.getReferredValue().isType(Value.Type.BOOLEAN, Value.Type.NULL)) {
            return switch (operator) {
                case EQUAL_EQUAL ->
                        new Reference(new Value(leftRef.getReferredValue().getAsBoolean() == rightRef.getReferredValue().getAsBoolean()));
                case BANG_EQUALS ->
                        new Reference(new Value(leftRef.getReferredValue().getAsBoolean() != rightRef.getReferredValue().getAsBoolean()));
                default -> throw new EvaluateException(this, "Unsupported operator for boolean value: " + operator);
            };
        } else if (leftRef.getReferredValue().isType(Value.Type.STRING, Value.Type.NULL) && rightRef.getReferredValue().isType(Value.Type.STRING, Value.Type.NULL)) {
            return switch (operator) {
                case EQUAL_EQUAL ->
                        new Reference(new Value(Objects.equals(leftRef.getReferredValue().getAsString(), rightRef.getReferredValue().getAsString())));
                case BANG_EQUALS ->
                        new Reference(new Value(!Objects.equals(leftRef.getReferredValue().getAsString(), rightRef.getReferredValue().getAsString())));
                default -> throw new EvaluateException(this, "Unsupported operator for string value: " + operator);
            };
        } else if (leftRef.getReferredValue().isType(Value.Type.FUNCTION, Value.Type.SCRIPT_OBJECT) && rightRef.getReferredValue().isType(Value.Type.SCRIPT_OBJECT, Value.Type.FUNCTION)) {
            return switch (operator) {
                case EQUAL_EQUAL ->
                        new Reference(new Value(leftRef.getReferredValue().getAsScriptObject().equals(rightRef.getReferredValue().getAsScriptObject())));
                case BANG_EQUALS ->
                        new Reference(new Value(!leftRef.getReferredValue().getAsScriptObject().equals(rightRef.getReferredValue().getAsScriptObject())));
                default -> throw new EvaluateException(this, "Unsupported operator for script object: " + operator);
            };
        } else {
            return new Reference(new Value(leftRef.getReferredValue().equals(rightRef.getReferredValue().getValue())));
        }
    }
}
