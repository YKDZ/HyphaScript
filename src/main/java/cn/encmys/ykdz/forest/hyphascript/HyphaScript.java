package cn.encmys.ykdz.forest.hyphascript;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class HyphaScript {
    public static @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

    private static @Nullable Plugin PLUGIN_INSTANCE;

    public static void init(@NotNull Plugin plugin) {
        PLUGIN_INSTANCE = plugin;
    }

    public static @NotNull Optional<Plugin> getPlugin() {
        return Optional.ofNullable(PLUGIN_INSTANCE);
    }
}
