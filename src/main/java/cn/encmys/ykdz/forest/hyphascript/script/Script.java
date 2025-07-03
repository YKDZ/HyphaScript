package cn.encmys.ykdz.forest.hyphascript.script;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.exception.LexerException;
import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.lexer.Lexer;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.parser.LexicalScope;
import cn.encmys.ykdz.forest.hyphascript.parser.Parser;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a script that can be parsed into an abstract syntax tree (AST)
 * and evaluated within a given context. Maintains state to avoid redundant
 * parsing and evaluation operations.
 */
public class Script {
    private final @NotNull String script;
    private @NotNull List<ASTNode> nodes = new ArrayList<>();
    private @Nullable ParserResult parserResult;
    private @Nullable LexicalScope lexicalScope;
    private @Nullable Context context;
    private boolean isParsed = false;
    private boolean isEvaluated = false;

    /**
     * Constructs a Script instance with the specified script content and execution context.
     *
     * @param script the source code of the script
     */
    public Script(@NotNull String script) {
        this.script = script;
    }

    /**
     * Parses the script into an AST. Will only execute parsing once per script version,
     * subsequent calls return cached result.
     *
     * @return parsing result with detailed status and metrics
     */
    public @NotNull ParserResult parse() {
        long startTime = System.currentTimeMillis();

        if (isParsed) {
            return parserResult != null ? parserResult : new ParserResult(
                    script,
                    ParserResult.Type.PARSED,
                    0,
                    "Parser result not initialized",
                    0,
                    0
            );
        }

        try {
            List<Token> tokens = new Lexer(script).tokenize();
            Parser parser = new Parser(tokens);
            nodes = parser.parse();
            lexicalScope = parser.getParseContext().getLexicalScope();
            handleParseSuccess(startTime);
        } catch (LexerException e) {
            handleLexerError(e, startTime);
        } catch (ParserException e) {
            handleParserError(e, startTime);
        }

        assert parserResult != null;
        return parserResult;
    }

    /**
     * Evaluates the script using a custom execution context.
     *
     * @param context context for variable resolution and function execution
     * @return evaluation result with value and execution metrics
     */
    public @NotNull EvaluateResult evaluate(@NotNull Context context) {
        this.context = context;

        long startTime = System.currentTimeMillis();

        if (!isParsed) {
            ParserResult parseResult = parse();
            if (parseResult.resultType() != ParserResult.Type.SUCCESS) {
                return createEvaluationErrorFromParseResult(parseResult, startTime);
            }
        }

        try {
            Value result = evaluateNodes(context);
            isEvaluated = true;
            return createSuccessEvaluationResult(result, startTime);
        } catch (EvaluateException e) {
            return createEvaluationErrorResult(e, startTime);
        }
    }

    private void handleParseSuccess(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        parserResult = new ParserResult(
                script,
                ParserResult.Type.SUCCESS,
                duration,
                "Successfully parsed",
                0,
                0
        );
        isParsed = true;
    }

    private void handleLexerError(@NotNull LexerException e, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        parserResult = new ParserResult(
                script,
                ParserResult.Type.LEXER_ERROR,
                duration,
                e.getMessage(),
                e.getLine(),
                e.getColumn()
        );
    }

    private void handleParserError(@NotNull ParserException e, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        parserResult = new ParserResult(
                script,
                ParserResult.Type.PARSER_ERROR,
                duration,
                e.getMessage(),
                e.getToken().line(),
                e.getToken().column()
        );
    }

    private @NotNull Value evaluateNodes(@NotNull Context context) throws EvaluateException {
        Value result = new Value();
        for (ASTNode node : nodes) {
            result = node.evaluate(context).getReferredValue();
        }
        return result;
    }

    private @NotNull EvaluateResult createSuccessEvaluationResult(Value result, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        return new EvaluateResult(
                EvaluateResult.Type.SUCCESS,
                script,
                result,
                duration,
                "Evaluation successful",
                null,
                0, 0, 0, 0
        );
    }

    private @NotNull EvaluateResult createEvaluationErrorResult(@NotNull EvaluateException e, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        return new EvaluateResult(
                EvaluateResult.Type.EVALUATE_ERROR,
                script,
                new Value(),
                duration,
                e.getMessage(),
                e.getCause(),
                e.getNode().getStartToken().line(),
                e.getNode().getStartToken().column(),
                e.getNode().getEndToken().line(),
                e.getNode().getEndToken().column()
        );
    }

    private @NotNull EvaluateResult createEvaluationErrorFromParseResult(@NotNull ParserResult parseResult, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        return new EvaluateResult(
                EvaluateResult.Type.PARSER_ERROR,
                script,
                new Value(),
                duration,
                parseResult.errorMsg(),
                null,
                parseResult.errorLine(),
                parseResult.errorColumn(),
                parseResult.errorLine(),
                parseResult.errorColumn()
        );
    }

    @NotNull
    public String getScript() {
        return script;
    }

    public boolean isEvaluated() {
        return isEvaluated;
    }

    public boolean isParsed() {
        return isParsed;
    }

    @Nullable
    public ParserResult getParserResult() {
        return parserResult;
    }

    public @Nullable LexicalScope getLexicalScope() {
        return lexicalScope;
    }

    private void resetState() {
        nodes.clear();
        parserResult = null;
        isParsed = false;
        isEvaluated = false;
    }

    @Override
    public String toString() {
        return "Script{" +
                "script='" + script + '\'' +
                ", parsed=" + isParsed +
                ", evaluated=" + isEvaluated +
                '}';
    }

    public @NotNull Context getContext() {
        if (!isEvaluated) throw new IllegalStateException("Evaluated script is not yet evaluated.");
        assert context != null;
        return context;
    }
}