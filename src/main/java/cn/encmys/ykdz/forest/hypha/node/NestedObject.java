package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class NestedObject extends ASTNode {
    @NotNull
    private final Map<String, ASTNode> objects;

    public NestedObject(@NotNull Map<String, ASTNode> objects) {
        this.objects = objects;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        return new Reference(null, new Value(objects.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().evaluate(ctx)
                ))));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NestedObject that = (NestedObject) o;
        return Objects.equals(objects, that.objects);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(objects);
    }
}
