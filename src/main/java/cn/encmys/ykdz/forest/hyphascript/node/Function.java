package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.function.ScriptFunction;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public class Function extends ASTNode {
    private final @NotNull String name;
    private final @NotNull LinkedHashMap<String, ASTNode> parameters;
    private final @NotNull String uncertainParameter;
    private final @NotNull ASTNode body;

    public Function(@NotNull String name, @NotNull LinkedHashMap<String, ASTNode> parameters, @NotNull String uncertainParameter, @NotNull ASTNode body, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.name = name;
        this.parameters = parameters;
        this.uncertainParameter = uncertainParameter;
        this.body = body;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        return new Reference(new Value(new ScriptFunction(name, parameters, uncertainParameter, body, ctx)));
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Function{" +
                "name='" + name + '\'' +
                ", parameters=" + parameters +
                ", uncertainParameter='" + uncertainParameter + '\'' +
                ", body=" + body +
                '}';
    }
}