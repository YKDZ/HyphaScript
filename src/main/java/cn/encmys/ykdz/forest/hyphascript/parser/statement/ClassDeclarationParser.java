package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.ClassDeclaration;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ClassDeclarationParser implements StatementParser {
    @Override
    public boolean canParse(@NotNull ParseContext ctx) {
        return ctx.match(Token.Type.CLASS);
    }

    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.previous();
        String name = ctx.consume(Token.Type.IDENTIFIER).value();
        ASTNode parent = null;
        if (ctx.match(Token.Type.EXTENDS)) {
            parent = ctx.parseExpression(PrecedenceTable.Precedence.LOWEST);
        }

        Map<String, ASTNode> members = new HashMap<>();
        Map<String, ASTNode> staticMembers = new HashMap<>();
        ctx.consume(Token.Type.LEFT_BRACE);
        while (!ctx.match(Token.Type.RIGHT_BRACE)) {
            boolean isStatic = ctx.match(Token.Type.STATIC);
            if (ctx.match(Token.Type.IDENTIFIER, Token.Type.STRING)) {
                Token identifier = ctx.previous();
                ctx.consume(Token.Type.COLON);
                if (isStatic) {
                    staticMembers.put(identifier.value(), ctx.parseExpression(PrecedenceTable.Precedence.LOWEST));
                } else {
                    members.put(identifier.value(), ctx.parseExpression(PrecedenceTable.Precedence.LOWEST));
                }
            } else {
                throw new ParserException("Key of class declaration must be identifier or string", ctx.current());
            }
            if (!ctx.check(Token.Type.RIGHT_BRACE)) ctx.consume(Token.Type.COMMA);
        }

        Token endToken = ctx.previous();
        return new ClassDeclaration(name, parent, members, staticMembers, startToken, endToken);
    }
}
