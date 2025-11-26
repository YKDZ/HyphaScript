package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.node.*;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public class FunctionDeclarationParser implements StatementParser {
    public static @NotNull Function parseFunction(@NotNull ParseContext ctx, @NotNull String functionName) {
        boolean isArrowFunction = functionName.isEmpty();

        Token leftParen = ctx.consume(Token.Type.LEFT_PAREN);
        LinkedHashMap<String, ASTNode> parameters = new LinkedHashMap<>();
        String uncertainParameter = "";

        if (!ctx.check(Token.Type.RIGHT_PAREN)) {
            do {
                if (ctx.match(Token.Type.SPREAD)) {
                    Token identifier = ctx.consume(Token.Type.IDENTIFIER);
                    uncertainParameter = identifier.value();
                    if (!ctx.check(Token.Type.RIGHT_PAREN)) {
                        throw new ParserException("Uncertain parameter should be the last parameter of function", identifier);
                    }
                    break;
                }
                Token identifier = ctx.consume(Token.Type.IDENTIFIER);
                ASTNode defaultValue = new Literal(new Value(null));
                if (ctx.match(Token.Type.EQUALS)) {
                    defaultValue = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
                }
                parameters.put(identifier.value(), defaultValue);
            } while (ctx.match(Token.Type.COMMA));
        }
        ctx.consume(Token.Type.RIGHT_PAREN);

        if (isArrowFunction) ctx.consume(Token.Type.ARROW_FUNCTION);

        ASTNode body;
        if (ctx.check(Token.Type.LEFT_BRACE)) {
            body = ctx.parseBlock();
        } else {
            body = new Return(ctx.parseExpression(PrecedenceTable.Precedence.LOWEST), leftParen, ctx.current());
        }

        return new Function(functionName, parameters, uncertainParameter, body, leftParen, ctx.current());
    }

    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.FUNCTION);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token functionToken = ctx.previous();
        Function function = parseFunction(ctx, ctx.consume(Token.Type.IDENTIFIER).value());
        return new FunctionDeclaration(function, functionToken, functionToken);
    }
}
