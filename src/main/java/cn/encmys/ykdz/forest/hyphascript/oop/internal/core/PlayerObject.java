package cn.encmys.ykdz.forest.hyphascript.oop.internal.core;

import cn.encmys.ykdz.forest.hyphascript.HyphaScript;
import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.ObjectName;
import cn.encmys.ykdz.forest.hyphascript.annotions.Static;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObject;
import cn.encmys.ykdz.forest.hyphascript.utils.ContextUtils;
import cn.encmys.ykdz.forest.hyphautils.utils.HyphaAdventureUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@ObjectName("Player")
public class PlayerObject extends InternalObject {
    @Static
    @Function("sound")
    @FunctionParas({"sound", "volume", "pitch", "__player"})
    public static void sound(@NotNull Context ctx) {
        Player player = ContextUtils.getPlayer(ctx).orElse(null);

        if (player == null) return;

        Sound sound;

        try {
            sound = Registry.SOUND_EVENT.get(Key.key(ContextUtils.getStringParam(ctx, "sound").orElse("")));
        } catch (Exception ignored) {
            return;
        }

        if (sound == null) sound = Sound.BLOCK_ANVIL_BREAK;

        float volume = ContextUtils.getFloatParam(ctx, "volume").orElse(1f);
        float pitch = ContextUtils.getFloatParam(ctx, "pitch").orElse(1f);

        player.playSound(player, sound, volume, pitch);
    }

    @Static
    @Function("message")
    @FunctionParas({"msg", "__player"})
    public static void message(@NotNull Context ctx) {
        ContextUtils.getPlayer(ctx)
                .ifPresent(player ->
                        ContextUtils.getStringParam(ctx, "msg")
                                .ifPresent(msg -> HyphaAdventureUtils.sendPlayerMessage(player, msg))
                );
    }

    @Static
    @Function("tp_to_loc")
    @FunctionParas({"world", "x", "y", "z", "yaw", "pitch", "__player"})
    public static void tpToLoc(@NotNull Context ctx) {
        HyphaScript.getPlugin()
                .map(instance -> instance.getServer().getWorld(ContextUtils.getStringParam(ctx, "world").orElse("")))
                .ifPresent(world -> ContextUtils.getPlayer(ctx)
                        .ifPresent(player -> player.teleport(
                                new Location(
                                        world,
                                        ContextUtils.getDoubleParam(ctx, "x").orElse(0),
                                        ContextUtils.getDoubleParam(ctx, "y").orElse(0),
                                        ContextUtils.getDoubleParam(ctx, "z").orElse(0),
                                        ContextUtils.getFloatParam(ctx, "yaw").orElse(0f),
                                        ContextUtils.getFloatParam(ctx, "pitch").orElse(0f)
                                )
                        )));
    }

    @Static
    @Function("tp_to_player")
    @FunctionParas({"name", "__player"})
    public static void tpToPlayer(@NotNull Context ctx) {
        ContextUtils.getStringParam(ctx, "name")
                .flatMap(name -> HyphaScript.getPlugin()
                        .flatMap(instance ->
                                Optional.ofNullable(instance.getServer().getPlayer(name))))
                .ifPresent(target -> ContextUtils.getPlayer(ctx)
                        .ifPresent(player -> player.teleport(
                                target
                        )));
    }
}
