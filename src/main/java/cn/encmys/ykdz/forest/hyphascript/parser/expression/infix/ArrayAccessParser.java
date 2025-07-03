package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.ArrayAccess;
import cn.encmys.ykdz.forest.hyphascript.node.Literal;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

public class ArrayAccessParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.MEMBER_ACCESS;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        Token lBracket = ctx.consume(Token.Type.LEFT_BRACKET);

        // 解析 from、to、step，允许为空
        ASTNode from = parseSliceComponent(ctx);
        ASTNode to = parseSliceComponent(ctx);
        ASTNode step = parseSliceComponent(ctx);

        Token rBracket = ctx.consume(Token.Type.RIGHT_BRACKET);

        return new ArrayAccess(left, from, to, step, lBracket, rBracket);
    }

    private @NotNull ASTNode parseSliceComponent(@NotNull ParseContext ctx) {
        if (ctx.match(Token.Type.COLON)) {
            return new Literal();
        }
        if (ctx.check(Token.Type.RIGHT_BRACKET)) {
            return new Literal();
        }

        ASTNode expr = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        ctx.match(Token.Type.COLON);

        return expr;
    }
}