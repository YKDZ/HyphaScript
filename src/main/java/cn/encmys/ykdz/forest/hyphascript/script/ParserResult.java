package cn.encmys.ykdz.forest.hyphascript.script;

import org.jetbrains.annotations.NotNull;

public record ParserResult(@NotNull String script, @NotNull Type resultType, @NotNull String errorMsg, int errorLine, int errorColumn) {
    public enum Type {
        PARSED,
        LEXER_ERROR,
        PARSER_ERROR,
        SUCCESS
    }

    @Override
    public String toString() {
        return "ParserResult{" +
                "script='" + script + '\'' +
                ", resultType=" + resultType +
                ", errorMsg='" + errorMsg + '\'' +
                ", errorLine=" + errorLine +
                ", errorColumn=" + errorColumn +
                '}';
    }
}
