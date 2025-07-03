package cn.encmys.ykdz.forest.hyphascript.lexer;

import cn.encmys.ykdz.forest.hyphascript.exception.LexerException;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private static final char EOF = '\0';
    private static final char NEWLINE = '\n';
    private static final char BACKTICK = '`';
    private static final char DOUBLE_QUOTE = '"';
    private static final char SINGLE_QUOTE = '\'';
    private static final char ESCAPE = '\\';

    private static final Map<String, Token.Type> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("let", Token.Type.LET);
        KEYWORDS.put("function", Token.Type.FUNCTION);
        KEYWORDS.put("if", Token.Type.IF);
        KEYWORDS.put("else", Token.Type.ELSE);
        KEYWORDS.put("true", Token.Type.BOOLEAN);
        KEYWORDS.put("false", Token.Type.BOOLEAN);
        KEYWORDS.put("null", Token.Type.NULL);
        KEYWORDS.put("return", Token.Type.RETURN);
        KEYWORDS.put("while", Token.Type.WHILE);
        KEYWORDS.put("do", Token.Type.DO);
        KEYWORDS.put("for", Token.Type.FOR);
        KEYWORDS.put("break", Token.Type.BREAK);
        KEYWORDS.put("continue", Token.Type.CONTINUE);
        KEYWORDS.put("typeof", Token.Type.TYPEOF);
        KEYWORDS.put("const", Token.Type.CONST);
        KEYWORDS.put("export", Token.Type.EXPORT);
        KEYWORDS.put("import", Token.Type.IMPORT);
        KEYWORDS.put("as", Token.Type.AS);
        KEYWORDS.put("from", Token.Type.FROM);
        KEYWORDS.put("new", Token.Type.NEW);
        KEYWORDS.put("try", Token.Type.TRY);
        KEYWORDS.put("catch", Token.Type.CATCH);
        KEYWORDS.put("finally", Token.Type.FINALLY);
        KEYWORDS.put("class", Token.Type.CLASS);
        KEYWORDS.put("extends", Token.Type.EXTENDS);
        KEYWORDS.put("static", Token.Type.STATIC);
        KEYWORDS.put("async", Token.Type.ASYNC);
        KEYWORDS.put("await", Token.Type.AWAIT);
        KEYWORDS.put("instanceof", Token.Type.INSTANCE_OF);
        KEYWORDS.put("sleep", Token.Type.SLEEP);
        KEYWORDS.put("java", Token.Type.JAVA);
    }

    private final @NotNull String script;
    private final @NotNull List<Token> tokens = new ArrayList<>();
    private int position = 0;
    private char currentChar;
    private int currentLine = 1;
    private int currentColumn = 1;

    public Lexer(@NotNull String script) {
        this.script = script;
        this.currentChar = script.isEmpty() ? EOF : script.charAt(0);
    }

    public @NotNull List<Token> tokenize() {
        tokens.clear();

        while (currentChar != EOF) {
            Token token = nextToken();
            tokens.add(token);
            if (token.type() == Token.Type.EOF) break;
        }

        handleOmit();

        if (!tokens.isEmpty() && tokens.getLast().type() != Token.Type.EOF) {
            tokens.add(new Token(Token.Type.EOF, "", currentLine, currentColumn));
        }

        return tokens;
    }

    private void handleOmit() {
        // 针对单行表达式，如果脚本中没有换行，则需要检查末尾是否缺少分号
        if (!script.contains("\n")) {
            int lastNonEofIndex = -1;
            for (int i = tokens.size() - 1; i >= 0; i--) {
                if (tokens.get(i).type() != Token.Type.EOF) {
                    lastNonEofIndex = i;
                    break;
                }
            }
            if (lastNonEofIndex != -1) {
                Token lastToken = tokens.get(lastNonEofIndex);
                if (lastToken.type() != Token.Type.FINISH) {
                    int line = lastToken.line();
                    int column = lastToken.column() + lastToken.value().length();
                    Token finishToken = new Token(Token.Type.FINISH, ";", line, column);
                    tokens.add(lastNonEofIndex + 1, finishToken);
                }
            }
        }
    }

    private void advance() {
        if (currentChar == NEWLINE) {
            currentLine++;
            currentColumn = 1;
        } else {
            currentColumn++;
        }

        position++;
        currentChar = position < script.length() ? script.charAt(position) : EOF;
    }

    private void skipWhitespaceAndComments() {
        while (Character.isWhitespace(currentChar) || isCommentStart()) {
            if (Character.isWhitespace(currentChar)) {
                advance();
            } else if (isCommentStart()) {
                handleComments();
            }
        }
    }

    private boolean isCommentStart() {
        return currentChar == '/' && (nextChar() == '/' || nextChar() == '*');
    }

    private void handleComments() {
        advance(); // 跳过 '/'
        if (currentChar == '/') {
            skipSingleLineComment();
        } else if (currentChar == '*') {
            skipMultiLineComment();
        }
    }

    private void skipSingleLineComment() {
        while (currentChar != NEWLINE && currentChar != EOF) {
            advance();
        }
        advance(); // 跳过换行符
    }

    private void skipMultiLineComment() {
        advance(); // 跳过 '*'
        while (!(currentChar == '*' && nextChar() == '/')) {
            if (currentChar == EOF) {
                throw new LexerException("Unterminated multi-line comment", currentLine, currentColumn);
            }
            advance();
        }
        advance(); // 跳过 '*'
        advance(); // 跳过 '/'
    }

    private char nextChar() {
        return position + 1 < script.length() ? script.charAt(position + 1) : EOF;
    }

    private Token nextToken() {
        skipWhitespaceAndComments();

        if (currentChar == EOF) {
            return new Token(Token.Type.EOF, "", currentLine, currentColumn);
        }

        return switch (currentChar) {
            case '(' -> parseSingleCharToken(Token.Type.LEFT_PAREN);
            case ')' -> parseSingleCharToken(Token.Type.RIGHT_PAREN);
            case '{' -> parseSingleCharToken(Token.Type.LEFT_BRACE);
            case '}' -> parseSingleCharToken(Token.Type.RIGHT_BRACE);
            case '[' -> parseSingleCharToken(Token.Type.LEFT_BRACKET);
            case ']' -> parseSingleCharToken(Token.Type.RIGHT_BRACKET);
            case ',' -> parseSingleCharToken(Token.Type.COMMA);
            case ';' -> parseSingleCharToken(Token.Type.FINISH);
            case '?' -> parseSingleCharToken(Token.Type.QUESTION);
            case '=' -> parseComplexOperator('=', Token.Type.EQUALS, Token.Type.EQUAL_EQUAL);
            case ':' -> parseComplexOperator(':', Token.Type.COLON, Token.Type.COLON_EQUALS);
            case '+' -> parseComplexOperator('+', Token.Type.PLUS, Token.Type.PLUS_EQUALS);
            case '-' -> parseComplexOperator('-', Token.Type.MINUS, Token.Type.MINUS_EQUALS);
            case '*' -> parseComplexOperator('*', Token.Type.MUL, Token.Type.MUL_EQUALS);
            case '/' -> parseComplexOperator('/', Token.Type.DIV, Token.Type.DIV_EQUALS);
            case '%' -> parseComplexOperator('%', Token.Type.MOD, Token.Type.MOD_EQUALS);
            case '^' -> parseComplexOperator('^', Token.Type.POWER, Token.Type.POWER_EQUALS);
            case '!', '<', '>' -> parseComparisonOperator();
            case '|', '&' -> parseLogicalOperator();
            case '.' -> {
                if (nextChar() == '.' && position + 1 < script.length() && script.charAt(position + 1) == '.'
                        && position + 2 < script.length() && script.charAt(position + 2) == '.') {
                    advance();
                    advance();
                    advance();
                    yield new Token(Token.Type.SPREAD, "...", currentLine, currentColumn);
                }
                yield parseSingleCharToken(Token.Type.DOT);
            }
            case DOUBLE_QUOTE -> readString();
            case BACKTICK -> readTemplateString();
            case SINGLE_QUOTE -> readChar();
            default -> {
                if (Character.isDigit(currentChar)) yield readNumber();
                if (isIdentifierStart(currentChar)) yield readIdentifier();
                throw new LexerException("Unexpected character: " + currentChar, currentLine, currentColumn);
            }
        };
    }

    @Contract("_ -> new")
    private @NotNull Token parseSingleCharToken(Token.Type type) {
        String value = String.valueOf(currentChar);
        advance();
        return new Token(type, value, currentLine, currentColumn);
    }

    @Contract("_, _, _ -> new")
    private @NotNull Token parseComplexOperator(char op, Token.Type baseType, Token.Type compoundType) {
        advance();
        if (currentChar == '=') {
            advance();
            return new Token(compoundType, op + "=", currentLine, currentColumn);
        } else if (currentChar == '>') {
            advance();
            return new Token(Token.Type.ARROW_FUNCTION, "=>", currentLine, currentColumn);
        }
        return new Token(baseType, String.valueOf(op), currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token parseComparisonOperator() {
        char first = currentChar;
        advance();
        if (currentChar == '=') {
            advance();
            return new Token(getComparisonType(first + "="), first + "=", currentLine, currentColumn);
        }
        return new Token(getComparisonType(String.valueOf(first)), String.valueOf(first), currentLine, currentColumn);
    }

    private @NotNull Token.Type getComparisonType(@NotNull String op) {
        return switch (op) {
            case ">" -> Token.Type.GREATER;
            case "<" -> Token.Type.LESS;
            case ">=" -> Token.Type.GREATER_EQUAL;
            case "<=" -> Token.Type.LESS_EQUAL;
            case "!=" -> Token.Type.BANG_EQUALS;
            case "!" -> Token.Type.BANG;
            default -> throw new LexerException("Invalid comparison operator: " + op, currentLine, currentColumn);
        };
    }

    @Contract(" -> new")
    private @NotNull Token parseLogicalOperator() {
        char first = currentChar;
        advance();
        if (currentChar == first) {
            advance();
            return new Token(first == '&' ? Token.Type.LOGIC_AND : Token.Type.LOGIC_OR,
                    first + "" + first, currentLine, currentColumn);
        }
        return new Token(first == '&' ? Token.Type.BIT_AND : Token.Type.BIT_OR,
                String.valueOf(first), currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token readTemplateString() {
        advance(); // 跳过起始反引号 `
        tokens.add(new Token(Token.Type.BACKTICK, "`", currentLine, currentColumn));

        StringBuilder textBuffer = new StringBuilder();

        while (currentChar != BACKTICK && currentChar != EOF) {
            if (currentChar == '$' && nextChar() == '{') {
                flushTextBuffer(textBuffer);

                advance(); // 跳过 $
                advance(); // 跳过 {

                // 持续解析直到遇到闭合 }
                while (currentChar != '}' && currentChar != EOF) {
                    tokens.add(nextToken());
                }
                if (currentChar == '}') advance(); // 跳过闭合 }

            } else {
                // 普通字符直接累积
                textBuffer.append(currentChar);
                advance();
            }
        }

        // 处理末尾剩余文本
        flushTextBuffer(textBuffer);
        advance(); // 跳过结束反引号`
        return new Token(Token.Type.BACKTICK, "`", currentLine, currentColumn);
    }

    private void flushTextBuffer(@NotNull StringBuilder buffer) {
        if (!buffer.isEmpty()) {
            tokens.add(new Token(Token.Type.STRING, buffer.toString(), currentLine, currentColumn));
            buffer.setLength(0);
        }
    }

    @Contract(" -> new")
    private @NotNull Token readNumber() {
        StringBuilder sb = new StringBuilder();
        boolean hasDecimal = false;

        while (Character.isDigit(currentChar) || (currentChar == '.' && !hasDecimal)) {
            if (currentChar == '.') hasDecimal = true;
            sb.append(currentChar);
            advance();
        }

        return new Token(Token.Type.NUMBER, sb.toString(), currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token readChar() {
        advance(); // 跳过起始的 '
        char value = handleEscapeSequences();
        if (currentChar != SINGLE_QUOTE) {
            throw new LexerException("Unclosed character literal", currentLine, currentColumn);
        }
        advance(); // 跳过结束的 '
        return new Token(Token.Type.CHAR, String.valueOf(value), currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token readString() {
        advance(); // 跳过起始的 "
        StringBuilder sb = new StringBuilder();

        while (currentChar != DOUBLE_QUOTE && currentChar != EOF) {
            sb.append(handleEscapeSequences());
        }

        if (currentChar != DOUBLE_QUOTE) {
            throw new LexerException("Unclosed string literal", currentLine, currentColumn);
        }
        advance(); // 跳过结束的 "

        return new Token(Token.Type.STRING, sb.toString(), currentLine, currentColumn);
    }

    private char handleEscapeSequences() {
        if (currentChar != ESCAPE) {
            char ch = currentChar;
            advance();
            return ch;
        }

        advance(); // 跳过转义符
        return switch (currentChar) {
            case 'n' -> NEWLINE;
            case 't' -> '\t';
            case 'r' -> '\r';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case '\'', '"', '\\' -> currentChar;
            default ->
                    throw new LexerException("Invalid escape sequence: \\" + currentChar, currentLine, currentColumn);
        };
    }

    private @NotNull Token readIdentifier() {
        StringBuilder sb = new StringBuilder();
        while (isIdentifierPart(currentChar)) {
            sb.append(currentChar);
            advance();
        }

        String identifier = sb.toString();
        if ("else".equals(identifier) && nextChar() == 'i') {
            return parseElseIf();
        }

        Token.Type type = KEYWORDS.getOrDefault(identifier, Token.Type.IDENTIFIER);
        return new Token(type, identifier, currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token parseElseIf() {
        skipWhitespaceAndComments();
        if (position + 3 < script.length()
                && script.startsWith("if", position + 1)
                && !Character.isLetterOrDigit(script.charAt(position + 3))) {
            advance(); // 跳过 i
            advance(); // 跳过 f
            return new Token(Token.Type.ELSE_IF, "else if", currentLine, currentColumn);
        }
        return new Token(Token.Type.ELSE, "else", currentLine, currentColumn);
    }

    private boolean isIdentifierStart(char c) {
        return Character.isAlphabetic(c) || c == '_' || c == '$' || c == '@';
    }

    private boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || Character.isDigit(c);
    }
}
