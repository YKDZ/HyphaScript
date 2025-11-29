package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Comparison;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class ComparisonParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.COMPARISON;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        Token op = ctx.consume(Token.Type.LESS, Token.Type.LESS_EQUAL, Token.Type.GREATER, Token.Type.GREATER_EQUAL, Token.Type.INSTANCE_OF);
        ASTNode right = ctx.parseExpression(precedence());
        return new Comparison(op.type(), left, right, left.getStartToken(), right.getEndToken());
    }
}
