package cn.encmys.ykdz.forest.hyphascript.node.pattern;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ObjectPattern(@NotNull Map<@NotNull String, @NotNull UnpackPattern> properties) implements UnpackPattern {

    @Override
    public void apply(@NotNull ASTNode node, @NotNull Context ctx, @NotNull Value value, boolean isConst) {
        if (value.type() != Value.Type.SCRIPT_OBJECT) {
            throw new EvaluateException(node, "Value is not an object, cannot unpack");
        }

        ScriptObject object = value.getAsScriptObject();
        for (Map.Entry<String, UnpackPattern> entry : properties.entrySet()) {
            String key = entry.getKey();
            UnpackPattern pattern = entry.getValue();
            Value memberValue = object.findMember(key).getReferredValue();
            pattern.apply(node, ctx, memberValue, isConst);
        }
    }
}
