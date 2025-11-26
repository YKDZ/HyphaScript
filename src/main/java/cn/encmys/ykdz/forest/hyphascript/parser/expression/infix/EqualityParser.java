package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Comparison;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

public class EqualityParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.EQUALITY;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        Token op = ctx.consume(Token.Type.EQUAL_EQUAL, Token.Type.BANG_EQUALS);
        ASTNode right = ctx.parseExpression(precedence());
        return new Comparison(op.type(), left, right, left.getStartToken(), right.getEndToken());
    }
}
