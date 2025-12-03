package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.HyphaScript;
import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;

@ObjectName("Date")
public class DateObject extends InternalObject {
    @Static
    @Function("now")
    public static long now() {
        try {
            return System.currentTimeMillis();
        } catch (Exception e) {
            HyphaScript.getLogger().ifPresent(
                    logger ->
                            logger.warning("Error when getting current time using Date#now, 0 will be returned as fallback. " + e.getMessage())
            );
            return 0L;
        }
    }
}
