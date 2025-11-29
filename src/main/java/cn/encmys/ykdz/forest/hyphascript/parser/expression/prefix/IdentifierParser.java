package cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Identifier;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class IdentifierParser implements ExpressionParser.Prefix {
    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token identifier = ctx.consume(Token.Type.IDENTIFIER);
        ctx.pushIdentifier(identifier.value());
        return new Identifier(identifier.value(), identifier);
    }
}
