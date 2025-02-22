package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Export extends ASTNode {
    private final String name;

    public Export(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        if (!ctx.hasMember(name))
            throw new EvaluateException(this, "Exported member no exits.");
        ctx.findMember(name).setExported(true);
        return new Reference();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Export export = (Export) o;
        return Objects.equals(name, export.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
