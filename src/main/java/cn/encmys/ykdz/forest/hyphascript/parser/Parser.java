package cn.encmys.ykdz.forest.hyphascript.parser;

import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.infix.*;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix.*;
import cn.encmys.ykdz.forest.hyphascript.parser.statement.*;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Parser {
    private final @NotNull ParseContext ctx;
    private final @NotNull Map<Token.Type, StatementParser> statementParsers = new HashMap<>();
    private final @NotNull Map<Token.Type, ExpressionParser.Prefix> prefixParsers = new HashMap<>();
    private final @NotNull Map<Token.Type, ExpressionParser.Infix> infixParsers = new HashMap<>();
    private final @NotNull PrecedenceTable precedenceTable = new PrecedenceTable();

    public Parser(@NotNull List<Token> tokens) {
        this.ctx = new ParseContext(this, tokens);
        registerCoreParsers();
    }

    public @NotNull List<ASTNode> parse() {
        if (ctx.getTokens().isEmpty()) return Collections.emptyList();

        List<ASTNode> statements = new ArrayList<>();
        while (!ctx.check(Token.Type.EOF)) {
            statements.add(parseStatement());
        }

        return statements;
    }

    public @NotNull ASTNode parseStatement() {
        Token.Type currentType = ctx.current().type();

        StatementParser parser = statementParsers.get(currentType);

        if (parser != null && parser.canParse(ctx)) {
            return parser.parse(ctx);
        }

        return parseExpressionStatement();
    }

    private @NotNull ASTNode parseExpressionStatement() {
        ASTNode expr = parseExpression(PrecedenceTable.Precedence.LOWEST);
        ctx.consumeStatementEnd();
        return expr;
    }

    public @NotNull ASTNode parseExpression(@NotNull PrecedenceTable.Precedence precedence) {
        Token startToken = ctx.current();
        ExpressionParser.Prefix prefix = prefixParsers.get(startToken.type());
        if (prefix == null) throw new ParserException("Unexpected token: " + startToken, startToken);

        ASTNode left = prefix.parse(ctx);

        while (precedence.lessThan(nextPrecedence())) {
            Token.Type nextType = ctx.current().type();
            ExpressionParser.Infix infix = infixParsers.get(nextType);
            if (infix == null) break;
            left = infix.parse(ctx, left);
        }

        return left;
    }

    public @NotNull ASTNode parseBlock(@NotNull ParseContext ctx) {
        StatementParser parser = statementParsers.get(Token.Type.LEFT_BRACE);
        // 就算不要求一定要用 {} 包裹
        // 也执行一次以确保整体性
        parser.canParse(ctx);
        return parser.parse(ctx);
    }

    private @NotNull PrecedenceTable.Precedence nextPrecedence() {
        return precedenceTable.get(ctx.current().type());
    }

    private void registerCoreParsers() {
        registerStatement(Token.Type.IF, new IfParser());
        registerStatement(Token.Type.LEFT_BRACE, new BlockParser());
        registerStatement(Token.Type.FOR, new ForParser());
        registerStatement(Token.Type.WHILE, new WhileParser());
        registerStatement(Token.Type.DO, new DoWhileStatement());
        registerStatement(Token.Type.RETURN, new ReturnParser());
        registerStatement(Token.Type.FUNCTION, new FunctionDeclarationParser());
        registerStatement(Token.Type.BREAK, new BreakParser());
        registerStatement(Token.Type.CONTINUE, new ContinueParser());
        registerStatement(Token.Type.IMPORT, new ImportParser());
        registerStatement(Token.Type.EXPORT, new ExportParser());
        registerStatement(Token.Type.TRY, new TryCatchParser());
        registerStatement(Token.Type.CONST, new VariableDeclarationParser());
        registerStatement(Token.Type.LET, new VariableDeclarationParser());
        registerStatement(Token.Type.CLASS, new ClassDeclarationParser());

        registerPrefix(Token.Type.SLEEP, new SleepParser());
        registerPrefix(Token.Type.BACKTICK, new TemplateStringParser());
        registerPrefix(Token.Type.QUESTION, new TemplateStringParser());
        registerPrefix(Token.Type.MINUS, new UnaryParser());
        registerPrefix(Token.Type.BANG, new UnaryParser());
        registerPrefix(Token.Type.TYPEOF, new UnaryParser());
        registerPrefix(Token.Type.NOT, new UnaryParser());
        registerPrefix(Token.Type.BOOLEAN, new BooleanParser());
        registerPrefix(Token.Type.CHAR, new CharParser());
        registerPrefix(Token.Type.IDENTIFIER, new IdentifierParser());
        registerPrefix(Token.Type.LEFT_BRACE, new LiteralScriptObjectParser());
        registerPrefix(Token.Type.NEW, new NewParser());
        registerPrefix(Token.Type.STRING, new StringParser());
        registerPrefix(Token.Type.NUMBER, new NumberParser());
        registerPrefix(Token.Type.NULL, new NullParser());
        registerPrefix(Token.Type.LEFT_BRACKET, new ArrayParser());
        registerPrefix(Token.Type.LEFT_PAREN, new GroupedExpressionOrArrowFunctionParser());

        registerInfix(Token.Type.QUESTION, new ConditionalOperatorParser());
        registerInfix(Token.Type.LEFT_BRACKET, new ArrayAccessParser());
        registerInfix(Token.Type.BIT_OR, new BitwiseOrParser());
        registerInfix(Token.Type.BIT_AND, new BitwiseAndParser());
        registerInfix(Token.Type.XOR, new BitwiseXorParser());
        registerInfix(Token.Type.SHIFT_LEFT, new BitwiseShiftLeftParser());
        registerInfix(Token.Type.SHIFT_RIGHT, new BitwiseShiftRightParser());
        registerInfix(Token.Type.LESS, new ComparisonParser());
        registerInfix(Token.Type.GREATER, new ComparisonParser());
        registerInfix(Token.Type.GREATER_EQUAL, new ComparisonParser());
        registerInfix(Token.Type.LESS_EQUAL, new ComparisonParser());
        registerInfix(Token.Type.INSTANCE_OF, new ComparisonParser());
        registerInfix(Token.Type.BANG_EQUALS, new EqualityParser());
        registerInfix(Token.Type.EQUAL_EQUAL, new EqualityParser());
        registerInfix(Token.Type.MUL, new MulDivModParser());
        registerInfix(Token.Type.DIV, new MulDivModParser());
        registerInfix(Token.Type.MOD, new MulDivModParser());
        registerInfix(Token.Type.PLUS, new PlusMinusParser());
        registerInfix(Token.Type.MINUS, new PlusMinusParser());
        registerInfix(Token.Type.POWER, new PowerParser());
        registerInfix(Token.Type.LEFT_PAREN, new FunctionCallParser());
        registerInfix(Token.Type.LEFT_BRACE, new MapFunctionCallParser());
        registerInfix(Token.Type.DOT, new MemberAccessParser());
        registerInfix(Token.Type.EQUALS, new AssignmentParser());
        registerInfix(Token.Type.COLON_EQUALS, new AssignmentParser());
        registerInfix(Token.Type.PLUS_EQUALS, new AssignmentParser());
        registerInfix(Token.Type.MINUS_EQUALS, new AssignmentParser());
        registerInfix(Token.Type.DIV_EQUALS, new AssignmentParser());
        registerInfix(Token.Type.MOD_EQUALS, new AssignmentParser());
        registerInfix(Token.Type.MUL_EQUALS, new AssignmentParser());
        registerInfix(Token.Type.POWER_EQUALS, new AssignmentParser());
        registerInfix(Token.Type.LOGIC_AND, new LogicalAndParser());
        registerInfix(Token.Type.LOGIC_OR, new LogicalOrParser());
    }

    private void registerStatement(@NotNull Token.Type type, @NotNull StatementParser parser) {
        statementParsers.put(type, parser);
    }

    private void registerPrefix(@NotNull Token.Type type, @NotNull ExpressionParser.Prefix parser) {
        prefixParsers.put(type, parser);
    }

    private void registerInfix(@NotNull Token.Type type, @NotNull ExpressionParser.Infix parser) {
        infixParsers.put(type, parser);
        precedenceTable.put(type, parser.precedence());
    }

    public @NotNull ParseContext getParseContext() {
        return ctx;
    }
}
