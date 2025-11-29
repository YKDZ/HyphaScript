package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Conditional;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class ConditionalOperatorParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.CONDITIONAL;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        ctx.consume(Token.Type.QUESTION);
        ASTNode thenBranch = ctx.parseExpression(precedence());
        ctx.consume(Token.Type.COLON);
        ASTNode elseBranch = ctx.parseExpression(precedence());
        return new Conditional(left, thenBranch, elseBranch, left.getStartToken(), elseBranch.getEndToken());
    }
}
