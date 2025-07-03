package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

public class Continue extends ASTNode {
    public Continue(@NotNull Token startToken) {
        super(startToken, startToken);
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        throw new ContinueNotificationException("Need to continue loop.");
    }
}
