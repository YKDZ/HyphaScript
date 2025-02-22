package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.BreakNotificationException;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

public class Break extends ASTNode {
    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        throw new BreakNotificationException("Need to break loop.");
    }
}
