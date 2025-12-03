package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.HyphaScript;
import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@ObjectName("Server")
public class ServerObject extends InternalObject {
    @Static
    @Function("broadcast")
    @FunctionParas("msg")
    public static void broadcast(@NotNull Context ctx) {
        ContextUtils.getComponentParam(ctx, "msg")
                .ifPresent(msg -> HyphaScript.getPlugin()
                        .ifPresent(instance -> instance.getServer().broadcast(msg)));
    }

    @Static
    @Function("player_from_name")
    @FunctionParas("name")
    public static @Nullable Player playerFromName(@NotNull Context ctx) {
        return ContextUtils.getStringParam(ctx, "name")
                .flatMap(name -> HyphaScript.getPlugin()
                        .map(instance -> instance.getServer().getPlayer(name)))
                .orElse(null);
    }

    @Static
    @Function("player_from_uuid")
    @FunctionParas("uuid")
    public static @Nullable Player playerFromUUID(@NotNull Context ctx) {
        return ContextUtils.getStringParam(ctx, "uuid")
                .flatMap(uuid -> HyphaScript.getPlugin()
                        .map(instance -> instance.getServer().getPlayer(UUID.fromString(uuid))))
                .orElse(null);
    }
}
