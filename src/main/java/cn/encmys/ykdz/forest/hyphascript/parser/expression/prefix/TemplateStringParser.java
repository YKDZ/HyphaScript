package cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Literal;
import cn.encmys.ykdz.forest.hyphascript.node.TemplateString;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TemplateStringParser implements ExpressionParser.Prefix {
    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        boolean isOptional = ctx.match(Token.Type.QUESTION);
        Token startToken = ctx.consume(Token.Type.BACKTICK);

        List<ASTNode> parts = new ArrayList<>();
        while (!ctx.match(Token.Type.BACKTICK)) {
            if (ctx.match(Token.Type.STRING)) {
                parts.add(new Literal(new Value(ctx.previous().value())));
            } else {
                parts.add(ctx.parseExpression(PrecedenceTable.Precedence.LOWEST));
            }
        }

        Token endToken = ctx.previous();
        return new TemplateString(parts, isOptional, startToken, endToken);
    }
}
