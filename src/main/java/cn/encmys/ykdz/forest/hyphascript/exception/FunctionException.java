package cn.encmys.ykdz.forest.hyphascript.exception;

import cn.encmys.ykdz.forest.hyphascript.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FunctionException extends RuntimeException {
    @NotNull
    private final String message;
    @NotNull
    private final Function function;
    @Nullable
    private final Throwable cause;

    public FunctionException(@NotNull Function function, @NotNull String message, @Nullable Throwable cause) {
        this.message = message;
        this.function = function;
        this.cause = cause;
    }

    @Override
    public @Nullable Throwable getCause() {
        return cause;
    }

    @Override
    public @NotNull String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "FunctionException{" +
                "message='" + message + '\'' +
                ", function=" + function +
                ", cause=" + cause +
                '}';
    }
}
