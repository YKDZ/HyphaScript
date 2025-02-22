package cn.encmys.ykdz.forest.hyphascript.parser;

import cn.encmys.ykdz.forest.hyphascript.exception.LexerException;
import cn.encmys.ykdz.forest.hyphascript.parser.token.Token;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private static final char EOF = '\0';
    private static final char NEWLINE = '\n';
    private static final char BACKTICK = '`';
    private static final char DOUBLE_QUOTE = '"';
    private static final char SINGLE_QUOTE = '\'';
    private static final char ESCAPE = '\\';

    private final String script;
    private final List<Token> tokens = new ArrayList<>();
    private int position = 0;
    private char currentChar;
    private int currentLine = 1;
    private int currentColumn = 0;

    public Lexer(@NotNull String script) {
        this.script = script;
        this.currentChar = !script.isBlank() ? script.charAt(position) : EOF;
    }

    public List<Token> tokenize() {
        tokens.clear();

        if (script.isBlank()) {
            tokens.add(new Token(Token.Type.EOF, String.valueOf(EOF), currentLine, currentColumn));
            return tokens;
        }

        while (position < script.length()) {
            tokens.add(nextToken());
        }

        if (!tokens.isEmpty() && tokens.getLast().type() != Token.Type.FINISH && currentLine == 1) {
            tokens.add(new Token(Token.Type.FINISH, ";", currentLine, currentColumn));
        }

        tokens.add(new Token(Token.Type.EOF, String.valueOf(EOF), currentLine, currentColumn));
        return tokens;
    }

    private void advance() {
        position++;
        if (currentChar == NEWLINE) {
            currentLine++;
            currentColumn = 0;
        } else {
            currentColumn++;
        }
        currentChar = position < script.length() ? script.charAt(position) : EOF;
    }

    private void skipWhitespaceAndComments() {
        while (currentChar != EOF) {
            if (Character.isWhitespace(currentChar)) {
                advance();
            } else if (currentChar == '/' && nextChar() == '/') {
                skipSingleLineComment();
            } else if (currentChar == '/' && nextChar() == '*') {
                skipMultiLineComment();
            } else {
                break;
            }
        }
    }

    private void skipSingleLineComment() {
        while (currentChar != NEWLINE && currentChar != EOF) {
            advance();
        }
    }

    private void skipMultiLineComment() {
        advance(); // Skip '/'
        advance(); // Skip '*'
        while (currentChar != EOF) {
            if (currentChar == '*' && nextChar() == '/') {
                advance(); // Skip '*'
                advance(); // Skip '/'
                break;
            }
            advance();
        }
    }

    private char nextChar() {
        return position + 1 < script.length() ? script.charAt(position + 1) : EOF;
    }

    public Token nextToken() {
        skipWhitespaceAndComments();

        if (Character.isDigit(currentChar)) {
            return readNumber();
        }

        if (Character.isAlphabetic(currentChar) || currentChar == '_' || currentChar == '$') {
            return readIdentifierOrKeyword();
        }

        return switch (currentChar) {
            case '(' -> parseSingleCharToken(Token.Type.LEFT_PAREN, "(");
            case ')' -> parseSingleCharToken(Token.Type.RIGHT_PAREN, ")");
            case '{' -> parseSingleCharToken(Token.Type.LEFT_BRACE, "{");
            case '}' -> parseSingleCharToken(Token.Type.RIGHT_BRACE, "}");
            case '[' -> parseSingleCharToken(Token.Type.LEFT_BRAKETS, "[");
            case ']' -> parseSingleCharToken(Token.Type.RIGHT_BRAKETS, "]");
            case ',' -> parseSingleCharToken(Token.Type.COMMA, ",");
            case ';' -> parseSingleCharToken(Token.Type.FINISH, ";");
            case '=' -> parseAssignmentOrEquals();
            case ':' -> parseAssignmentOrColon();
            case '+' -> parsePlusOrAssignment();
            case '-' -> parseMinusOrAssignment();
            case '*' -> parseMulOrAssignment();
            case '/' -> parseDivOrAssignment();
            case '%' -> parseModOrAssignment();
            case '^' -> parseSingleCharToken(Token.Type.POWER, "^");
            case '!', '<', '>' -> parseComparisonOrBang();
            case '|', '&' -> parseLogicalOrArrowFunction();
            case '.' -> parseSingleCharToken(Token.Type.DOT, ".");
            case EOF -> new Token(Token.Type.EOF, String.valueOf(EOF), currentLine, currentColumn);
            case DOUBLE_QUOTE -> readString();
            case BACKTICK -> readTemplateString();
            case SINGLE_QUOTE -> readChar();
            default -> throw new LexerException("Unexpected character: " + currentChar, script,  currentLine, currentColumn);
        };
    }

    @Contract("_, _ -> new")
    private @NotNull Token parseSingleCharToken(Token.Type type, String value) {
        advance();
        return new Token(type, value, currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token parseAssignmentOrEquals() {
        advance();
        if (currentChar == '=') {
            advance();
            return new Token(Token.Type.EQUAL_EQUAL, "==", currentLine, currentColumn);
        }
        return new Token(Token.Type.EQUALS, "=", currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token parsePlusOrAssignment() {
        advance();
        if (currentChar == '=') {
            advance();
            return new Token(Token.Type.PLUS_EQUALS, "+=", currentLine, currentColumn);
        }
        return new Token(Token.Type.PLUS, "+", currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token parseMinusOrAssignment() {
        advance();
        if (currentChar == '=') {
            advance();
            return new Token(Token.Type.MINUS_EQUALS, "-=", currentLine, currentColumn);
        }
        return new Token(Token.Type.MINUS, "-", currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token parseAssignmentOrColon() {
        advance();
        if (currentChar == '=') {
            advance();
            return new Token(Token.Type.COLON_EQUALS, ":=", currentLine, currentColumn);
        }
        return new Token(Token.Type.COLON, ":", currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token parseMulOrAssignment() {
        advance();
        if (currentChar == '=') {
            advance();
            return new Token(Token.Type.MUL_EQUALS, "*=", currentLine, currentColumn);
        }
        return new Token(Token.Type.MUL, "*", currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token parseDivOrAssignment() {
        advance();
        if (currentChar == '=') {
            advance();
            return new Token(Token.Type.DIV_EQUALS, "/=", currentLine, currentColumn);
        }
        return new Token(Token.Type.DIV, "/", currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token parseModOrAssignment() {
        advance();
        if (currentChar == '=') {
            advance();
            return new Token(Token.Type.MOD_EQUALS, "%=", currentLine, currentColumn);
        }
        return new Token(Token.Type.MOD, "%", currentLine, currentColumn);
    }

    private @NotNull Token parseComparisonOrBang() {
        StringBuilder sb = new StringBuilder();
        while (currentChar == '>' || currentChar == '<' || currentChar == '=' || currentChar == '!') {
            sb.append(currentChar);
            advance();
        }
        return switch (sb.toString()) {
            case ">" -> new Token(Token.Type.GREATER, ">", currentLine, currentColumn);
            case "<" -> new Token(Token.Type.LESS, "<", currentLine, currentColumn);
            case "<=" -> new Token(Token.Type.LESS_EQUAL, "<=", currentLine, currentColumn);
            case ">=" -> new Token(Token.Type.GREATER_EQUAL, ">=", currentLine, currentColumn);
            case "!=" -> new Token(Token.Type.BANG_EQUAL, "!=", currentLine, currentColumn);
            default -> throw new LexerException("Unexpected character: " + currentChar, script, currentLine, currentColumn);
        };
    }

    private @NotNull Token parseLogicalOrArrowFunction() {
        StringBuilder sb = new StringBuilder();
        while (currentChar == '|' || currentChar == '&') {
            sb.append(currentChar);
            advance();
        }
        return switch (sb.toString()) {
            case "&&" -> new Token(Token.Type.LOGIC_AND, "&&", currentLine, currentColumn);
            case "||" -> new Token(Token.Type.LOGIC_OR, "||", currentLine, currentColumn);
            case "|" -> new Token(Token.Type.BIT_OR, "|", currentLine, currentColumn);
            case "&" -> new Token(Token.Type.BIT_AND, "&", currentLine, currentColumn);
            default -> throw new LexerException("Unexpected character: " + currentChar, script, currentLine, currentColumn);
        };
    }

    @Contract(" -> new")
    private @NotNull Token readTemplateString() {
        StringBuilder currentText = new StringBuilder();
        advance(); // Skip initial backtick
        tokens.add(new Token(Token.Type.BACKTICK, String.valueOf(BACKTICK), currentLine, currentColumn));

        while (currentChar != BACKTICK && currentChar != EOF) {
            if (currentChar == '$' && nextChar() == '{') {
                if (!currentText.isEmpty()) {
                    tokens.add(new Token(Token.Type.STRING, currentText.toString(), currentLine, currentColumn));
                    currentText.setLength(0);
                }
                advance(); // Skip '$'
                advance(); // Skip '{'
                while (currentChar != '}' && currentChar != EOF) {
                    tokens.add(nextToken());
                }
                if (currentChar == '}') {
                    advance(); // Skip '}'
                }
            } else {
                currentText.append(currentChar);
                advance();
            }
        }

        if (!currentText.isEmpty()) {
            tokens.add(new Token(Token.Type.STRING, currentText.toString(), currentLine, currentColumn));
        }

        advance();
        return new Token(Token.Type.BACKTICK, String.valueOf(BACKTICK), currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token readNumber() {
        StringBuilder number = new StringBuilder();
        while (Character.isDigit(currentChar) || currentChar == '.' || currentChar == '_') {
            if (currentChar != '_') number.append(currentChar);
            advance();
        }
        return new Token(Token.Type.NUMBER, number.toString(), currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token readChar() {
        // 前单引号
        advance();
        char result;
        if (currentChar == ESCAPE) result = parseEscapeSequence();
        else {
            result = currentChar;
            // 字符本身
            advance();
        }
        // 后单引号
        advance();
        return new Token(Token.Type.CHAR, String.valueOf(result), currentLine, currentColumn);
    }

    @Contract(" -> new")
    private @NotNull Token readString() {
        advance();
        StringBuilder str = new StringBuilder();
        while (currentChar != DOUBLE_QUOTE && currentChar != EOF) {
            if (currentChar == ESCAPE) {
                str.append(parseEscapeSequence());
            } else {
                str.append(currentChar);
            }
            advance();
        }
        advance();
        return new Token(Token.Type.STRING, str.toString(), currentLine, currentColumn);
    }

    private char parseEscapeSequence() {
        advance();
        return switch (currentChar) {
            case DOUBLE_QUOTE -> DOUBLE_QUOTE;
            case ESCAPE -> ESCAPE;
            case SINGLE_QUOTE -> SINGLE_QUOTE;
            case 'n' -> NEWLINE;
            case 't' -> '\t';
            case 'r' -> '\r';
            case 'b' -> '\b';
            case 'f' -> '\f';
            default -> throw new LexerException("Unknown escape sequence: \\" + currentChar, script, currentLine, currentColumn);
        };
    }

    private @NotNull Token readIdentifierOrKeyword() {
        StringBuilder identifier = new StringBuilder();
        while (Character.isAlphabetic(currentChar) || Character.isDigit(currentChar) || currentChar == '_' || currentChar == '$') {
            identifier.append(currentChar);
            advance();
        }
        String result = identifier.toString();
        return switch (result) {
            case "let" -> new Token(Token.Type.LET, result, currentLine, currentColumn);
            case "function" -> new Token(Token.Type.FUNCTION, result, currentLine, currentColumn);
            case "if" -> new Token(Token.Type.IF, result, currentLine, currentColumn);
            case "else" -> parseElseOrElseIf();
            case "true", "false" -> new Token(Token.Type.BOOLEAN, result, currentLine, currentColumn);
            case "null" -> new Token(Token.Type.NULL, result, currentLine, currentColumn);
            case "return" -> new Token(Token.Type.RETURN, result, currentLine, currentColumn);
            case "while" -> new Token(Token.Type.WHILE, result, currentLine, currentColumn);
            case "do" -> new Token(Token.Type.DO, result, currentLine, currentColumn);
            case "for" -> new Token(Token.Type.FOR, result, currentLine, currentColumn);
            case "break" -> new Token(Token.Type.BREAK, result, currentLine, currentColumn);
            case "continue" -> new Token(Token.Type.CONTINUE, result, currentLine, currentColumn);
            case "typeof" -> new Token(Token.Type.TYPEOF, result, currentLine, currentColumn);
            case "const" -> new Token(Token.Type.CONST, result, currentLine, currentColumn);
            case "export" -> new Token(Token.Type.EXPORT, result, currentLine, currentColumn);
            case "import" -> new Token(Token.Type.IMPORT, result, currentLine, currentColumn);
            case "as" -> new Token(Token.Type.AS, result, currentLine, currentColumn);
            case "from" -> new Token(Token.Type.FROM, result, currentLine, currentColumn);
            case "new" -> new Token(Token.Type.NEW, result, currentLine, currentColumn);
            case "try" -> new Token(Token.Type.TRY, result, currentLine, currentColumn);
            case "catch" -> new Token(Token.Type.CATCH, result, currentLine, currentColumn);
            case "finally" -> new Token(Token.Type.FINALLY, result, currentLine, currentColumn);
            default -> new Token(Token.Type.IDENTIFIER, result, currentLine, currentColumn);
        };
    }

    @Contract(" -> new")
    private @NotNull Token parseElseOrElseIf() {
        skipWhitespaceAndComments();
        if (currentChar == 'i') {
            advance();
            advance();
            return new Token(Token.Type.ELSE_IF, "else if", currentLine, currentColumn);
        }
        return new Token(Token.Type.ELSE, "else", currentLine, currentColumn);
    }
}