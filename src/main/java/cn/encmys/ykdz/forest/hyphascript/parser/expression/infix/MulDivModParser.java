package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Div;
import cn.encmys.ykdz.forest.hyphascript.node.Mod;
import cn.encmys.ykdz.forest.hyphascript.node.Mul;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

public class MulDivModParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.MUL_DIV_MOD;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        Token op = ctx.consume(Token.Type.MUL, Token.Type.DIV, Token.Type.MOD);
        return switch (op.type()) {
            case MUL -> new Mul(left, ctx.parseExpression(precedence()), op, ctx.current());
            case DIV -> new Div(left, ctx.parseExpression(precedence()), op, ctx.current());
            case MOD -> new Mod(left, ctx.parseExpression(precedence()), op, ctx.current());
            default -> throw new ParserException("Provide non mul, div or mod operator to MulDivModParser", op);
        };
    }
}
