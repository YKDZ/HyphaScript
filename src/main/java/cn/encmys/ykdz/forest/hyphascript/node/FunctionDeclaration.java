package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

public class FunctionDeclaration extends ASTNode {
    private final @NotNull Function function;
    private final boolean isExported;

    public FunctionDeclaration(@NotNull Function function, boolean isExported, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.function = function;
        this.isExported = isExported;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        ctx.declareMember(function.getName(), function.evaluate(ctx));
        if (isExported) ctx.setExported(function.getName());
        return new Reference();
    }

    @Override
    public String toString() {
        return "FunctionDeclaration{" +
                "function=" + function +
                '}';
    }
}
