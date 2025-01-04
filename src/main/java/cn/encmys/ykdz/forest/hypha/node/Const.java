package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Const extends ASTNode {
    @NotNull
    private final String name;
    @NotNull
    private final ASTNode initValue;
    private final boolean isExported;

    public Const(@NotNull String name, @NotNull ASTNode initValue, boolean isExported) {
        this.name = name;
        this.initValue = initValue;
        this.isExported = isExported;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference initRef = initValue.evaluate(ctx);
        initRef.setConst(true);
        initRef.setExported(isExported);
        ctx.declareReference(name, initRef);
        return new Reference();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Const aConst = (Const) o;
        return isExported == aConst.isExported && Objects.equals(name, aConst.name) && Objects.equals(initValue, aConst.initValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, initValue, isExported);
    }
}