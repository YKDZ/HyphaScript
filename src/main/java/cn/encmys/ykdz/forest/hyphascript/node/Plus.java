package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Stream;

public class Plus extends ASTNode {
    private final @NotNull ASTNode left;
    private final @NotNull ASTNode right;

    public Plus(@NotNull ASTNode left, @NotNull ASTNode right, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.left = left;
        this.right = right;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference leftRef = left.evaluate(ctx);
        Reference rightRef = right.evaluate(ctx);

        if (leftRef.getReferredValue().isType(Value.Type.STRING)) {
            // 字符串链接中所有对象都被当作字符串
            String l = leftRef.getReferredValue().getAsString();
            String r = rightRef.getReferredValue().getAsString();
            return new Reference(new Value(l.concat(r)));
        } else if (leftRef.getReferredValue().isType(Value.Type.ARRAY)) {
            Reference[] leftArray = leftRef.getReferredValue().getAsArray();
            // 不修改原始数组
            // 数组 + 数组
            if (rightRef.getReferredValue().isType(Value.Type.ARRAY)) {
                Reference[] rightArray = rightRef.getReferredValue().getAsArray();
                return new Reference(new Value(
                        Stream.concat(Arrays.stream(leftArray.clone()), Arrays.stream(rightArray.clone()))
                                .toArray(Reference[]::new)
                ));
            }
            // 数组 + 引用
            else {
                return new Reference(new Value(
                        Stream.concat(
                                Stream.of(leftArray).map(Reference::clone),
                                Stream.of(rightRef.clone())
                        )
                ));
            }
        }

        if (!leftRef.getReferredValue().isType(Value.Type.NUMBER) || !rightRef.getReferredValue().isType(Value.Type.NUMBER)) {
            throw new EvaluateException(this, "+ operations require number operands but given: left: " + leftRef.getReferredValue() + ", right: " + rightRef.getReferredValue());
        }

        BigDecimal l = leftRef.getReferredValue().getAsBigDecimal();
        BigDecimal r = rightRef.getReferredValue().getAsBigDecimal();

        return new Reference(new Value(l.add(r)));
    }
}
