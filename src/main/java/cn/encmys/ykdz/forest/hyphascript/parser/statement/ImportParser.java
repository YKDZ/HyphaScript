package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Import;
import cn.encmys.ykdz.forest.hyphascript.node.ImportAllAs;
import cn.encmys.ykdz.forest.hyphascript.node.ImportObjects;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.utils.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ImportParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.IMPORT);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        if (ctx.check(Token.Type.STRING))
            return parseUnifiedImport(ctx);
        else if (ctx.check(Token.Type.LEFT_BRACE))
            return parseImportObjects(ctx);
        else if (ctx.check(Token.Type.MUL))
            return parseImportAllAs(ctx);
        else
            throw new ParserException("Wrong usage of import", ctx.previous());
    }

    private @NotNull ASTNode parseUnifiedImport(@NotNull ParseContext ctx) {
        final Token startToken = ctx.previous();
        final Token target = ctx.consume(Token.Type.STRING);

        String alias = null;
        if (ctx.match(Token.Type.AS)) {
            alias = ctx.consume(Token.Type.IDENTIFIER).value();
        }

        ctx.consume(Token.Type.FROM);
        String from = ctx.consume(Token.Type.STRING).value();

        if (alias == null) {
            alias = ReflectionUtils.classNameFromPackage(target.value());
        }

        ctx.consumeStatementEnd();

        return new Import(target.value(), alias, from, startToken, ctx.previous());
    }

    private @NotNull ASTNode parseImportAllAs(@NotNull ParseContext ctx) {
        final Token startToken = ctx.previous();

        ctx.consume(Token.Type.MUL);
        ctx.consume(Token.Type.AS);
        final Token as = ctx.consume(Token.Type.IDENTIFIER);

        ctx.consume(Token.Type.FROM);
        final Token from = ctx.consume(Token.Type.STRING);

        ctx.consumeStatementEnd();

        return new ImportAllAs(as.value(), from.value(), startToken, ctx.previous());
    }

    private @NotNull ASTNode parseImportObjects(@NotNull ParseContext ctx) {
        Token startToken = ctx.previous();

        Map<String, String> imported = new HashMap<>();
        ctx.consume(Token.Type.LEFT_BRACE);
        while (!ctx.match(Token.Type.RIGHT_BRACE)) {
            Token name = ctx.consume(Token.Type.IDENTIFIER);
            Token as = name;
            if (ctx.match(Token.Type.AS))
                as = ctx.consume(Token.Type.IDENTIFIER);
            imported.put(name.value(), as.value());
            if (ctx.check(Token.Type.COMMA))
                ctx.consume(Token.Type.COMMA);
        }
        ctx.consume(Token.Type.FROM);
        Token from = ctx.consume(Token.Type.STRING);
        ctx.consumeStatementEnd();

        Token endToken = ctx.previous();

        return new ImportObjects(imported, from.value(), startToken, endToken);
    }
}
