package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.ScriptArray;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

@ObjectName("ArrayIterator")
public class ArrayIteratorObject extends InternalObject {
    @Function("next")
    public static ScriptObject next(@NotNull Context ctx) {
        ScriptObject thisIterator = ctx.findMember("this").getReferredValue().getAsScriptObject();
        int index = thisIterator.findMember("index").getReferredValue().getAsBigDecimal().intValue();
        ScriptArray array = thisIterator.findMember("array").getReferredValue().getAsArray();

        ScriptObject result = new ScriptObject();
        if (index < array.length()) {
            Reference value = array.get(index);
            Value val = (value != null) ? value.getReferredValue() : new Value(null);

            result.declareMember("value", new Reference(val));
            result.declareMember("done", new Value(false));

            thisIterator.putMember("index", new Reference(new Value(index + 1)));
        } else {
            result.declareMember("value", new Value(null));
            result.declareMember("done", new Value(true));
        }
        return result;
    }
}
