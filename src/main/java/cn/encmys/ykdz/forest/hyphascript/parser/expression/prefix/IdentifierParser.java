package cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix;

import cn.encmys.ykdz.forest.hyphascript.node.*;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public class IdentifierParser implements ExpressionParser.Prefix {
    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        if (ctx.check(Token.Type.IDENTIFIER) && ctx.lookAhead(1) == Token.Type.ARROW_FUNCTION) {
            return parseArrowFunction(ctx);
        }

        Token identifier = ctx.consume(Token.Type.IDENTIFIER);
        ctx.pushIdentifier(identifier.value());
        return new Identifier(identifier.value(), identifier);
    }

    private ASTNode parseArrowFunction(ParseContext ctx) {
        Token paramToken = ctx.consume(Token.Type.IDENTIFIER);
        ctx.consume(Token.Type.ARROW_FUNCTION);

        LinkedHashMap<String, ASTNode> parameters = new LinkedHashMap<>();
        parameters.put(paramToken.value(), new Literal(new Value(null)));

        ASTNode body;
        if (ctx.check(Token.Type.LEFT_BRACE)) {
            body = ctx.parseBlock();
        } else {
            body = new Return(ctx.parseExpression(PrecedenceTable.Precedence.LOWEST), paramToken, ctx.current());
        }

        return new Function("", parameters, "", body, paramToken, ctx.current());
    }
}
