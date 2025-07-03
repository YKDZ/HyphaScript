package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Block;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BlockParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.LEFT_BRACE);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        ctx.enterLexicalScope();
        Token start = ctx.previous();
        if (start.type() == Token.Type.LEFT_BRACE) {
            List<ASTNode> statements = new ArrayList<>();

            while (!ctx.check(Token.Type.RIGHT_BRACE)) {
                statements.add(ctx.parseStatement());
            }

            Token end = ctx.consume(Token.Type.RIGHT_BRACE);
            ctx.leaveLexicalScope();
            return new Block(statements, start, end);
        } else {
            ASTNode statement = ctx.parseStatement();
            ctx.leaveLexicalScope();
            return statement;
        }
    }
}