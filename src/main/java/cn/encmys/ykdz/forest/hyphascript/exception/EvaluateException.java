package cn.encmys.ykdz.forest.hyphascript.exception;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvaluateException extends RuntimeException {
    private final @NotNull String message;
    private final @NotNull ASTNode node;
    private final @Nullable Throwable cause;

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

    @Override
    public String toString() {
        return "EvaluateException{" +
                "message='" + message + '\'' +
                ", node=" + node +
                ", cause=" + cause +
                '}';
    }
}
