package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Break;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class BreakParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.BREAK);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.previous();
        ctx.consumeStatementEnd();
        return new Break(startToken);
    }
}
