package cn.encmys.ykdz.forest.hyphascript.value;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ScriptArray extends TreeMap<Integer, Reference> {
    private static final @NotNull ThreadLocal<Set<ScriptArray>> HASH_CODE_VISITED = ThreadLocal
            .withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

    public ScriptArray() {
        super();
    }

    public ScriptArray(@NotNull Map<@NotNull Integer, @NotNull Reference> map) {
        super(map);
    }

    public int length() {
        if (isEmpty())
            return 0;
        return lastKey() + 1;
    }

    public void push(@NotNull Reference value) {
        put(length(), value);
    }

    @Override
    public int hashCode() {
        final Set<ScriptArray> visited = HASH_CODE_VISITED.get();
        if (visited.contains(this)) {
            return 0;
        }
        visited.add(this);
        try {
            return super.hashCode();
        } finally {
            visited.remove(this);
            if (visited.isEmpty()) {
                HASH_CODE_VISITED.remove();
            }
        }
    }
}
