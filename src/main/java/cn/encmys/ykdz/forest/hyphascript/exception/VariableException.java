package cn.encmys.ykdz.forest.hyphascript.exception;

import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

public class VariableException extends RuntimeException {
    @NotNull
    private final Reference reference;
    @NotNull
    private final String message;

    public VariableException(@NotNull final Reference reference, @NotNull final String message) {
        this.reference = reference;
        this.message = message;
    }

    @Override
    public String toString() {
        return "VariableException{" +
                "variable=" + reference +
                ", message='" + message + '\'' +
                '}';
    }
}
