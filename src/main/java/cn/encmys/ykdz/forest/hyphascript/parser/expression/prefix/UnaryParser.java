package cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.UnaryOperation;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class UnaryParser implements ExpressionParser.Prefix {
    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token op = ctx.consume(Token.Type.MINUS, Token.Type.BANG, Token.Type.TYPEOF, Token.Type.NOT);
        ASTNode expr = ctx.parseExpression(PrecedenceTable.Precedence.UNARY);
        return new UnaryOperation(op.type(), expr, op, expr.getEndToken());
    }
}
