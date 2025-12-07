package cn.encmys.ykdz.forest.hyphascript.script;

import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public record EvaluateResult(@NotNull Type type, @NotNull String script, @NotNull Value value, long timeCost,
                             @NotNull String errorMsg, @Nullable Throwable cause,
                             int errorStartLine, int errorStartColumn, int errorEndLine, int errorEndColumn) {
    @Override
    public @NotNull String toString() {
        if (type == Type.SUCCESS) {
            return "";
        }

        String[] lines = script.split("\\R");
        StringBuilder sb = new StringBuilder();
        sb.append("Script Error").append(" in ").append(timeCost).append(" ms: ").append(errorMsg);

        if (cause != null) {
            sb.append("\n").append(
                    Arrays.stream(cause.getStackTrace())
                            .map(StackTraceElement::toString)
                            .collect(Collectors.joining("\n")));
        }

        // 添加具体错误位置信息
        if (errorStartLine == errorEndLine) {
            sb.append(" (Line ").append(errorStartLine)
                    .append(", Columns ").append(errorStartColumn).append("-").append(errorEndColumn).append(")");
        } else {
            sb.append(" (Line ").append(errorStartLine).append(":").append(errorStartColumn)
                    .append(" to Line ").append(errorEndLine).append(":").append(errorEndColumn).append(")");
        }
        sb.append("\n");

        // 显示错误行及附近1行上下文
        int startLineIndex = Math.max(0, errorStartLine - 2); // -2 表示向前显示1行上下文
        int endLineIndex = Math.min(lines.length, errorEndLine + 1); // +1 表示向后显示1行上下文

        for (int i = startLineIndex; i < endLineIndex; i++) {
            String line = lines[i];
            int currentLineNum = i + 1;
            sb.append(line).append("\n");

            if (currentLineNum >= errorStartLine && currentLineNum <= errorEndLine) {
                int lineLength = line.length();

                int startCol = (currentLineNum == errorStartLine) ? errorStartColumn : 1;
                int endCol = (currentLineNum == errorEndLine) ? errorEndColumn : lineLength;

                int spaces = Math.max(0, startCol - 1);
                spaces = Math.min(spaces, lineLength);

                int length;
                if (currentLineNum == errorStartLine && currentLineNum == errorEndLine) {
                    length = endCol - startCol + 1;
                } else if (currentLineNum == errorStartLine) {
                    length = lineLength - startCol + 1;
                } else if (currentLineNum == errorEndLine) {
                    length = endCol;
                } else {
                    length = lineLength;
                }

                length = Math.max(1, length);
                if (lineLength > 0) {
                    length = Math.min(length, lineLength - spaces);
                }

                if (length > 0 && spaces + length <= lineLength + 1) { // Allow 1 char overflow for empty lines or end
                    // of line
                    // Actually, repeat throws if count < 0.
                    // And we want to avoid printing beyond line if possible, but for empty line
                    // maybe we want a caret?
                    // If line is empty, spaces=0, length=1. " " * 0 + "^" * 1 = "^".
                    // If line is "abc", spaces=3, length=1. " ^".

                    String indicator = " ".repeat(spaces) + "^".repeat(length);
                    sb.append(indicator).append("\n");
                }
            }
        }

        return sb.toString().trim();
    }

    public enum Type {
        EVALUATE_ERROR,
        PARSER_ERROR,
        SUCCESS,
    }
}