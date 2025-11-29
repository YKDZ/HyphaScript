package cn.encmys.ykdz.forest.hyphascript.exception;

import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import org.jetbrains.annotations.NotNull;

public class ParserException extends RuntimeException {
    private final @NotNull String message;
    private final @NotNull Token token;

    public ParserException(@NotNull String message, @NotNull Token token) {
        this.message = message;
        this.token = token;
    }

    @Override
    public @NotNull String getMessage() {
        return message;
    }

    public @NotNull Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "ParserException{" +
                "message='" + message + '\'' +
                ", token=" + token +
                '}';
    }
}
