package cn.encmys.ykdz.forest.hyphascript.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Logger {
    void info(@NotNull String msg);

    void warn(@NotNull String msg);

    void error(@Nullable String msg);
}
