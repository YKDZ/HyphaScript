package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Const extends ASTNode {
    @NotNull
    private final String name;
    @NotNull
    private final ASTNode initValue;
    private final boolean isExported;

    public Const(@NotNull String name, @NotNull ASTNode initValue, boolean isExported, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.name = name;
        this.initValue = initValue;
        this.isExported = isExported;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Reference initRef = initValue.evaluate(ctx);
        initRef.setConst(true);
        try {
            ctx.declareMember(name, initRef);
            if (isExported) ctx.setExported(name);
        } catch (Exception e) {
            throw new EvaluateException(this, "Error declaring reference " + name + " in " + ctx, e);
        }
        return new Reference();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Const aConst = (Const) o;
        return isExported == aConst.isExported && Objects.equals(name, aConst.name) && Objects.equals(initValue, aConst.initValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, initValue, isExported);
    }
}