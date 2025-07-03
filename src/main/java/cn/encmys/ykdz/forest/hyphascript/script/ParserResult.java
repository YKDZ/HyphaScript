package cn.encmys.ykdz.forest.hyphascript.script;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ParserResult(@NotNull String script, @NotNull Type resultType, long timeCost, @NotNull String errorMsg,
                           int errorLine,
                           int errorColumn) {
    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        if (resultType != Type.LEXER_ERROR && resultType != Type.PARSER_ERROR) {
            return "";
        }

        String[] lines = script.split("\\R");
        StringBuilder sb = new StringBuilder();

        String errorType = resultType == Type.LEXER_ERROR ? "Lexer Error" : "Parser Error";
        sb.append(errorType)
                .append(" in ").append(timeCost).append(" ms: ").append(errorMsg)
                .append(" (Line ").append(errorLine).append(", Column ").append(errorColumn).append(")")
                .append("\n");

        int errorLineIndex = errorLine - 1;
        if (errorLineIndex >= 0 && errorLineIndex < lines.length) {
            int startLine = Math.max(0, errorLineIndex - 1);
            int endLine = Math.min(lines.length, errorLineIndex + 2);

            for (int i = startLine; i < endLine; i++) {
                sb.append(lines[i]).append("\n");
                if (i == errorLineIndex) {
                    String line = lines[i];
                    int lineLength = line.length();
                    int spaces = Math.min(errorColumn - 1, lineLength);
                    String indicator = " ".repeat(spaces) + "^";
                    sb.append(indicator).append("\n");
                }
            }
        } else {
            sb.append("[Error line not available]\n");
        }

        return sb.toString().trim();
    }

    public enum Type {
        PARSED,
        LEXER_ERROR,
        PARSER_ERROR,
        SUCCESS
    }
}
