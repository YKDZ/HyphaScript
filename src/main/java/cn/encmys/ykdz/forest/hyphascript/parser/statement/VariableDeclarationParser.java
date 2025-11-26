package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.node.*;
import cn.encmys.ykdz.forest.hyphascript.node.pattern.ArrayPattern;
import cn.encmys.ykdz.forest.hyphascript.node.pattern.IdentifierPattern;
import cn.encmys.ykdz.forest.hyphascript.node.pattern.ObjectPattern;
import cn.encmys.ykdz.forest.hyphascript.node.pattern.UnpackPattern;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableDeclarationParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.check(Token.Type.LET, Token.Type.CONST);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        if (ctx.lookAhead(1) != Token.Type.LEFT_BRACE &&
                ctx.lookAhead(1) != Token.Type.LEFT_BRACKET) {
            return parseNormal(ctx);
        } else {
            return parseUnpack(ctx);
        }
    }

    private @NotNull ASTNode parseNormal(@NotNull ParseContext ctx) {
        boolean isExported = ctx.previous().type() == Token.Type.EXPORT;

        Token startToken = ctx.consume(Token.Type.LET, Token.Type.CONST);
        boolean isConst = startToken.type() == Token.Type.CONST;

        Token identifier = ctx.consume(Token.Type.IDENTIFIER);
        ASTNode initValue = new Literal(new Value(null));
        if (ctx.match(Token.Type.EQUALS, Token.Type.COLON_EQUALS, Token.Type.PLUS_EQUALS, Token.Type.MINUS_EQUALS, Token.Type.DIV_EQUALS, Token.Type.MUL_EQUALS, Token.Type.MOD_EQUALS)) {
            initValue = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        }
        ctx.consumeStatementEnd();

        Token endToken = ctx.previous();

        return isConst ?
                new Const(identifier.value(), initValue, isExported, startToken, endToken) :
                new Let(identifier.value(), initValue, isExported, startToken, endToken);
    }

    private @NotNull ASTNode parseUnpack(@NotNull ParseContext ctx) {
        Token startToken = ctx.consume(Token.Type.LET, Token.Type.CONST);
        boolean isConst = startToken.type() == Token.Type.CONST;

        UnpackPattern pattern = parsePattern(ctx);

        // 解析等号和右侧的值
        ctx.consume(Token.Type.EQUALS);
        ASTNode from = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        ctx.consumeStatementEnd();

        Token endToken = ctx.previous();

        return new Unpack(isConst, from, pattern, startToken, endToken);
    }

    private UnpackPattern parsePattern(ParseContext ctx) {
        if (ctx.match(Token.Type.LEFT_BRACE)) {
            // Object pattern
            Map<String, UnpackPattern> properties = new HashMap<>();
            while (!ctx.match(Token.Type.RIGHT_BRACE)) {
                Token keyToken = ctx.consume(Token.Type.IDENTIFIER);
                String key = keyToken.value();
                UnpackPattern subPattern;
                if (ctx.match(Token.Type.COLON)) {
                    // Nested or renamed
                    subPattern = parsePattern(ctx);
                } else {
                    // Shorthand
                    subPattern = new IdentifierPattern(key);
                }
                properties.put(key, subPattern);
                if (!ctx.check(Token.Type.RIGHT_BRACE)) ctx.consume(Token.Type.COMMA);
            }
            return new ObjectPattern(properties);
        } else if (ctx.match(Token.Type.LEFT_BRACKET)) {
            // Array pattern
            List<UnpackPattern> elements = new ArrayList<>();
            while (!ctx.match(Token.Type.RIGHT_BRACKET)) {
                elements.add(parsePattern(ctx));
                if (!ctx.check(Token.Type.RIGHT_BRACKET)) ctx.consume(Token.Type.COMMA);
            }
            return new ArrayPattern(elements);
        } else {
            // Identifier pattern
            Token token = ctx.consume(Token.Type.IDENTIFIER);
            return new IdentifierPattern(token.value());
        }
    }
}
