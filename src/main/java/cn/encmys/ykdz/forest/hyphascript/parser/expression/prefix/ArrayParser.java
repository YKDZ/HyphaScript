package cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Array;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArrayParser implements ExpressionParser.Prefix {
    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.consume(Token.Type.LEFT_BRACKET);
        List<ASTNode> nodes = new ArrayList<>();
        while (!ctx.match(Token.Type.RIGHT_BRACKET)) {
            nodes.add(ctx.parseExpression(PrecedenceTable.Precedence.LOWEST));
            if (ctx.check(Token.Type.COMMA) && !ctx.check(Token.Type.RIGHT_BRACKET)) ctx.consume(Token.Type.COMMA);
        }
        Token endToken = ctx.previous();
        return new Array(nodes, startToken, endToken);
    }
}
