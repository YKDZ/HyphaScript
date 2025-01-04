package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import org.jetbrains.annotations.NotNull;

public abstract class ASTNode {
    @NotNull
    public abstract Reference evaluate(@NotNull Context ctx);
}
