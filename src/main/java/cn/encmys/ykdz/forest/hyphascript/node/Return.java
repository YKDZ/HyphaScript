package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.ReturnNotificationException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Return extends ASTNode {
    @Nullable
    private final ASTNode returnValue;

    public Return(@Nullable ASTNode returnValue, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.returnValue = returnValue;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        if (returnValue == null) {
            throw new ReturnNotificationException(new Reference());
        }
        throw new ReturnNotificationException(returnValue.evaluate(ctx));
    }

    @Override
    public String toString() {
        return "Return{" +
                "returnValue=" + returnValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Return aReturn = (Return) o;
        return Objects.equals(returnValue, aReturn.returnValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(returnValue);
    }
}
