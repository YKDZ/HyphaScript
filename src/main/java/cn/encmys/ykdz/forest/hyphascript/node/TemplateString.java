package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TemplateString extends ASTNode {
    @NotNull
    private final List<ASTNode> parts;
    private final boolean isOptional;

    public TemplateString(@NotNull List<ASTNode> parts, boolean isOptional, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.parts = parts;
        this.isOptional = isOptional;
    }

    private static @NotNull Reference buildComponent(@NotNull List<Value> parts, boolean isOptional, boolean overflow) {
        Component result = null;
        for (Value value : parts) {
            switch (value.getType()) {
                case VOID -> {
                }
                case NULL -> {
                    if (isOptional) return new Reference(new Value(null));
                }
                default -> {
                    Component comp = value.getAsAdventureComponent();
                    if (result == null) {
                        result = overflow ? comp : Component.empty().append(comp);
                    } else {
                        result = result.append(comp);
                    }
                }
            }
        }
        return new Reference(new Value(result == null ? Component.empty() : result));
    }

    private static @NotNull Reference buildString(@NotNull List<Value> parts, boolean isOptional) {
        StringBuilder result = new StringBuilder();
        for (Value value : parts) {
            switch (value.getType()) {
                case VOID -> {
                }
                case NULL -> {
                    if (isOptional) return new Reference(new Value(null));
                }
                default -> result.append(value.getAsString());
            }
        }
        return new Reference(new Value(result.toString()));
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        List<Value> partValues = parts.stream()
                .map(node -> node.evaluate(ctx).getReferredValue())
                .toList();
        // 只要包含一个组件，即视为组件模板串
        boolean isComponent = partValues.stream()
                .anyMatch(value -> value.isType(Value.Type.ADVENTURE_COMPONENT));
        return isComponent ? buildComponent(partValues, isOptional, ctx.getConfig().componentDecorationOverflow()) : buildString(partValues, isOptional);
    }
}
