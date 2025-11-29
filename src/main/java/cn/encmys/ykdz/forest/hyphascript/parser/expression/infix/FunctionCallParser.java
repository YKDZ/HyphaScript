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

import java.util.ArrayList;
import java.util.List;

public class FunctionCallParser implements ExpressionParser.Infix {

    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.CALL;
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        Token startToken = ctx.consume(Token.Type.LEFT_PAREN);
        List<ASTNode> arguments = new ArrayList<>();
        if (!ctx.check(Token.Type.RIGHT_PAREN)) {
            do {
                arguments.add(ctx.parseExpression(PrecedenceTable.Precedence.LOWEST));
            } while (ctx.match(Token.Type.COMMA));
        }
        Token endToken = ctx.consume(Token.Type.RIGHT_PAREN);

        if (left instanceof MemberAccess memberAccess) {
            return new FunctionCall(memberAccess.getTarget(), memberAccess.getMember(), arguments, startToken, endToken, true);
        } else if (left instanceof Identifier identifier) {
            return new FunctionCall(left, identifier.getName(), arguments, startToken, endToken, false);
        }

        return new FunctionCall(left, "", arguments, startToken, endToken, false);
    }

    private @NotNull List<ASTNode> parseArguments(@NotNull ParseContext ctx) {
        List<ASTNode> args = new ArrayList<>();
        while (!ctx.check(Token.Type.RIGHT_PAREN)) {
            // 解析表达式并将其添加到参数列表
            args.add(ctx.parseExpression(PrecedenceTable.Precedence.LOWEST));
            if (!ctx.match(Token.Type.COMMA)) break;
        }
        return args;
    }
}
