package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.ArrayAccess;
import cn.encmys.ykdz.forest.hyphascript.node.Literal;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import org.jetbrains.annotations.NotNull;

public class ArrayAccessParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.MEMBER_ACCESS;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        ctx.consume(Token.Type.LEFT_BRACKET);

        ASTNode from = new Literal(false);
        ASTNode to = new Literal(false);
        ASTNode step = new Literal(false);
        boolean isSlice = false;

        if (!ctx.check(Token.Type.COLON) && !ctx.check(Token.Type.RIGHT_BRACKET)) {
            from = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        }

        if (ctx.match(Token.Type.COLON)) {
            isSlice = true;
            if (!ctx.check(Token.Type.COLON) && !ctx.check(Token.Type.RIGHT_BRACKET)) {
                to = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
            }

            if (ctx.match(Token.Type.COLON)) {
                if (!ctx.check(Token.Type.RIGHT_BRACKET)) {
                    step = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
                }
            }
        }

        Token rBracket = ctx.consume(Token.Type.RIGHT_BRACKET);

        return new ArrayAccess(left, from, to, step, left.getStartToken(), rBracket, isSlice);
    }
}