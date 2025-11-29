package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public class LogicalAnd extends ASTNode {
    private final @NotNull ASTNode left;
    private final @NotNull ASTNode right;

    public LogicalAnd(@NotNull ASTNode left, @NotNull ASTNode right, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.left = left;
        this.right = right;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        final boolean l = left.evaluate(ctx).getReferredValue().getAsBoolean();

        if (!l) return new Reference(new Value(false));

        return new Reference(new Value(right.evaluate(ctx).getReferredValue().getAsBoolean()));
    }
}
