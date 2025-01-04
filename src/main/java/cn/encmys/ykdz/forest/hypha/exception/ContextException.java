package cn.encmys.ykdz.forest.hypha.exception;

import cn.encmys.ykdz.forest.hypha.context.Context;
import org.jetbrains.annotations.NotNull;

public class ContextException extends RuntimeException {
    @NotNull
    private final String message;
    @NotNull
    private final Context ctx;

    public ContextException(@NotNull Context ctx, @NotNull String message) {
        this.ctx = ctx;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ContextException{" +
               "message='" + message + '\'' +
               ", ctx=" + ctx +
               '}';
    }
}
