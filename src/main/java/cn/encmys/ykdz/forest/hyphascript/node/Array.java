package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Array extends ASTNode {
    private final @NotNull List<ASTNode> values;

    public Array(@NotNull List<ASTNode> values, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.values = values;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        return new Reference(new Value(
                values.stream()
                        .map((node) -> node.evaluate(ctx))
                        .toArray(Reference[]::new)
        ));
    }
}
