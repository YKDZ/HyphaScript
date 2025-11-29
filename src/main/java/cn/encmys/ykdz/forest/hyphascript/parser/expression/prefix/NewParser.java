package cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.New;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NewParser implements ExpressionParser.Prefix {
    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.consume(Token.Type.NEW);

        ASTNode constructor = ctx.parseExpression(PrecedenceTable.Precedence.CALL);

        ctx.consume(Token.Type.LEFT_PAREN);
        List<ASTNode> args = parseArguments(ctx);
        Token rParen = ctx.consume(Token.Type.RIGHT_PAREN);

        return new New(constructor, args, startToken, rParen);
    }

    private @NotNull List<ASTNode> parseArguments(@NotNull ParseContext ctx) {
        List<ASTNode> args = new ArrayList<>();
        while (!ctx.check(Token.Type.RIGHT_PAREN)) {
            args.add(ctx.parseExpression(PrecedenceTable.Precedence.LOWEST));
            if (!ctx.match(Token.Type.COMMA)) break;
        }
        return args;
    }
}