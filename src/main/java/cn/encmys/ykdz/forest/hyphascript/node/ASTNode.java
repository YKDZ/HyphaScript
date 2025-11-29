package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

public abstract class ASTNode {
    private final @NotNull Token startToken;
    private final @NotNull Token endToken;

    protected ASTNode(@NotNull Token startToken, @NotNull Token endToken) {
        this.startToken = startToken;
        this.endToken = endToken;
    }

    public abstract @NotNull Reference evaluate(@NotNull Context ctx);

    public @NotNull Token getStartToken() {
        return startToken;
    }

    public @NotNull Token getEndToken() {
        return endToken;
    }
}
