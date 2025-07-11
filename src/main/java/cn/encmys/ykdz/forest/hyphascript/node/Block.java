package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class Block extends ASTNode {
    @NotNull
    private final List<ASTNode> statements;

    public Block(@NotNull List<ASTNode> statements, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.statements = statements;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference result = new Reference();
        Context localContext = new Context(ctx);
        for (ASTNode statement : statements) {
            result = statement.evaluate(localContext);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Block{" +
                "statements=" + statements +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return Objects.equals(statements, block.statements);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(statements);
    }
}
