package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TemplateString extends ASTNode {
    @NotNull
    private final List<ASTNode> parts;

    public TemplateString(@NotNull List<ASTNode> parts) {
        this.parts = parts;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        StringBuilder result = new StringBuilder();
        for (ASTNode part : parts) {
            Value value = part.evaluate(ctx).getReferedValue();
            switch (value.getType()) {
                case NULL -> result.append("null");
                case VOID -> {
                }
                default -> result.append(value.getValue());
            }
        }
        return new Reference(null, new Value(result.toString()));
    }
}
