package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.MemberAccess;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

public class MemberAccessParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.MEMBER_ACCESS;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        Token dot = ctx.consume(Token.Type.DOT);
        Token member = ctx.consume(Token.Type.IDENTIFIER);
        boolean isRead = !ctx.check(Token.Type.EQUALS, Token.Type.PLUS_EQUALS, Token.Type.MUL_EQUALS, Token.Type.MUL_EQUALS, Token.Type.DIV_EQUALS, Token.Type.MOD_EQUALS);
        return new MemberAccess(left, member.value(), isRead, dot, ctx.previous());
    }
}