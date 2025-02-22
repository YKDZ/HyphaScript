package cn.encmys.ykdz.forest.hyphascript.script;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.exception.LexerException;
import cn.encmys.ykdz.forest.hyphascript.exception.ParserException;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Block;
import cn.encmys.ykdz.forest.hyphascript.parser.Lexer;
import cn.encmys.ykdz.forest.hyphascript.parser.Parser;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Script {
    @NotNull
    private String script;
    @NotNull
    private List<ASTNode> nodes = new ArrayList<>();
    @NotNull
    private final Context context;
    @Nullable
    private ParserResult parserResult;
    private boolean isParsed = false;
    private boolean isEvaluated = false;

    public Script(@NotNull String script, @NotNull Context context) {
        this.script = script;
        this.context = context;
    }

    public ParserResult parse() {
        if (isParsed) return new ParserResult(script, ParserResult.Type.PARSED, "", 0, 0);

        try {
            Lexer lexer = new Lexer(script);
            Parser parser = new Parser(lexer.tokenize());
            nodes = parser.parse();

            parserResult = new ParserResult(script, ParserResult.Type.SUCCESS, "", 0, 0);
        } catch (LexerException e) {
            parserResult = new ParserResult(script, ParserResult.Type.LEXER_ERROR, e.getMessage(), e.getLine(), e.getColumn());
        } catch (ParserException e) {
            parserResult = new ParserResult(script, ParserResult.Type.PARSER_ERROR, e.getMessage(), e.getToken().line(), e.getToken().column());
        }

        return parserResult;
    }

    /**
     * Evaluate the script with default context provided in the constructor
     * @return Evaluated result
     */
    @NotNull
    public EvaluateResult evaluate() {
        return evaluate(context);
    }

    /**
     * Evaluate the script with given context
     * @return Evaluated result
     */
    @NotNull
    public EvaluateResult evaluate(@NotNull Context ctx) {
        if (!isParsed) {
            parse();
            assert parserResult != null;
            if (parserResult.resultType() != ParserResult.Type.SUCCESS) {
                return new EvaluateResult(EvaluateResult.Type.PARSER_ERROR, script, new Value(), parserResult.errorMsg(), parserResult.errorLine(), parserResult.errorColumn());
            }
        }

        Value result = new Value();

        try {
            result = (new Block(nodes)).evaluate(ctx).getReferedValue();
            isEvaluated = true;
        } catch (EvaluateException e) {
            return new EvaluateResult(EvaluateResult.Type.EVALUATE_ERROR, script, result, e.getMessage(), 0, 0);
        }

        return new EvaluateResult(EvaluateResult.Type.SUCCESS, script, result, "", 0, 0);
    }

    @NotNull
    public String getScript() {
        return script;
    }

    public void setScript(@NotNull String script) {
        isParsed = false;
        isEvaluated = false;
        this.script = script;
    }

    @NotNull
    public Context getContext() {
        return context;
    }

    public boolean isEvaluated() {
        return isEvaluated;
    }

    public boolean isParsed() {
        return isParsed;
    }

    public @Nullable ParserResult getParserResult() {
        return parserResult;
    }

    @Override
    public String toString() {
        return "Script{" +
                "script='" + script + '\'' +
                '}';
    }
}
