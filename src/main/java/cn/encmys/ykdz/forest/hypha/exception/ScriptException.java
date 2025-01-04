package cn.encmys.ykdz.forest.hypha.exception;

import cn.encmys.ykdz.forest.hypha.node.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptException extends RuntimeException {
    @NotNull
    private final String message;
    @NotNull
    private final ASTNode node;
    @Nullable
    private final Throwable cause;

    public ScriptException(@NotNull ASTNode node, @NotNull String message) {
        super();
        this.message = message;
        this.node = node;
        this.cause = null;
    }

    public ScriptException(@NotNull ASTNode node, @NotNull String message, @NotNull Throwable cause) {
        super();
        this.message = message;
        this.node = node;
        this.cause = cause;
    }

    @Override
    public String toString() {
        return "ScriptException{" +
                "message='" + message + '\'' +
                ", node=" + node +
                ", cause=" + cause +
                '}';
    }
}
