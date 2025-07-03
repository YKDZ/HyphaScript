package cn.encmys.ykdz.forest.hyphascript.exception;

import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptObjectException extends RuntimeException {
    private final @NotNull ScriptObject obj;
    private final @NotNull String message;
    private final @Nullable Throwable cause;

    public ScriptObjectException(@NotNull ScriptObject obj, @NotNull String message) {
        this(obj, message, null);
    }

    public ScriptObjectException(@NotNull ScriptObject obj, @NotNull String message, @Nullable Throwable cause) {
        this.obj = obj;
        this.message = message;
        this.cause = cause;
    }

    @Override
    public @NotNull String getMessage() {
        return message;
    }

    @Override
    public @Nullable Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return "ScriptObjectException{" +
                "obj=" + obj +
                ", message='" + message + '\'' +
                ", cause=" + cause +
                '}';
    }
}
