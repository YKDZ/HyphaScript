package cn.encmys.ykdz.forest.hyphascript.value;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

public class ScriptArray extends TreeMap<Integer, Reference> {
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
}
