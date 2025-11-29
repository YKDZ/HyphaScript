package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Assignment;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class AssignmentParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.ASSIGNMENT;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        Token op = ctx.consume(Token.Type.EQUALS, Token.Type.COLON_EQUALS, Token.Type.PLUS_EQUALS, Token.Type.MINUS_EQUALS, Token.Type.DIV_EQUALS, Token.Type.MUL_EQUALS, Token.Type.MOD_EQUALS, Token.Type.POWER_EQUALS);
        ASTNode right = ctx.parseExpression(precedence());
        return new Assignment(op.type(), left, right, left.getStartToken(), right.getEndToken());
    }
}
