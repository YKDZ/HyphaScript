package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.DoWhileLoop;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class DoWhileStatement implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.DO);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.previous();

        ASTNode body = ctx.parseBlock();
        ctx.consume(Token.Type.WHILE);
        ctx.consume(Token.Type.LEFT_PAREN);
        ASTNode condition = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        ctx.consume(Token.Type.RIGHT_PAREN);
        ctx.consumeStatementEnd();

        Token endToken = ctx.previous();

        return new DoWhileLoop(body, condition, startToken, endToken);
    }
}
