package cn.encmys.ykdz.forest.hyphascript.node.pattern;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public record IdentifierPattern(@NotNull String name) implements UnpackPattern {

    @Override
    public void apply(@NotNull ASTNode node, @NotNull Context ctx, @NotNull Value value, boolean isConst) {
        ctx.declareMember(name, new Reference(value, isConst));
    }
}
