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

public interface Function {
    @NotNull Reference call(@NotNull Value targetValue, @NotNull List<Value> parameters, @NotNull Context ctx);

    @NotNull Reference call(@NotNull Value targetValue, @NotNull Map<String, Value> parameters, @NotNull Context ctx);

    @NotNull String getName();

    @NotNull LinkedHashMap<String, ASTNode> getParameters();

    @NotNull Context getCapturedContext();

    void setCapturedContext(@NotNull Context capturedContext);

    @NotNull ScriptObject getPrototype();

    void setManualTarget(@Nullable Value manualTarget);

    ScriptObject clone();

    @NotNull String toString();
}
