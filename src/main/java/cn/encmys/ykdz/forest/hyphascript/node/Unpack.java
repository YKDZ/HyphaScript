package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Unpack extends ASTNode {
    private final boolean isConst;
    @NotNull
    private final ASTNode from;
    @NotNull
    private final List<String> to;

    public Unpack(boolean isConst, @NotNull ASTNode from, @NotNull List<String> to) {
        this.isConst = isConst;
        this.from = from;
        this.to = to;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value fromValue = from.evaluate(ctx).getReferedValue();

        switch (fromValue.getType()) {
            case ARRAY -> {
                Map<Integer, Reference> arr = fromValue.getAsArray();
                for (int i = 0; i < to.size() && i < arr.size(); i++) {
                    ctx.declareReference(to.get(i), arr.get(i).getReferedValue(), isConst, false);
                }
            }
            case NESTED_OBJECT -> {
                Map<String, Reference> nestedObjects = fromValue.getAsNestedObject();
                for (String to : to) {
                    ctx.declareReference(to, nestedObjects.get(to).getReferedValue(), isConst, false);
                }
            }
            default -> throw new EvaluateException(this, "Unpack can not be casted in this type of value");
        }

        return new Reference();
    }

    @Override
    public String toString() {
        return "Unpack{" +
                "isConst=" + isConst +
                ", from=" + from +
                ", to=" + to +
                '}';
    }
}
