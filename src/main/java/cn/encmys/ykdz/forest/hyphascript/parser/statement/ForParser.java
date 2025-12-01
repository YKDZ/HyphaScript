package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.node.*;
import cn.encmys.ykdz.forest.hyphascript.node.pattern.*;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.FOR);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.previous();

        ctx.consume(Token.Type.LEFT_PAREN);

        if (checkForInOrOf(ctx)) {
            return parseForInOrOf(ctx, startToken);
        }

        ASTNode initialization = new Literal(false);
        if (!ctx.check(Token.Type.FINISH)) {
            initialization = ctx.parseStatement();
        } else {
            ctx.consume(Token.Type.FINISH);
        }

        ASTNode condition = new Literal(false);
        if (!ctx.check(Token.Type.FINISH)) {
            condition = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        }
        ctx.consume(Token.Type.FINISH);

        ASTNode afterThought = new Literal(false);
        if (!ctx.check(Token.Type.RIGHT_PAREN)) {
            afterThought = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        }
        ctx.consume(Token.Type.RIGHT_PAREN);

        ASTNode body = ctx.parseBlock();

        Token endToken = ctx.previous();

        return new ForLoop(initialization, condition, afterThought, body, startToken, endToken);
    }

    private boolean checkForInOrOf(ParseContext ctx) {
        int offset = 0;
        if (ctx.check(Token.Type.LET, Token.Type.CONST)) {
            offset++;
        } else if (ctx.check(Token.Type.IDENTIFIER)
                && (ctx.lookAhead(1) == Token.Type.IN || ctx.lookAhead(1) == Token.Type.OF)) {
            return true;
        }

        Token.Type type = ctx.lookAhead(offset);

        if (type == Token.Type.IDENTIFIER) {
            offset++;
            Token.Type next = ctx.lookAhead(offset);
            if (next == Token.Type.IN || next == Token.Type.OF)
                return true;
        } else if (type == Token.Type.LEFT_BRACKET) {
            offset = skipBalanced(ctx, offset, Token.Type.LEFT_BRACKET, Token.Type.RIGHT_BRACKET);
            Token.Type next = ctx.lookAhead(offset);
            if (next == Token.Type.IN || next == Token.Type.OF)
                return true;
        } else if (type == Token.Type.LEFT_BRACE) {
            offset = skipBalanced(ctx, offset, Token.Type.LEFT_BRACE, Token.Type.RIGHT_BRACE);
            Token.Type next = ctx.lookAhead(offset);
            if (next == Token.Type.IN || next == Token.Type.OF)
                return true;
        }

        return false;
    }

    private int skipBalanced(ParseContext ctx, int startOffset, Token.Type open, Token.Type close) {
        int depth = 1;
        int offset = startOffset + 1;
        while (depth > 0) {
            Token.Type t = ctx.lookAhead(offset);
            if (t == Token.Type.EOF)
                break;
            if (t == open)
                depth++;
            else if (t == close)
                depth--;
            offset++;
        }
        return offset;
    }

    private ASTNode parseForInOrOf(ParseContext ctx, Token startToken) {
        ASTNode variable;

        boolean isDecl = ctx.match(Token.Type.LET, Token.Type.CONST);
        Token declToken = isDecl ? ctx.previous() : null;
        boolean isConst = isDecl && declToken.type() == Token.Type.CONST;

        if (ctx.check(Token.Type.LEFT_BRACKET) || ctx.check(Token.Type.LEFT_BRACE)) {
            // Pattern syntax
            UnpackPattern pattern = parsePattern(ctx);
            variable = new Unpack(isConst, new Literal(new Value(null)), pattern,
                    declToken != null ? declToken : ctx.current(), ctx.current());
        } else {
            // Identifier syntax
            Token nameToken = ctx.consume(Token.Type.IDENTIFIER);
            if (isDecl) {
                variable = new Let(nameToken.value(), new Literal(new Value(null)), false, declToken, nameToken);
            } else {
                variable = new Identifier(nameToken.value(), nameToken);
            }
        }

        boolean isOf = ctx.match(Token.Type.OF);
        if (!isOf) {
            ctx.consume(Token.Type.IN);
        }

        ASTNode target = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        ctx.consume(Token.Type.RIGHT_PAREN);
        ASTNode body = ctx.parseBlock();
        Token endToken = ctx.previous();

        if (isOf) {
            return new ForOfLoop(variable, target, body, startToken, endToken);
        } else {
            return new ForInLoop(variable, target, body, startToken, endToken);
        }
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
                    subPattern = parsePattern(ctx);
                } else {
                    subPattern = new IdentifierPattern(key);
                }
                properties.put(key, subPattern);
                if (!ctx.check(Token.Type.RIGHT_BRACE))
                    ctx.consume(Token.Type.COMMA);
            }
            return new ObjectPattern(properties);
        } else if (ctx.match(Token.Type.LEFT_BRACKET)) {
            // Array pattern
            List<UnpackPattern> elements = new ArrayList<>();
            while (!ctx.match(Token.Type.RIGHT_BRACKET)) {
                elements.add(parsePattern(ctx));
                if (!ctx.check(Token.Type.RIGHT_BRACKET))
                    ctx.consume(Token.Type.COMMA);
            }
            return new ArrayPattern(elements);
        } else {
            // Identifier pattern
            Token token = ctx.consume(Token.Type.IDENTIFIER);
            return new IdentifierPattern(token.value());
        }
    }
}
