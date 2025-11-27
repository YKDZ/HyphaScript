package cn.encmys.ykdz.forest.hyphascript.function;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JavaFunction extends ScriptObject implements Function {
    private @NotNull String name;
    private @NotNull JavaFunctionBody body;

    public JavaFunction(@NotNull String name, @NotNull JavaFunctionBody body) {
        this.name = name;
        this.body = body;
    }

    @Override
    public @NotNull Reference call(@NotNull Value target, @NotNull List<Value> parameters, @NotNull Context ctx) {
        return new Reference(new Value(body.apply(target, parameters, ctx)));
    }

    @Override
    public @NotNull Reference call(@NotNull Value target, @NotNull Map<String, Value> parameters, @NotNull Context ctx) {
        return new Reference(new Value(body.apply(target, parameters.values().stream().toList(), ctx)));
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull LinkedHashMap<String, ASTNode> getParameters() {
        return new LinkedHashMap<>();
    }

    @Override
    public @NotNull Context getCapturedContext() {
        return new Context();
    }

    @Override
    public void setCapturedContext(@NotNull Context capturedContext) {
    }

    @Override
    public @NotNull ScriptObject getPrototype() {
        return super.findMember("prototype").getReferredValue().getAsScriptObject();
    }

    @Override
    public void setManualTarget(@Nullable Value manualTarget) {

    }

    @Override
    public ScriptObject clone() {
        try {
            JavaFunction cloned = (JavaFunction) super.clone();

            cloned.name = this.name;
            cloned.body = this.body;

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
