package cn.encmys.ykdz.forest.hypha.exception;

import cn.encmys.ykdz.forest.hypha.function.Function;
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
    public String toString() {
        return "FunctionException{" +
                "message='" + message + '\'' +
                ", function=" + function +
                ", cause=" + cause +
                '}';
    }
}
