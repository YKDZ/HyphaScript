package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

public class FunctionDeclaration extends ASTNode {
    private final @NotNull Function function;

    public FunctionDeclaration(@NotNull Function function, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.function = function;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        ctx.declareMember(function.getName(), function.evaluate(ctx));
        return new Reference();
    }

    @Override
    public String toString() {
        return "FunctionDeclaration{" +
                "function=" + function +
                '}';
    }
}
