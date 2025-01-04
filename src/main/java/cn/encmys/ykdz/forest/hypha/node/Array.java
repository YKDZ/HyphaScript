package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class Array extends ASTNode {
    @NotNull
    private final List<ASTNode> values;

    public Array(@NotNull List<ASTNode> values) {
        this.values = values;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        return new Reference(null, new Value(values.stream()
                .map(value -> value.evaluate(ctx))
                .toArray()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Array array = (Array) o;
        return Objects.equals(values, array.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }
}
