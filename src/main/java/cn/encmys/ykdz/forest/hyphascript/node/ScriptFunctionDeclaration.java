package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.function.ScriptFunction;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ScriptFunctionDeclaration extends ASTNode {
    @NotNull
    private final String name;
    @NotNull
    private final List<String> receivers;
    @NotNull
    private final List<String> parameters;
    @NotNull
    private final ASTNode body;

    public ScriptFunctionDeclaration(@NotNull String name, @NotNull List<String> receivers, @NotNull List<String> parameters, @NotNull ASTNode body) {
        this.name = name;
        this.receivers = receivers;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        ctx.declareReference(name, new Value(new ScriptFunction(receivers, parameters, body)));
        return new Reference();
    }

    @Override
    public String toString() {
        return "ScriptFunctionDefinition{" +
                "name='" + name + '\'' +
                ", parameters=" + parameters +
                ", body=" + body +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptFunctionDeclaration that = (ScriptFunctionDeclaration) o;
        return Objects.equals(name, that.name) && Objects.equals(parameters, that.parameters) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameters, body);
    }
}
