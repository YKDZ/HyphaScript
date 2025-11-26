package cn.encmys.ykdz.forest.hyphascript.node.pattern;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public interface UnpackPattern {
    void apply(@NotNull ASTNode node, @NotNull Context ctx, @NotNull Value value, boolean isConst);
}
