package cn.encmys.ykdz.forest.hyphascript.node.pattern;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.ScriptArray;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ArrayPattern(@NotNull List<@NotNull UnpackPattern> elements) implements UnpackPattern {

    @Override
    public void apply(@NotNull ASTNode node, @NotNull Context ctx, @NotNull Value value, boolean isConst) {
        if (value.getType() != Value.Type.ARRAY) {
            throw new EvaluateException(node, "Value is not an array, cannot unpack");
        }

        ScriptArray arr = value.getAsArray();
        for (int i = 0; i < elements.size(); i++) {
            UnpackPattern pattern = elements.get(i);
            Reference ref = arr.get(i);
            Value val = (ref != null) ? ref.getReferredValue() : new Value(null);
            pattern.apply(node, ctx, val, isConst);
        }
    }
}
