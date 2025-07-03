package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.TryCatch;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

public class TryCatchParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.TRY);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.previous();
        ASTNode tryBlock = ctx.parseBlock();
        ctx.consume(Token.Type.CATCH);
        String caughtObjName = null;
        if (ctx.match(Token.Type.IDENTIFIER)) {
            caughtObjName = ctx.previous().value();
        }
        ASTNode catchBlock = ctx.parseBlock();
        ASTNode finallyBlock = null;
        if (ctx.match(Token.Type.FINALLY)) {
            finallyBlock = ctx.parseBlock();
        }
        Token endToken = ctx.previous();
        return new TryCatch(tryBlock, caughtObjName, catchBlock, finallyBlock, startToken, endToken);
    }
}
