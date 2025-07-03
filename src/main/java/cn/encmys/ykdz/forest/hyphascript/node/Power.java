package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class Power extends ASTNode {
    private final @NotNull ASTNode left;
    private final @NotNull ASTNode right;

    public Power(@NotNull ASTNode left, @NotNull ASTNode right, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.left = left;
        this.right = right;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference leftRef = left.evaluate(ctx);
        Reference rightRef = right.evaluate(ctx);

        if (!leftRef.getReferredValue().isType(Value.Type.NUMBER) || !rightRef.getReferredValue().isType(Value.Type.NUMBER)) {
            throw new EvaluateException(this, "^ operations require number operands but given: left: " + leftRef.getReferredValue() + ", right: " + rightRef.getReferredValue());
        }
        BigDecimal l = leftRef.getReferredValue().getAsBigDecimal();
        BigDecimal r = rightRef.getReferredValue().getAsBigDecimal();

        return new Reference(new Value(l.pow(r.intValue())));
    }
}
