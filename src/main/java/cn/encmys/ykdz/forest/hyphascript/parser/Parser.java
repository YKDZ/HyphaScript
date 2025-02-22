package cn.encmys.ykdz.forest.hyphascript.parser;

import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.function.ScriptFunction;
import cn.encmys.ykdz.forest.hyphascript.node.*;
import cn.encmys.ykdz.forest.hyphascript.parser.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;

public class Parser {
    @NotNull
    private final List<Token> tokens;
    private int currentIndex = 0;

    public Parser(@NotNull List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<ASTNode> parse() {
        if (tokens.isEmpty()) return Collections.emptyList();

        List<ASTNode> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseStatement(false));
        }
        return statements;
    }

    private ASTNode parseStatement(boolean isInline) {
        if (match(Token.Type.IF, Token.Type.ELSE_IF)) return parseIfElseStatement();
        if (match(Token.Type.FOR)) return parseForStatement();
        if (match(Token.Type.DO)) return parseDoWhileStatement();
        if (match(Token.Type.WHILE)) return parseWhileStatement();
        if (match(Token.Type.RETURN)) return parseReturnStatement();
        if (match(Token.Type.BREAK)) return parseBreakStatement();
        if (match(Token.Type.CONTINUE)) return parseContinueStatement();
        if (match(Token.Type.LET)) {
            if (check(Token.Type.LEFT_BRAKETS) || check(Token.Type.LEFT_BRACE)) {
                return parseUnpackStatement();
            }
            return parseVariableDeclaration(isInline, false, false);
        }
        if (match(Token.Type.CONST)) {
            if (check(Token.Type.LEFT_BRAKETS) || check(Token.Type.LEFT_BRACE)) {
                return parseUnpackStatement();
            }
            return parseVariableDeclaration(isInline, false, true);
        }
        if (match(Token.Type.IMPORT)) return parseImport();
        if (match(Token.Type.EXPORT)) return parseExport();
        if (match(Token.Type.TRY)) return parseTryCatch();

        ASTNode expression = parseExpressionWithParentheses();
        consume(Token.Type.FINISH);
        return expression;
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseTryCatch() {
        ASTNode tryBlock = parseBlock();
        consume(Token.Type.CATCH);
        String caughtObjName = null;
        if (match(Token.Type.IDENTIFIER)) {
            caughtObjName = previous().value();
        }
        ASTNode catchBlock = parseBlock();
        ASTNode finallyBlock = null;
        if (match(Token.Type.FINALLY)) {
            finallyBlock = parseBlock();
        }
        return new TryCatch(tryBlock, caughtObjName, catchBlock, finallyBlock);
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseUnpackStatement() {
        Token startToken = previous();
        boolean isConst = previous().type() == Token.Type.CONST;

        List<String> to = new ArrayList<>();
        ASTNode from;

        if (match(Token.Type.LEFT_BRAKETS)) {
            // 数组解包
            while (!match(Token.Type.RIGHT_BRAKETS)) {
                if (check(Token.Type.IDENTIFIER)) {
                    to.add(consume(Token.Type.IDENTIFIER).value());
                } else {
                    throw new ParserException("Expected identifier in array unpack pattern", startToken);
                }
                if (!check(Token.Type.RIGHT_BRAKETS)) consume(Token.Type.COMMA);
            }
        } else if (match(Token.Type.LEFT_BRACE)) {
            // 对象解包
            while (!match(Token.Type.RIGHT_BRACE)) {
                if (check(Token.Type.IDENTIFIER)) {
                    to.add(consume(Token.Type.IDENTIFIER).value());
                } else {
                    throw new ParserException("Expected identifier in object unpack pattern", startToken);
                }
                if (!check(Token.Type.RIGHT_BRACE)) consume(Token.Type.COMMA);
            }
        } else {
            throw new ParserException("Expected '[' or '{' in unpack statement", startToken);
        }

        // 解析等号和右侧的值
        consume(Token.Type.EQUALS);
        from = parseExpressionWithParentheses();
        consume(Token.Type.FINISH);

        return new Unpack(isConst, from, to);
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseBreakStatement() {
        consume(Token.Type.FINISH);
        return new Break();
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseContinueStatement() {
        consume(Token.Type.FINISH);
        return new Continue();
    }

    private @NotNull ASTNode parseVariableDeclaration(boolean isInline, boolean isExported, boolean isConst) {
        Token identifier = consume(Token.Type.IDENTIFIER);
        ASTNode initValue = new Literal();
        if (match(Token.Type.EQUALS, Token.Type.COLON_EQUALS, Token.Type.PLUS_EQUALS, Token.Type.MINUS_EQUALS, Token.Type.DIV_EQUALS, Token.Type.MUL_EQUALS, Token.Type.MOD_EQUALS)) {
            initValue = parseExpressionWithParentheses();
        }
        if (check(Token.Type.FINISH) && !isInline) consume(Token.Type.FINISH);
        return isConst ? new Const(identifier.value(), initValue, isExported) : new Let(identifier.value(), initValue, isExported);
    }

    @NotNull
    private ASTNode parseExport() {
        Token startToken = previous();

        if (check(Token.Type.IDENTIFIER)) {
            Token namespace = consume(Token.Type.IDENTIFIER);
            consume(Token.Type.FINISH);
            return new Export(namespace.value());
        } else if (match(Token.Type.LET)) {
            return parseVariableDeclaration(false, true, false);
        } else if (match(Token.Type.CONST)) {
            return parseVariableDeclaration(false, true, true);
        }
        throw new ParserException("Wrong usage of export", startToken);
    }

    @NotNull
    private ASTNode parseImport() {
        Token startToken = previous();
        if (check(Token.Type.STRING)) return parseImportNamespace();
        else if (check(Token.Type.LEFT_BRACE)) return parseImportObjects();
        else throw new ParserException("Wrong usage of import", startToken);
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseImportNamespace() {
        Token namespace = consume(Token.Type.STRING);
        Token as = namespace;
        if (match(Token.Type.AS)) as = consume(Token.Type.IDENTIFIER);
        consume(Token.Type.FINISH);
        return new ImportNamespace(namespace.value(), as.value());
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseImportObjects() {
        Map<String, String> imported = new HashMap<>();
        consume(Token.Type.LEFT_BRACE);
        while (!match(Token.Type.RIGHT_BRACE)) {
            Token name = consume(Token.Type.IDENTIFIER);
            Token as = name;
            if (match(Token.Type.AS)) as = consume(Token.Type.IDENTIFIER);
            imported.put(name.value(), as.value());
            if (check(Token.Type.COMMA)) consume(Token.Type.COMMA);
        }
        consume(Token.Type.FROM);
        Token from = consume(Token.Type.STRING);
        consume(Token.Type.FINISH);
        return new ImportObjects(imported, from.value());
    }

    private MemberAccess parseIdentifierChain(boolean isNew) {
        MemberAccess base = new MemberAccess(new Literal(new Value(consume(Token.Type.IDENTIFIER).value())));

        while (true) {
            if (match(Token.Type.DOT)) {
                Token member = consume(Token.Type.IDENTIFIER);
                base = new MemberAccess(base, new Literal(new Value(member.value())));
            } else if (match(Token.Type.LEFT_BRAKETS)) {
                ASTNode from = new Literal();
                ASTNode to = new Literal();
                ASTNode step = new Literal();

                if (!check(Token.Type.COLON)) {
                    from = parseExpressionWithParentheses();
                }

                if (match(Token.Type.COLON)) {
                    // 发现冒号表示这是一个切片访问
                    // 则步长默认为 1
                    // 因为步长为 VOID 时代表索引访问
                    step = new Literal(new Value(1));
                    if (!check(Token.Type.COLON) && !check(Token.Type.RIGHT_BRAKETS)) {
                        to = parseExpressionWithParentheses();
                    }

                    if (match(Token.Type.COLON)) {
                        if (!check(Token.Type.RIGHT_BRAKETS)) {
                            step = parseExpressionWithParentheses();
                        }
                    }
                }
                consume(Token.Type.RIGHT_BRAKETS);
                base = new MemberAccess(
                        new ArrayAccess(base, from, to, step),
                        new Literal(new Value())
                );
            } else if (match(Token.Type.LEFT_PAREN)) {
                List<ASTNode> arguments = new ArrayList<>();
                if (!check(Token.Type.RIGHT_PAREN)) {
                    do {
                        arguments.add(parseExpressionWithParentheses());
                    } while (match(Token.Type.COMMA));
                }
                consume(Token.Type.RIGHT_PAREN);
                // 是否是调用构造方法
                if (!isNew) base = new MemberAccess(new FunctionCall(base.getTarget(), base, arguments), new Literal(new Value()));
                else base = new MemberAccess(new New(base, arguments), new Literal(new Value()));
            } else break;
        }

        return base;
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseWhileStatement() {
        consume(Token.Type.LEFT_PAREN);
        ASTNode condition = parseExpressionWithParentheses();
        consume(Token.Type.RIGHT_PAREN);
        ASTNode body = parseBlock();
        return new WhileLoop(condition, body);
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseDoWhileStatement() {
        ASTNode body = parseBlock();
        consume(Token.Type.WHILE);
        consume(Token.Type.LEFT_PAREN);
        ASTNode condition = parseExpressionWithParentheses();
        consume(Token.Type.RIGHT_PAREN);
        consume(Token.Type.FINISH);
        return new DoWhileLoop(body, condition);
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseForStatement() {
        consume(Token.Type.LEFT_PAREN);

        ASTNode initialization = new Literal();
        if (!check(Token.Type.FINISH)) {
            initialization = parseStatement(false);
        } else {
            consume(Token.Type.FINISH);
        }

        ASTNode condition = new Literal();
        if (!check(Token.Type.FINISH)) {
            condition = parseExpressionWithParentheses();
        }
        consume(Token.Type.FINISH);

        ASTNode afterThought = new Literal();
        if (!check(Token.Type.RIGHT_PAREN)) {
            afterThought = parseStatement(true);
        }
        consume(Token.Type.RIGHT_PAREN);

        ASTNode body = parseBlock();
        return new ForLoop(initialization, condition, afterThought, body);
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseIfElseStatement() {
        consume(Token.Type.LEFT_PAREN);
        ASTNode condition = parseExpressionWithParentheses();
        consume(Token.Type.RIGHT_PAREN);

        ASTNode thenBranch = parseBlock();

        ASTNode elseBranch = new Literal();

        if (match(Token.Type.ELSE)) {
            if (match(Token.Type.ELSE_IF)) {
                elseBranch = parseIfElseStatement();
            } else {
                elseBranch = parseBlock();
            }
        }

        return new Conditional(condition, thenBranch, elseBranch);
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseReturnStatement() {
        ASTNode value = null;
        if (!check(Token.Type.FINISH)) {
            value = parseExpressionWithParentheses();
        }
        consume(Token.Type.FINISH);
        return new Return(value);
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseFunctionDeclaration() {
        Token funcName = consume(Token.Type.IDENTIFIER);
        consume(Token.Type.LEFT_PAREN);
        List<String> receivers = new ArrayList<>();
        List<String> parameters = new ArrayList<>();
        if (!check(Token.Type.RIGHT_PAREN)) {
            do {
                Token identifier = consume(Token.Type.IDENTIFIER);
                if (identifier.value().startsWith("__")) receivers.add(identifier.value());
                else parameters.add(identifier.value());
            } while (match(Token.Type.COMMA));
        }
        consume(Token.Type.RIGHT_PAREN);
        ASTNode body;
        if (check(Token.Type.LEFT_BRACE)) {
            body = parseBlock();
        } else {
            body = new Return(parseExpressionWithParentheses());
        }
        // 处理立即调用的函数
        if (match(Token.Type.LEFT_PAREN)) {
            List<ASTNode> arguments = new ArrayList<>();
            if (!check(Token.Type.RIGHT_PAREN)) {
                do {
                    arguments.add(parseExpressionWithParentheses());
                } while (match(Token.Type.COMMA));
            }
            consume(Token.Type.RIGHT_PAREN);
            return new FunctionCall(new Literal(), new MemberAccess(new Literal(new Value(new ScriptFunction(receivers, parameters, body))), new Literal(new Value())), arguments);
        }
        return new ScriptFunctionDeclaration(funcName.value(), receivers, parameters, body);
    }

    private @NotNull ASTNode parseArrowFunction() {
        consume(Token.Type.LEFT_PAREN);
        List<String> receivers = new ArrayList<>();
        List<String> parameters = new ArrayList<>();
        if (!match(Token.Type.RIGHT_PAREN)) {
            do {
                Token identifier = consume(Token.Type.IDENTIFIER);
                if (identifier.value().startsWith("__")) receivers.add(identifier.value());
                else parameters.add(identifier.value());
            } while (match(Token.Type.COMMA));
            consume(Token.Type.RIGHT_PAREN);
        }
        consume(Token.Type.EQUALS);
        consume(Token.Type.GREATER);
        ASTNode body;
        if (check(Token.Type.LEFT_BRACE)) {
            body = parseBlock();
        } else {
            body = new Return(parseExpressionWithParentheses());
        }
        // 处理立即调用的函数
        if (match(Token.Type.LEFT_PAREN)) {
            List<ASTNode> arguments = new ArrayList<>();
            if (!check(Token.Type.RIGHT_PAREN)) {
                do {
                    arguments.add(parseExpressionWithParentheses());
                } while (match(Token.Type.COMMA));
            }
            consume(Token.Type.RIGHT_PAREN);
            return new FunctionCall(new Literal(), new MemberAccess(new Literal(new Value(new ScriptFunction(receivers, parameters, body))), new Literal(new Value())), arguments);
        }
        return new Literal(new Value(new ScriptFunction(receivers, parameters, body)));
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseBlock() {
        consume(Token.Type.LEFT_BRACE);
        List<ASTNode> statements = new ArrayList<>();
        while (!check(Token.Type.RIGHT_BRACE)) {
            statements.add(parseStatement(false));
        }
        consume(Token.Type.RIGHT_BRACE);
        return new Block(statements);
    }

    private ASTNode parseExpressionWithParentheses() {
        return parseExpressionWithParenthesesHelper(parseExpression());
    }

    private ASTNode parseExpressionWithParenthesesHelper(ASTNode expression) {
        if (match(Token.Type.LEFT_PAREN)) {
            ASTNode innerExpression = parseExpressionWithParentheses();
            consume(Token.Type.RIGHT_PAREN);
            return parseExpressionWithParenthesesHelper(innerExpression);
        }
        return expression;
    }

    private @NotNull ASTNode parseExpression() {
        return parseLogicalOr();
    }

    private @NotNull ASTNode parseLogicalOr() {
        ASTNode left = parseLogicalAnd();

        while (match(Token.Type.LOGIC_OR)) {
            Token operator = previous();
            ASTNode right = parseLogicalAnd();
            left = new BinaryOperation(operator.type(), left, right);
        }

        return left;
    }

    private @NotNull ASTNode parseLogicalAnd() {
        ASTNode left = parseBitwiseOr();

        while (match(Token.Type.LOGIC_AND)) {
            Token operator = previous();
            ASTNode right = parseBitwiseOr();
            left = new BinaryOperation(operator.type(), left, right);
        }

        return left;
    }

    private @NotNull ASTNode parseBitwiseOr() {
        ASTNode left = parseBitwiseAnd();

        while (match(Token.Type.BIT_OR)) {
            Token operator = previous();
            ASTNode right = parseBitwiseAnd();
            left = new BinaryOperation(operator.type(), left, right);
        }

        return left;
    }

    private @NotNull ASTNode parseBitwiseAnd() {
        ASTNode left = parseEquality();

        while (match(Token.Type.BIT_AND)) {
            Token operator = previous();
            ASTNode right = parseEquality();
            left = new BinaryOperation(operator.type(), left, right);
        }

        return left;
    }

    private @NotNull ASTNode parseEquality() {
        ASTNode left = parseComparison();

        while (match(Token.Type.EQUAL_EQUAL, Token.Type.BANG_EQUAL)) {
            Token operator = previous();
            ASTNode right = parseComparison();
            left = new BinaryOperation(operator.type(), left, right);
        }

        return left;
    }

    private @NotNull ASTNode parseComparison() {
        ASTNode left = parseTerm();

        while (match(Token.Type.LESS, Token.Type.LESS_EQUAL, Token.Type.GREATER, Token.Type.GREATER_EQUAL)) {
            Token operator = previous();
            ASTNode right = parseTerm();
            left = new BinaryOperation(operator.type(), left, right);
        }

        return left;
    }

    private @NotNull ASTNode parseTerm() {
        ASTNode left = parseFactor();

        while (match(Token.Type.PLUS, Token.Type.MINUS, Token.Type.POWER, Token.Type.MOD)) {
            Token operator = previous();
            ASTNode right = parseFactor();
            left = new BinaryOperation(operator.type(), left, right);
        }

        return left;
    }

    private @NotNull ASTNode parseFactor() {
        ASTNode left = parseUnary();

        while (match(Token.Type.MUL, Token.Type.DIV)) {
            Token operator = previous();
            ASTNode right = parseUnary();
            left = new BinaryOperation(operator.type(), left, right);
        }

        return left;
    }

    private @NotNull ASTNode parseUnary() {
        if (match(Token.Type.BANG, Token.Type.MINUS, Token.Type.TYPEOF)) {
            Token operator = previous();
            ASTNode right = parseUnary();
            return new UnaryOperation(operator.type(), right);
        }

        return parsePrimary();
    }

    private @NotNull ASTNode parsePrimary() {
        if (match(Token.Type.FUNCTION)) return parseFunctionDeclaration();
        if (match(Token.Type.LEFT_BRAKETS)) {
            return parseArray();
        }
        if (match(Token.Type.NUMBER)) {
            return new Literal(new Value(new BigDecimal(previous().value())));
        }
        if (match(Token.Type.STRING)) {
            return new Literal(new Value(previous().value()));
        }
        if (check(Token.Type.BACKTICK)) {
            return parseTemplateString();
        }
        if (match(Token.Type.CHAR)) {
            return new Literal(new Value(previous().value().charAt(0)));
        }
        if (match(Token.Type.BOOLEAN)) {
            return new Literal(new Value(Boolean.valueOf(previous().value())));
        }
        if (match(Token.Type.NULL)) {
            return new Literal(new Value(null));
        }
        if (match(Token.Type.NEW)) {
            return parseIdentifierChain(true);
        }
        if (check(Token.Type.IDENTIFIER)) {
            MemberAccess member = parseIdentifierChain(false);
            if (match(Token.Type.EQUALS, Token.Type.COLON_EQUALS, Token.Type.PLUS_EQUALS, Token.Type.MINUS_EQUALS, Token.Type.DIV_EQUALS, Token.Type.MUL_EQUALS, Token.Type.MOD_EQUALS)) {
                return new Assignment(previous().type(), member, parseExpressionWithParentheses());
            }
            return member;
        }
        if (check(Token.Type.LEFT_BRACE)) {
            return parseNestedObject();
        }
        if (check(Token.Type.LEFT_PAREN)) {
            if (isArrowFunction()) {
                return parseArrowFunction();
            }

            consume(Token.Type.LEFT_PAREN);
            ASTNode expression = parseExpressionWithParentheses();
            consume(Token.Type.RIGHT_PAREN);

            // 右括号后紧跟左括号
            if (match(Token.Type.LEFT_PAREN)) {
                List<ASTNode> arguments = new ArrayList<>();
                if (!check(Token.Type.RIGHT_PAREN)) {
                    do {
                        arguments.add(parseExpressionWithParentheses());
                    } while (match(Token.Type.COMMA));
                }
                consume(Token.Type.RIGHT_PAREN);
                return new MemberAccess(new FunctionCall(new Literal(), new MemberAccess(expression, new Literal(new Value())), arguments), new Literal(new Value()));
            }

            return expression;
        }

        throw new ParserException("Unexpected token in expression.", tokens.get(currentIndex - 1));
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseTemplateString() {
        List<ASTNode> parts = new ArrayList<>();
        consume(Token.Type.BACKTICK);
        while (!match(Token.Type.BACKTICK)) {
            if (match(Token.Type.STRING)) {
                parts.add(new Literal(new Value(previous().value())));
            } else {
                parts.add(parseExpression());
            }
        }
        return new TemplateString(parts);
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseArray() {
        List<ASTNode> nodes = new ArrayList<>();
        while (!match(Token.Type.RIGHT_BRAKETS)) {
            nodes.add(parseExpressionWithParentheses());
            if (check(Token.Type.COMMA) && !check(Token.Type.RIGHT_BRAKETS)) consume(Token.Type.COMMA);
        }
        return new MemberAccess(new Array(nodes), new Literal(new Value()));
    }

    @Contract(" -> new")
    private @NotNull ASTNode parseNestedObject() {
        Token startToken = consume(Token.Type.LEFT_BRACE);

        Map<String, ASTNode> objects = new HashMap<>();
        while (!match(Token.Type.RIGHT_BRACE)) {
            if (check(Token.Type.IDENTIFIER)) {
                Token identifier = consume(Token.Type.IDENTIFIER);
                consume(Token.Type.COLON);
                objects.put(identifier.value(), parseExpressionWithParentheses());
            } else if (check(Token.Type.STRING)) {
                Token string = consume(Token.Type.STRING);
                consume(Token.Type.COLON);
                objects.put(string.value(), parseExpressionWithParentheses());
            } else {
                throw new ParserException("Key of nested object must be identifier or string", startToken);
            }
            if (!check(Token.Type.RIGHT_BRACE)) consume(Token.Type.COMMA);
        }
        return new NestedObject(objects);
    }

    private boolean isArrowFunction() {
        // 跳过自己的左括号
        int tempIndex = currentIndex + 1;
        while (tempIndex < tokens.size() && tokens.get(tempIndex).type() != Token.Type.LEFT_PAREN) {
            tempIndex++;
            if (tokens.get(tempIndex).type() == Token.Type.EQUALS &&
                    tokens.get(tempIndex + 1).type() == Token.Type.GREATER) {
                        return true;
            }
        }
        return false;
    }

    private boolean match(@NotNull Token.Type... types) {
        if (tokens.size() > currentIndex && Arrays.asList(types).contains(tokens.get(currentIndex).type())) {
            currentIndex++;
            return true;
        }
        return false;
    }

    private boolean check(@NotNull Token.Type... types) {
        return currentIndex < tokens.size() && Arrays.asList(types).contains(tokens.get(currentIndex).type());
    }

    private boolean checkNext(@NotNull Token.Type... types) {
        return currentIndex + 1 < tokens.size() && Arrays.asList(types).contains(tokens.get(currentIndex + 1).type());
    }

    private Token previous() {
        return tokens.get(currentIndex - 1);
    }

    private Token consume(@NotNull Token.Type... expects) {
        if (match(expects)) {
            return previous();
        }
        throw new ParserException("Unexpected token, expected " + Arrays.toString(expects) + " but given: " + tokens.get(currentIndex).value() + ".", tokens.get(currentIndex - 1));
    }

    private boolean isAtEnd() {
        return check(Token.Type.EOF);
    }
}