package cn.encmys.ykdz.forest.hyphascript.exception;

import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public class ValueException extends RuntimeException {
    @NotNull
    private final Value value;
    @NotNull
    private final String message;

    public ValueException(@NotNull Value value, @NotNull String message) {
        this.value = value;
        this.message = message;
    }

    @Override
    public @NotNull String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ValueException{" +
                "value=" + value +
                ", message='" + message + '\'' +
                '}';
    }
}
