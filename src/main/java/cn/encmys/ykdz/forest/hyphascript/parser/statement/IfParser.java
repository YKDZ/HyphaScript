package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Conditional;
import cn.encmys.ykdz.forest.hyphascript.node.Literal;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class IfParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.IF);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token start = ctx.previous();

        ctx.consume(Token.Type.LEFT_PAREN);
        ASTNode condition = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        ctx.consume(Token.Type.RIGHT_PAREN);

        ASTNode thenBranch = ctx.parseBlock();
        ASTNode elseBranch = parseElse(ctx);

        return new Conditional(condition, thenBranch, elseBranch, start, ctx.previous());
    }

    private @NotNull ASTNode parseElse(@NotNull ParseContext ctx) {
        if (!ctx.match(Token.Type.ELSE)) return new Literal();

        if (ctx.match(Token.Type.ELSE_IF)) {
            return parse(ctx);
        }

        return ctx.parseBlock();
    }
}