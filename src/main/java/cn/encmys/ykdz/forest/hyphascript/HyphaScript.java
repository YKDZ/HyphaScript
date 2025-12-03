package cn.encmys.ykdz.forest.hyphascript;

import cn.encmys.ykdz.forest.hyphascript.logger.Logger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class HyphaScript {
    public static @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

    private static @Nullable Logger LOGGER_INSTANCE;
    private static @Nullable Plugin PLUGIN_INSTANCE;

    public static void init(@NotNull Plugin plugin, @NotNull Logger logger) {
        PLUGIN_INSTANCE = plugin;
        LOGGER_INSTANCE = logger;
    }

    public static @NotNull Optional<Plugin> getPlugin() {
        return Optional.ofNullable(PLUGIN_INSTANCE);
    }

    public static @NotNull Optional<Logger> getLogger() {
        return Optional.ofNullable(LOGGER_INSTANCE);
    }
}
