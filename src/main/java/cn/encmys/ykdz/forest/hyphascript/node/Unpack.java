package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Unpack extends ASTNode {
    private final boolean isConst;
    @NotNull
    private final ASTNode from;
    @NotNull
    private final List<String> to;

    public Unpack(boolean isConst, @NotNull ASTNode from, @NotNull List<String> to, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.isConst = isConst;
        this.from = from;
        this.to = to;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value fromValue = from.evaluate(ctx).getReferredValue();

        switch (fromValue.getType()) {
            case ARRAY -> {
                Reference[] arr = fromValue.getAsArray();
                for (int i = 0; i < to.size() && i < arr.length; i++) {
                    ctx.declareMember(to.get(i), new Reference(arr[i].getReferredValue(), isConst));
                }
            }
            case SCRIPT_OBJECT -> {
                ScriptObject object = fromValue.getAsScriptObject();
                to.forEach(to -> ctx.declareMember(to, new Reference(object.findMember(to).getReferredValue(), isConst)));
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
