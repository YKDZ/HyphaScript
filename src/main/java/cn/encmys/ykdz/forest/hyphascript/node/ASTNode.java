package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

public abstract class ASTNode {
    @NotNull
    public abstract Reference evaluate(@NotNull Context ctx);
}
