package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.node.pattern.UnpackPattern;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public class Unpack extends ASTNode {
    private final boolean isConst;
    @NotNull
    private final ASTNode from;
    @NotNull
    private final UnpackPattern pattern;

    public Unpack(boolean isConst, @NotNull ASTNode from, @NotNull UnpackPattern pattern, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.isConst = isConst;
        this.from = from;
        this.pattern = pattern;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value fromValue = from.evaluate(ctx).getReferredValue();
        pattern.apply(this, ctx, fromValue, isConst);
        return new Reference();
    }

    @Override
    public String toString() {
        return "Unpack{" +
                "isConst=" + isConst +
                ", from=" + from +
                ", pattern=" + pattern +
                '}';
    }
}
