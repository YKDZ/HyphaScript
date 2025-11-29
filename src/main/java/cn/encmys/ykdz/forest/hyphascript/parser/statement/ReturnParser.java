package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Return;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class ReturnParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.RETURN);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.previous();
        ASTNode value = null;
        if (!ctx.check(Token.Type.FINISH)) {
            value = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        }
        ctx.consumeStatementEnd();
        Token endToken = ctx.previous();
        return new Return(value, startToken, endToken);
    }
}
