package cn.encmys.ykdz.forest.hypha.exception;

import cn.encmys.ykdz.forest.hypha.value.Reference;
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
