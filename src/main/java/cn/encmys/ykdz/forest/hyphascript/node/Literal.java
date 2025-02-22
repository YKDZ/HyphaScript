package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Literal extends ASTNode {
    @NotNull
    private final Reference value;

    public Literal() {
        this.value = new Reference();
    }

    public Literal(@NotNull Value value) {
        this.value = new Reference(null, value);
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        return value;
    }

    @Override
    public String toString() {
        return "Literal{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Literal literal = (Literal) o;
        return Objects.equals(value, literal.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
