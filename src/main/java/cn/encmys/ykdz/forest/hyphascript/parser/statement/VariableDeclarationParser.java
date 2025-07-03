package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.node.*;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        if (ctx.check(Token.Type.FINISH)) ctx.consume(Token.Type.FINISH);

        Token endToken = ctx.previous();

        return isConst ?
                new Const(identifier.value(), initValue, isExported, startToken, endToken) :
                new Let(identifier.value(), initValue, isExported, startToken, endToken);
    }

    private @NotNull ASTNode parseUnpack(@NotNull ParseContext ctx) {
        Token startToken = ctx.consume(Token.Type.LET, Token.Type.CONST);
        boolean isConst = startToken.type() == Token.Type.CONST;

        List<String> to = new ArrayList<>();
        ASTNode from;

        if (ctx.match(Token.Type.LEFT_BRACKET)) {
            // 数组解包
            while (!ctx.match(Token.Type.RIGHT_BRACKET)) {
                if (ctx.check(Token.Type.IDENTIFIER)) {
                    to.add(ctx.consume(Token.Type.IDENTIFIER).value());
                } else {
                    throw new ParserException("Expected identifier in array unpack pattern", startToken);
                }
                if (!ctx.check(Token.Type.RIGHT_BRACKET)) ctx.consume(Token.Type.COMMA);
            }
        } else if (ctx.match(Token.Type.LEFT_BRACE)) {
            // 对象解包
            while (!ctx.match(Token.Type.RIGHT_BRACE)) {
                if (ctx.check(Token.Type.IDENTIFIER)) {
                    to.add(ctx.consume(Token.Type.IDENTIFIER).value());
                } else {
                    throw new ParserException("Expected identifier in object unpack pattern", startToken);
                }
                if (!ctx.check(Token.Type.RIGHT_BRACE)) ctx.consume(Token.Type.COMMA);
            }
        } else {
            throw new ParserException("Expected '[' or '{' in unpack statement", startToken);
        }

        // 解析等号和右侧的值
        ctx.consume(Token.Type.EQUALS);
        from = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        ctx.consume(Token.Type.FINISH);

        Token endToken = ctx.previous();

        return new Unpack(isConst, from, to, startToken, endToken);
    }
}
