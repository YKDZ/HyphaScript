package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Conditional extends ASTNode {
    @NotNull
    private final ASTNode condition;
    @NotNull
    private final ASTNode thenBranch;
    @NotNull
    private final ASTNode elseBranch;

    public Conditional(@NotNull ASTNode condition, @NotNull ASTNode thenBranch, @NotNull ASTNode elseBranch, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value conditionValue = condition.evaluate(ctx).getReferredValue();
        return conditionValue.getAsBoolean() ? thenBranch.evaluate(ctx) : elseBranch.evaluate(ctx);
    }

    @Override
    public String toString() {
        return "Conditional{" +
                "condition=" + condition +
                ", thenBranch=" + thenBranch +
                ", elseBranch=" + elseBranch +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conditional that = (Conditional) o;
        return Objects.equals(condition, that.condition) && Objects.equals(thenBranch, that.thenBranch) && Objects.equals(elseBranch, that.elseBranch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, thenBranch, elseBranch);
    }
}
