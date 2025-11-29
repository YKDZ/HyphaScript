package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;

@ObjectName("Date")
public class DateObject extends InternalObject {
    @Static
    @Function("now")
    public static long now() {
        return System.currentTimeMillis();
    }
}
