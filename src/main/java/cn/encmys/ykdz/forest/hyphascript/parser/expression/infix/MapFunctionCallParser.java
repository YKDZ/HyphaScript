package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.FunctionCall;
import cn.encmys.ykdz.forest.hyphascript.node.Identifier;
import cn.encmys.ykdz.forest.hyphascript.node.MemberAccess;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MapFunctionCallParser implements ExpressionParser.Infix {
    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.CALL;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        ctx.consume(Token.Type.LEFT_BRACE);
        Map<String, ASTNode> args = parseArguments(ctx);
        Token rParen = ctx.consume(Token.Type.RIGHT_BRACE);

        if (left instanceof MemberAccess memberAccess) {
            return new FunctionCall(memberAccess.getTarget(), memberAccess.getMember(), args, left.getStartToken(), rParen, true);
        } else if (left instanceof Identifier identifier) {
            return new FunctionCall(left, identifier.getName(), args, left.getStartToken(), rParen, false);
        }

        return new FunctionCall(left, "", args, left.getStartToken(), rParen, false);
    }

    private @NotNull Map<String, ASTNode> parseArguments(@NotNull ParseContext ctx) {
        Map<String, ASTNode> args = new HashMap<>();
        while (!ctx.check(Token.Type.RIGHT_BRACE)) {
            String paraName = ctx.consume(Token.Type.IDENTIFIER).value();
            ctx.consume(Token.Type.EQUALS);
            args.put(paraName, ctx.parseExpression(PrecedenceTable.Precedence.LOWEST));
            if (!ctx.match(Token.Type.COMMA)) break;
        }
        return args;
    }
}
