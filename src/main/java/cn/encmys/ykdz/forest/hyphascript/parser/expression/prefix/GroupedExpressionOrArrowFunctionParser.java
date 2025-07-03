package cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.parser.statement.FunctionDeclarationParser;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

public class GroupedExpressionOrArrowFunctionParser implements ExpressionParser.Prefix {
    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        if (isArrowFunction(ctx)) return FunctionDeclarationParser.parseFunction(ctx, "");

        ctx.consume(Token.Type.LEFT_PAREN);
        ASTNode expr = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        ctx.consume(Token.Type.RIGHT_PAREN);
        return expr;
    }

    private boolean isArrowFunction(@NotNull ParseContext ctx) {
        int parenDepth = 1; // 已消耗了第一个左括号
        for (int i = ctx.getCurrentIndex() + 1; i < ctx.getTokens().size(); i++) {
            Token token = ctx.getTokens().get(i);
            switch (token.type()) {
                case LEFT_PAREN -> parenDepth++;
                case RIGHT_PAREN -> {
                    if (--parenDepth == 0) {
                        // 检查右括号后的箭头
                        return i + 1 < ctx.getTokens().size()
                                && ctx.getTokens().get(i + 1).type() == Token.Type.ARROW_FUNCTION;
                    }
                }
                case ARROW_FUNCTION -> {
                    // 在括号未闭合前出现箭头是非法的
                    return false;
                }
            }
        }
        return false;
    }
}
