package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

public class Sleep extends ASTNode {
    private final @NotNull ASTNode duration;

    public Sleep(@NotNull ASTNode duration, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.duration = duration;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        try {
            Thread.sleep(duration.evaluate(ctx).getReferredValue().getAsBigDecimal().intValue());
        } catch (InterruptedException e) {
            throw new EvaluateException(this, e.getMessage(), e);
        }
        return new Reference();
    }
}
