package cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Literal;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public class StringParser implements ExpressionParser.Prefix {
    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token token = ctx.consume(Token.Type.STRING);
        return new Literal(new Value(token.value()), token);
    }
}
