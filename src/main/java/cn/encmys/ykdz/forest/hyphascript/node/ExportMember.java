package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ExportMember extends ASTNode {
    private final String name;

    public ExportMember(@NotNull String name, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.name = name;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        if (!ctx.hasMember(name))
            throw new EvaluateException(this, "Exported member no exits.");
        ctx.setExported(name);
        return new Reference();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportMember exportMember = (ExportMember) o;
        return Objects.equals(name, exportMember.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
