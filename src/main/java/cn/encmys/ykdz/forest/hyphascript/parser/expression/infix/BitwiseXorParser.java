package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.BitwiseXor;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class BitwiseXorParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.XOR;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        ctx.consume(Token.Type.XOR);
        ASTNode right = ctx.parseExpression(precedence());
        return new BitwiseXor(left, right, left.getStartToken(), right.getEndToken());
    }
}
