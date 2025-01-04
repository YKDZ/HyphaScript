package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.parser.token.Token;
import cn.encmys.ykdz.forest.hypha.utils.DecimalUtils;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Objects;

public class BinaryOperation extends ASTNode {
    @NotNull
    private final Token.Type operator;
    @NotNull
    private final ASTNode left;
    @NotNull
    private final ASTNode right;

    public BinaryOperation(@NotNull Token.Type operator, @NotNull ASTNode left, @NotNull ASTNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference leftRef = left.evaluate(ctx);
        Reference rightRef = right.evaluate(ctx);

        if (operator == Token.Type.DOT) {
            if (!leftRef.getReferedValue().isType(Value.Type.NESTED_OBJECT) && !leftRef.getReferedValue().isType(Value.Type.JAVA_OBJECT))
                throw new IllegalArgumentException("Operator . can only be casted in a complex value.");
            else if (!rightRef.getReferedValue().isType(Value.Type.STRING))
                throw new IllegalArgumentException("Operator . can only be casted by an identifier (alphabetic string).");

            Object object = leftRef.getReferedValue();
            String fieldName = rightRef.getReferedValue().getAsString();
            try {
                Field field = getField(object.getClass(), fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    return new Reference(null, new Value(field.get(object)));
                }
            } catch (Exception e) {
                throw new ScriptException(this, "Failed to access field '" + fieldName + "' on object of type " + object.getClass().getName());
            }
        }

        if (operator == Token.Type.PLUS) {
            if (leftRef.getReferedValue().isType(Value.Type.STRING)) {
                // 字符串链接中所有对象都被当作字符串
                String l = leftRef.getReferedValue().getAsString();
                String r = rightRef.getReferedValue().toString();
                return new Reference(null, new Value(l.concat(r)));
            }
        }

        switch (operator) {
            case PLUS, MINUS, MUL, DIV, MOD, POWER:
                if (!leftRef.getReferedValue().isType(Value.Type.BIG_DECIMAL) || !rightRef.getReferedValue().isType(Value.Type.BIG_DECIMAL)) {
                    throw new RuntimeException("Arithmetic operations require number operands.");
                }
                BigDecimal l = leftRef.getReferedValue().getAsBigDecimal();
                BigDecimal r = rightRef.getReferedValue().getAsBigDecimal();
                return switch (operator) {
                    case PLUS -> new Reference(null, new Value(l.add(r)));
                    case MINUS -> new Reference(null, new Value(l.subtract(r)));
                    case MUL -> new Reference(null, new Value(l.multiply(r)));
                    case DIV -> new Reference(null, new Value(l.divide(r, ctx.getConfig().divRoundingMode())));
                    case MOD -> new Reference(null, new Value(l.remainder(r)));
                    case POWER -> new Reference(null, new Value(l.pow(r.intValue())));
                    default -> throw new RuntimeException("Unsupported operator: " + operator);
                };

            case GREATER, LESS, GREATER_EQUAL, LESS_EQUAL, EQUAL_EQUAL, BANG_EQUAL:
                if (leftRef.getReferedValue().isType(Value.Type.BIG_DECIMAL) && rightRef.getReferedValue().isType(Value.Type.BIG_DECIMAL)) {
                    BigDecimal lComp = leftRef.getReferedValue().getAsBigDecimal();
                    BigDecimal rComp = rightRef.getReferedValue().getAsBigDecimal();
                    return switch (operator) {
                        case GREATER -> new Reference(null, new Value(lComp.compareTo(rComp) > 0));
                        case LESS -> new Reference(null, new Value(lComp.compareTo(rComp) < 0));
                        case GREATER_EQUAL -> new Reference(null, new Value(lComp.compareTo(rComp) >= 0));
                        case LESS_EQUAL -> new Reference(null, new Value(lComp.compareTo(rComp) <= 0));
                        case EQUAL_EQUAL -> new Reference(null, new Value(DecimalUtils.isEquals(lComp, rComp)));
                        case BANG_EQUAL -> new Reference(null, new Value(!DecimalUtils.isEquals(lComp, rComp)));
                        default -> throw new RuntimeException("Unsupported operator: " + operator);
                    };
                } else if (leftRef.getReferedValue().isType(Value.Type.BOOLEAN) && rightRef.getReferedValue().isType(Value.Type.BOOLEAN)) {
                    return switch (operator) {
                        case EQUAL_EQUAL -> new Reference(null, new Value(leftRef.getReferedValue().getAsBoolean() == rightRef.getReferedValue().getAsBoolean()));
                        case BANG_EQUAL ->new Reference(null, new Value(leftRef.getReferedValue().getAsBoolean() != rightRef.getReferedValue().getAsBoolean()));
                        default -> throw new RuntimeException("Unsupported operator for boolean value: " + operator);
                    };
                } else if (leftRef.getReferedValue().isType(Value.Type.STRING) && rightRef.getReferedValue().isType(Value.Type.STRING)) {
                    return switch (operator) {
                        case EQUAL_EQUAL -> new Reference(null, new Value(Objects.equals(leftRef.getReferedValue().getAsString(), rightRef.getReferedValue().getAsString())));
                        case BANG_EQUAL -> new Reference(null, new Value(!Objects.equals(leftRef.getReferedValue().getAsString(), rightRef.getReferedValue().getAsString())));
                        default -> throw new RuntimeException("Unsupported operator for string value: " + operator);
                    };
                }

            case LOGIC_AND, LOGIC_OR, BIT_AND, BIT_OR:
                if (!leftRef.getReferedValue().isType(Value.Type.BOOLEAN) || !rightRef.getReferedValue().isType(Value.Type.BOOLEAN)) {
                    throw new RuntimeException("Logical operations require boolean operands.");
                }
                boolean lBool = leftRef.getReferedValue().getAsBoolean();
                boolean rBool = rightRef.getReferedValue().getAsBoolean();
                return switch (operator) {
                    case LOGIC_AND -> new Reference(null, new Value(lBool && rBool));
                    case LOGIC_OR -> new Reference(null, new Value(lBool || rBool));
                    case BIT_AND -> new Reference(null, new Value(lBool & rBool));
                    case BIT_OR -> new Reference(null, new Value(lBool | rBool));
                    default -> throw new RuntimeException("Unsupported operator: " + operator);
                };

            default:
                throw new RuntimeException("Unsupported operator: " + operator);
        }
    }

    @Contract(pure = true)
    private Field getField(@NotNull Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "BinaryOperation{" +
                "operator=" + operator +
                ", left=" + left +
                ", right=" + right +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BinaryOperation that = (BinaryOperation) o;
        return operator == that.operator && Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, left, right);
    }
}
