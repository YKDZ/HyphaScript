package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public class InstanceOf extends ASTNode {
    private final @NotNull ASTNode left;
    private final @NotNull ASTNode right;

    public InstanceOf(@NotNull ASTNode left, @NotNull ASTNode right, @NotNull Token startToken,
            @NotNull Token endToken) {
        super(startToken, endToken);
        this.left = left;
        this.right = right;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value leftValue = left.evaluate(ctx).getReferredValue();
        Value rightValue = right.evaluate(ctx).getReferredValue();

        if (!rightValue.isType(Value.Type.SCRIPT_OBJECT, Value.Type.FUNCTION)) {
            throw new EvaluateException(this, "Right operands of instanceof require constructor function object.");
        }

        try {
            ScriptObject left = leftValue.getAsScriptObject();
            ScriptObject right = rightValue.getAsScriptObject();

            Value __proto__ = left.getProto();
            ScriptObject target = right.findMember("prototype").getReferredValue().getAsScriptObject();
            while (__proto__.isType(Value.Type.NULL)) {
                if (__proto__.getAsScriptObject().equals(target)) {
                    return new Reference(new Value(true));
                }
                __proto__ = __proto__.getAsScriptObject().getProto();
            }
        } catch (Exception ignored) {
        }

        return new Reference(new Value(false));
    }
}
