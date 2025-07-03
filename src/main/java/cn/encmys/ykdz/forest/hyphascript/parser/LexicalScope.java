package cn.encmys.ykdz.forest.hyphascript.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class LexicalScope {
    private final @NotNull List<@NotNull String> identifiers = new ArrayList<>();
    private final @Nullable LexicalScope parent;
    private final @NotNull List<@NotNull LexicalScope> children = new ArrayList<>();

    private LexicalScope(@Nullable LexicalScope parent) {
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public static @NotNull LexicalScope create() {
        return new LexicalScope(null);
    }

    public @NotNull LexicalScope enter() {
        return new LexicalScope(this);
    }

    public @NotNull LexicalScope leave() {
        if (parent == null) throw new AssertionError("Cannot leave to a null lexical scope");
        return parent;
    }

    public void pushIdentifier(@NotNull String identifier) {
        identifiers.add(identifier);
    }

    public @NotNull @Unmodifiable List<String> getIdentifiers() {
        return Collections.unmodifiableList(identifiers);
    }

    public @NotNull @Unmodifiable List<LexicalScope> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean contains(@NotNull String identifier) {
        if (identifiers.contains(identifier)) {
            return true;
        }
        for (LexicalScope child : children) {
            if (child.contains(identifier)) {
                return true;
            }
        }
        return false;
    }

    public @NotNull Set<@NotNull String> flattenToSet() {
        Set<String> result = new HashSet<>();
        flattenIntoSet(result);
        return result;
    }

    private void flattenIntoSet(@NotNull Set<@NotNull String> result) {
        result.addAll(identifiers);
        for (LexicalScope child : children) {
            child.flattenIntoSet(result);
        }
    }

    @Override
    public String toString() {
        return formatAsTree("", true);
    }

    private @NotNull String formatAsTree(@NotNull String prefix, boolean isTail) {
        String connector = isTail ? "└── " : "├── ";
        String currentLine = prefix + connector + "Scope: " + formatIdentifiers();

        StringBuilder builder = new StringBuilder(currentLine);
        for (int i = 0; i < children.size(); i++) {
            boolean isChildTail = (i == children.size() - 1);
            String childPrefix = prefix + (isTail ? "    " : "│   ");
            builder.append("\n").append(children.get(i).formatAsTree(childPrefix, isChildTail));
        }

        return builder.toString();
    }

    private @NotNull String formatIdentifiers() {
        return identifiers.isEmpty()
                ? "[]"
                : "[" + String.join(", ", identifiers) + "]";
    }
}
