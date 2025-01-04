package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import org.jetbrains.annotations.NotNull;

public class Continue extends ASTNode {
    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        throw new ContinueNotificationException("Need to continue loop.");
    }
}
