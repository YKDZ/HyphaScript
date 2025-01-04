package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ReturnNotificationException;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Return extends ASTNode {
    @Nullable
    private final ASTNode returnValue;

    public Return(@Nullable ASTNode returnValue) {
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
