package cn.encmys.ykdz.forest.hyphascript.exception;

import org.jetbrains.annotations.NotNull;

public class LexerException extends RuntimeException {
    private final @NotNull String message;
    private final @NotNull String script;
    private final int line;
    private final int column;

    public LexerException(@NotNull String message, @NotNull String script,  int line, int column) {
        this.message = message;
        this.script = script;
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public @NotNull String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "LexerException{" +
                "message='" + message + '\'' +
                ", script='" + script + '\'' +
                ", line=" + line +
                ", column=" + column +
                '}';
    }
}
