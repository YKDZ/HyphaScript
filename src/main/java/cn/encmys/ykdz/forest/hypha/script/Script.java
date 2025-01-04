package cn.encmys.ykdz.forest.hypha.script;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.node.ASTNode;
import cn.encmys.ykdz.forest.hypha.node.Block;
import cn.encmys.ykdz.forest.hypha.parser.Lexer;
import cn.encmys.ykdz.forest.hypha.parser.Parser;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Script {
    @NotNull
    private String script;
    @NotNull
    private List<ASTNode> nodes = new ArrayList<>();
    @NotNull
    private final Context context;
    private boolean isParsed = false;
    private boolean isEvaluated = false;

    public Script(@NotNull String script, @NotNull Context context) {
        this.script = script;
        this.context = context;
    }

    public void parse() {
        if (isParsed) return;

        try {
            Lexer lexer = new Lexer(script);
            Parser parser = new Parser(lexer.tokenize());
            nodes = parser.parse();
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    /**
     * Evaluate the script with default context provided in the constructor
     * @return Evaluated result
     */
    @NotNull
    public Value evaluate() {
        return evaluate(context);
    }

    /**
     * Evaluate the script with given context
     * @return Evaluated result
     */
    @NotNull
    public Value evaluate(@NotNull Context ctx) {
        if (!isParsed) parse();

        Value result = new Value();

        if (isEvaluated) return result;

        try {
            result = (new Block(nodes)).evaluate(ctx).getReferedValue();
            isEvaluated = true;
        } catch (ScriptException e) {
            e.printStackTrace();
        }

        return result;
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

    @Override
    public String toString() {
        return "Script{" +
                "script='" + script + '\'' +
                '}';
    }
}
