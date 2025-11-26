package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public class BitwiseOr extends ASTNode {
    private final @NotNull ASTNode left;
    private final @NotNull ASTNode right;

    public BitwiseOr(@NotNull ASTNode left, @NotNull ASTNode right, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.left = left;
        this.right = right;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value leftValue = left.evaluate(ctx).getReferredValue();
        Value rightValue = right.evaluate(ctx).getReferredValue();
        if (leftValue.isType(Value.Type.NUMBER) && rightValue.isType(Value.Type.NUMBER)) {
            return new Reference(new Value(leftValue.getAsBigDecimal().toBigInteger().or(rightValue.getAsBigDecimal().toBigInteger())));
        }
        throw new EvaluateException(this, "Bitwise or (|) should have two number operator");
    }
}

