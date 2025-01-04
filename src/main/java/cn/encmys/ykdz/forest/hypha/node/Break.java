package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.BreakNotificationException;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import org.jetbrains.annotations.NotNull;

public class Break extends ASTNode {
    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        throw new BreakNotificationException("Need to break loop.");
    }
}
