package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

public class Continue extends ASTNode {
    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        throw new ContinueNotificationException("Need to continue loop.");
    }
}
