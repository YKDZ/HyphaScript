package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.WhileLoop;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

public class WhileParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.WHILE);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.previous();

        ctx.consume(Token.Type.LEFT_PAREN);
        ASTNode condition = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        ctx.consume(Token.Type.RIGHT_PAREN);
        ASTNode body = ctx.parseBlock();

        Token endToken = ctx.previous();

        return new WhileLoop(condition, body, startToken, endToken);
    }
}
