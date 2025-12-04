package cn.encmys.ykdz.forest.hyphascript.logger;

import org.jetbrains.annotations.NotNull;

public interface Logger {
    void info(@NotNull String msg);

    void warn(@NotNull String msg);

    void error(@NotNull String msg);

    void debug(@NotNull String msg);
}
