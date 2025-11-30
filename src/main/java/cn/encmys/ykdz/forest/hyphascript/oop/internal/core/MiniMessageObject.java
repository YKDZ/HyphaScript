package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.HyphaScript;
import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@ObjectName("MiniMessage")
public class MiniMessageObject extends InternalObject {
    @Static
    @Function("deser")
    @FunctionParas("str")
    public static Component deser(@NotNull Context ctx) {
        return HyphaScript.miniMessage.deserialize(ctx.findMember("str").getReferredValue().getAsString());
    }

    @Static
    @Function("ser")
    @FunctionParas("component")
    public static String ser(@NotNull Context ctx) {
        return HyphaScript.miniMessage.serialize(ctx.findMember("component").getReferredValue().getAsAdventureComponent());
    }
}
