package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaAdventureUtils;
import org.jetbrains.annotations.NotNull;

@ObjectName("console")
public class ConsoleObject extends InternalObject {
    @Static
    @Function("log")
    @FunctionParas({"message"})
    public static void log(@NotNull Context ctx) {
        HyphaAdventureUtils.sendConsoleMessage(ctx.findMember("message").getReferredValue().toReadableString());
    }
}
