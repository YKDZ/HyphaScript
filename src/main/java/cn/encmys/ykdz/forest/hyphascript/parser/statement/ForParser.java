package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.ForLoop;
import cn.encmys.ykdz.forest.hyphascript.node.Literal;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import org.jetbrains.annotations.NotNull;

public class ForParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.FOR);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.previous();

        ctx.consume(Token.Type.LEFT_PAREN);

        ASTNode initialization = new Literal(false);
        if (!ctx.check(Token.Type.FINISH)) {
            initialization = ctx.parseStatement();
        } else {
            ctx.consume(Token.Type.FINISH);
        }

        ASTNode condition = new Literal(false);
        if (!ctx.check(Token.Type.FINISH)) {
            condition = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        }
        ctx.consume(Token.Type.FINISH);

        ASTNode afterThought = new Literal(false);
        if (!ctx.check(Token.Type.RIGHT_PAREN)) {
            afterThought = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        }
        ctx.consume(Token.Type.RIGHT_PAREN);

        ASTNode body = ctx.parseBlock();

        Token endToken = ctx.previous();

        return new ForLoop(initialization, condition, afterThought, body, startToken, endToken);
    }
}
