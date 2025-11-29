package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.BreakNotificationException;
import cn.encmys.ykdz.forest.hyphascript.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hyphascript.exception.ReturnNotificationException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TryCatch extends ASTNode {
    private final @NotNull ASTNode tryBlock;
    private final @Nullable String catchObjName;
    private final @NotNull ASTNode catchBlock;
    private final @Nullable ASTNode finallyBlock;

    public TryCatch(@NotNull ASTNode tryBlock, @Nullable String caughtObjName, @NotNull ASTNode catchBlock, @Nullable ASTNode finallyBlock, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.tryBlock = tryBlock;
        this.catchObjName = caughtObjName;
        this.catchBlock = catchBlock;
        this.finallyBlock = finallyBlock;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Context localContext = new Context(ctx);
        try {
            tryBlock.evaluate(localContext);
            return new Reference();
        }
        // try catch 不应该拦截功能性异常
        catch (ReturnNotificationException | ContinueNotificationException | BreakNotificationException e) {
            throw e;
        } catch (Throwable e) {
            if (catchObjName != null) localContext.declareMember(catchObjName, new Value(e));
            catchBlock.evaluate(localContext);
        } finally {
            if (finallyBlock != null) finallyBlock.evaluate(localContext);
        }
        return new Reference();
    }
}
