package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.ScriptArray;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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
        // Flatten nested Plus nodes to optimize chain operations
        List<ASTNode> operands = new ArrayList<>();
        flattenOperands(operands);

        // Evaluate the first operand
        Reference accumulator = operands.getFirst().evaluate(ctx);

        // Optimization for String concatenation
        if (accumulator.getReferredValue().isType(Value.Type.STRING)) {
            StringBuilder sb = new StringBuilder(accumulator.getReferredValue().getAsString());
            for (int i = 1; i < operands.size(); i++) {
                Reference next = operands.get(i).evaluate(ctx);
                // In Plus logic, if left is String, right is always converted to String
                sb.append(next.getReferredValue().getAsString());
            }
            return new Reference(new Value(sb.toString()));
        }

        // Fallback for non-string types (Number, Array, Component, etc.)
        for (int i = 1; i < operands.size(); i++) {
            Reference next = operands.get(i).evaluate(ctx);
            accumulator = performAdd(accumulator, next, ctx);
        }

        return accumulator;
    }

    private void flattenOperands(List<ASTNode> operands) {
        Deque<ASTNode> stack = new ArrayDeque<>();
        ASTNode current = this;
        while (current instanceof Plus) {
            stack.push(((Plus) current).right);
            current = ((Plus) current).left;
        }
        stack.push(current); // The leftmost leaf

        while (!stack.isEmpty()) {
            operands.add(stack.pop());
        }
    }

    private Reference performAdd(Reference leftRef, Reference rightRef, Context ctx) {
        // 字符串链接中所有对象都被当作字符串
        if (leftRef.getReferredValue().isType(Value.Type.STRING)) {
            String l = leftRef.getReferredValue().getAsString();
            String r = rightRef.getReferredValue().getAsString();
            return new Reference(new Value(l.concat(r)));
        }
        // Adventure 组件链接中所有对象都被当作组件
        else if (leftRef.getReferredValue().isType(Value.Type.ADVENTURE_COMPONENT)) {
            Component left = leftRef.getReferredValue().getAsAdventureComponent();
            Component right = rightRef.getReferredValue().getAsAdventureComponent();
            return ctx.getConfig().componentDecorationOverflow() ? new Reference(new Value(left.append(right)))
                    : new Reference(new Value(Component.empty().append(left).append(right)));
        } else if (leftRef.getReferredValue().isType(Value.Type.ARRAY)) {
            ScriptArray leftArray = leftRef.getReferredValue().getAsArray();
            // 不修改原始数组
            // 数组 + 数组
            if (rightRef.getReferredValue().isType(Value.Type.ARRAY)) {
                ScriptArray rightArray = rightRef.getReferredValue().getAsArray();
                ScriptArray newArray = new ScriptArray();
                int index = 0;
                for (int i = 0; i < leftArray.length(); i++) {
                    if (leftArray.containsKey(i)) {
                        newArray.put(index, leftArray.get(i).clone());
                    }
                    index++;
                }
                for (int i = 0; i < rightArray.length(); i++) {
                    if (rightArray.containsKey(i)) {
                        newArray.put(index, rightArray.get(i).clone());
                    }
                    index++;
                }
                return new Reference(new Value(newArray));
            }
            // 数组 + 引用
            else {
                ScriptArray newArray = new ScriptArray();
                int index = 0;
                for (int i = 0; i < leftArray.length(); i++) {
                    if (leftArray.containsKey(i)) {
                        newArray.put(index, leftArray.get(i).clone());
                    }
                    index++;
                }
                newArray.put(index, rightRef.clone());
                return new Reference(new Value(newArray));
            }
        }

        if (!leftRef.getReferredValue().isType(Value.Type.NUMBER)
                || !rightRef.getReferredValue().isType(Value.Type.NUMBER)) {
            throw new EvaluateException(this, "+ operations require number operands but given: left: "
                    + leftRef.getReferredValue() + ", right: " + rightRef.getReferredValue());
        }

        BigDecimal l = leftRef.getReferredValue().getAsBigDecimal();
        BigDecimal r = rightRef.getReferredValue().getAsBigDecimal();

        return new Reference(new Value(l.add(r)));
    }
}
