package cn.encmys.ykdz.forest.hypha.exception;

import cn.encmys.ykdz.forest.hypha.value.Value;
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
    public String toString() {
        return "ValueException{" +
                "value=" + value +
                ", message='" + message + '\'' +
                '}';
    }
}
