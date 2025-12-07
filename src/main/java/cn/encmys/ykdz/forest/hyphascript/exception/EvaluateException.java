package cn.encmys.ykdz.forest.hyphascript.exception;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvaluateException extends RuntimeException {
    private final @NotNull String message;
    private final @NotNull ASTNode node;
    private final @Nullable Throwable cause;
    private @Nullable String script;

    public EvaluateException(@NotNull ASTNode node, @NotNull String message) {
        super();
        this.message = message;
        this.node = node;
        this.cause = null;
    }

    public EvaluateException(@NotNull ASTNode node, @NotNull String message, @NotNull Throwable cause) {
        super();
        this.message = message;
        this.node = node;
        this.cause = cause;
    }

    public void setScript(@Nullable String script) {
        this.script = script;
    }

    public @Nullable String getScript() {
        return script;
    }

    public @NotNull ASTNode getNode() {
        return node;
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
        return "EvaluateException{" +
                "message='" + message + '\'' +
                ", node=" + node +
                ", cause=" + cause +
                '}';
    }
}
