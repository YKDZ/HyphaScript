package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Minus;
import cn.encmys.ykdz.forest.hyphascript.node.Plus;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

public class PlusMinusParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.PLUS_MINUS;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        Token op = ctx.consume(Token.Type.PLUS, Token.Type.MINUS);
        ASTNode right = ctx.parseExpression(precedence());
        return switch (op.type()) {
            case PLUS -> new Plus(left, right, left.getStartToken(), right.getEndToken());
            case MINUS -> new Minus(left, right, left.getStartToken(), right.getEndToken());
            default -> throw new ParserException("Provide non plus or minus operator to PlusMinusParser", op);
        };
    }
}
