package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Let extends ASTNode {
    @NotNull
    private final String name;
    @NotNull
    private final ASTNode initValue;
    private final boolean isExported;

    public Let(@NotNull String name, @NotNull ASTNode initValue, boolean isExported) {
        this.name = name;
        this.initValue = initValue;
        this.isExported = isExported;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference init = initValue.evaluate(ctx);
        init.setConst(false);
        init.setExported(isExported);
        ctx.declareReference(name, init);
        return new Reference();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Let let = (Let) o;
        return isExported == let.isExported && Objects.equals(name, let.name) && Objects.equals(initValue, let.initValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, initValue, isExported);
    }
}
