package cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix;

import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Identifier;
import cn.encmys.ykdz.forest.hyphascript.node.LiteralScriptObject;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LiteralScriptObjectParser implements ExpressionParser.Prefix {
    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx) {
        Token startToken = ctx.consume(Token.Type.LEFT_BRACE);

        Map<String, ASTNode> members = new HashMap<>();
        while (!ctx.match(Token.Type.RIGHT_BRACE)) {
            if (ctx.match(Token.Type.IDENTIFIER, Token.Type.STRING, Token.Type.NUMBER)) {
                Token identifier = ctx.previous();
                if (!ctx.match(Token.Type.COLON)) {
                    members.put(identifier.value(), new Identifier(identifier.value(), identifier));
                } else {
                    members.put(identifier.value(), ctx.parseExpression(PrecedenceTable.Precedence.LOWEST));
                }
            } else {
                throw new ParserException("Key of literal script object must be identifier, string, char or number.", startToken);
            }
            if (!ctx.check(Token.Type.RIGHT_BRACE)) ctx.consume(Token.Type.COMMA);
        }

        Token endToken = ctx.previous();
        return new LiteralScriptObject(members, startToken, endToken);
    }
}
