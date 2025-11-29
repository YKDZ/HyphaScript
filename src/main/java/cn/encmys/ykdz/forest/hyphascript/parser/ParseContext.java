package cn.encmys.ykdz.forest.hyphascript.parser;

import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParseContext {
    private final @NotNull
    @Unmodifiable List<Token> tokens;
    private final @NotNull Parser parser;
    private @NotNull LexicalScope lexicalScope = LexicalScope.create();
    private int currentIndex = 0;

    public ParseContext(@NotNull Parser parser, @NotNull List<Token> tokens) {
        this.parser = parser;
        this.tokens = Collections.unmodifiableList(tokens);
    }

    public @NotNull ASTNode parseStatement() {
        return parser.parseStatement();
    }

    public @NotNull ASTNode parseExpression(@NotNull PrecedenceTable.Precedence precedence) {
        return parser.parseExpression(precedence);
    }

    public @NotNull ASTNode parseBlock() {
        return parser.parseBlock(this);
    }

    public boolean match(@NotNull Token.Type... types) {
        Token currentToken = tokens.get(currentIndex);
        if (Arrays.asList(types).contains(currentToken.type())) {
            currentIndex++;
            return true;
        }
        return false;
    }

    public boolean match(@NotNull Token.Type type) {
        Token currentToken = tokens.get(currentIndex);
        if (type == currentToken.type()) {
            currentIndex++;
            return true;
        }
        return false;
    }

    public boolean check(@NotNull Token.Type... types) {
        if (currentIndex > tokens.size() - 1) return false;
        return Arrays.asList(types).contains(tokens.get(currentIndex).type());
    }

    public boolean check(@NotNull Token.Type type) {
        if (currentIndex > tokens.size() - 1) return false;
        return type == tokens.get(currentIndex).type();
    }

    public @NotNull Token previous() {
        int index = Math.max(currentIndex - 1, 0);
        return tokens.get(index);
    }

    public @NotNull Token consume(@NotNull Token.Type... expects) {
        if (match(expects)) {
            return previous();
        }
        throw new ParserException("Unexpected token, expected " + Arrays.toString(expects) + " but given: " + current() + ".", current());
    }

    public @NotNull Token consume(@NotNull Token.Type expect) {
        if (match(expect)) {
            return previous();
        }
        throw new ParserException("Unexpected token, expected " + expect + " but given: " + current() + ".", current());
    }

    public void consumeStatementEnd() {
        if (match(Token.Type.FINISH)) {
            return;
        }
        if (check(Token.Type.RIGHT_BRACE) || check(Token.Type.EOF)) {
            return;
        }
        if (previous().line() < current().line()) {
            return;
        }
        throw new ParserException("Unexpected token, expected FINISH or statement end but given: " + current() + ".", current());
    }

    public @NotNull Token current() {
        return tokens.get(currentIndex);
    }

    public @NotNull Token.Type lookAhead(int n) {
        int index = currentIndex + n;
        return (index >= 0 && index < tokens.size())
                ? tokens.get(index).type()
                : Token.Type.EOF;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public @NotNull @Unmodifiable List<Token> getTokens() {
        return tokens;
    }

    public void enterLexicalScope() {
        this.lexicalScope = lexicalScope.enter();
    }

    public void leaveLexicalScope() {
        this.lexicalScope = lexicalScope.leave();
    }

    public void pushIdentifier(@NotNull String identifier) {
        lexicalScope.pushIdentifier(identifier);
    }

    public @NotNull LexicalScope getLexicalScope() {
        return lexicalScope;
    }
}
