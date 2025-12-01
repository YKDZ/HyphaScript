package cn.encmys.ykdz.forest.hyphascript.value;

import java.util.Map;
import java.util.TreeMap;

public class ScriptArray extends TreeMap<Integer, Reference> {
    public ScriptArray() {
        super();
    }

    public ScriptArray(Map<Integer, Reference> map) {
        super(map);
    }

    public int length() {
        if (isEmpty())
            return 0;
        return lastKey() + 1;
    }
}
