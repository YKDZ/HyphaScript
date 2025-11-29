package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.node.*;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ExportParser implements StatementParser {
    private static @NotNull ASTNode parseExportAllFrom(@NotNull ParseContext ctx) {
        final Token startToken = ctx.previous();
        ctx.consume(Token.Type.MUL);

        final String as = ctx.match(Token.Type.AS) ? ctx.consume(Token.Type.IDENTIFIER).value() : null;

        ctx.consume(Token.Type.FROM);
        final Token from = ctx.consume(Token.Type.STRING);

        ctx.consumeStatementEnd();

        return new ExportAllFrom(as, from.value(), startToken, ctx.previous());
    }

    private static @NotNull ASTNode parseExportMember(@NotNull ParseContext ctx) {
        final Token startToken = ctx.previous();
        final Token name = ctx.consume(Token.Type.IDENTIFIER);

        ctx.consumeStatementEnd();

        Token endToken = ctx.previous();
        return new ExportMember(name.value(), startToken, endToken);
    }

    private static @NotNull ASTNode parseExportObject(@NotNull ParseContext ctx) {
        Token startToken = ctx.previous();

        Map<String, String> exported = new HashMap<>();
        ctx.consume(Token.Type.LEFT_BRACE);
        while (!ctx.match(Token.Type.RIGHT_BRACE)) {
            Token name = ctx.consume(Token.Type.IDENTIFIER);
            Token as = name;
            if (ctx.match(Token.Type.AS)) as = ctx.consume(Token.Type.IDENTIFIER);
            exported.put(name.value(), as.value());
            if (ctx.check(Token.Type.COMMA)) ctx.consume(Token.Type.COMMA);
        }

        if (ctx.match(Token.Type.FROM)) return parseExportFrom(exported, startToken, ctx);

        ctx.consumeStatementEnd();
        return new ExportObject(exported, startToken, ctx.previous());
    }

    private static @NotNull ASTNode parseExportFrom(@NotNull Map<String, String> exported, @NotNull Token startToken, @NotNull ParseContext ctx) {
        Token from = ctx.consume(Token.Type.STRING);
        ctx.consumeStatementEnd();
        Token endToken = ctx.previous();

        return new ExportFrom(exported, from.value(), startToken, endToken);
    }

    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.EXPORT);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        if (ctx.check(Token.Type.CONST, Token.Type.LET)) {
            return new VariableDeclarationParser().parse(ctx);
        } else if (ctx.check(Token.Type.FUNCTION)) {
            return new FunctionDeclarationParser().parse(ctx);
        }

        if (ctx.check(Token.Type.LEFT_BRACE)) {
            return parseExportObject(ctx);
        } else if (ctx.check(Token.Type.MUL)) {
            return parseExportAllFrom(ctx);
        }

        return parseExportMember(ctx);
    }
}
