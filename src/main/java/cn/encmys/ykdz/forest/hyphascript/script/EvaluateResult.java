package cn.encmys.ykdz.forest.hyphascript.script;

import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public record EvaluateResult(@NotNull Type type, @NotNull String script, @NotNull Value value, long timeCost,
                             @NotNull String errorMsg, @Nullable Throwable cause,
                             int errorStartLine, int errorStartColumn, int errorEndLine, int errorEndColumn
) {
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
                            .collect(Collectors.joining("\n"))
            );
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
        int errorLineIndex = errorStartLine - 1;
        int startLine = Math.max(0, errorLineIndex - 1);
        int endLine = Math.min(lines.length, errorLineIndex + 2); // +2 因为循环条件是 i < endLine

        for (int i = startLine; i < endLine; i++) {
            sb.append(lines[i]).append("\n");
            if (i == errorLineIndex) {
                int lineLength = lines[i].length();
                // 计算起始空格和 ^ 的长度
                int spaces = Math.min(errorStartColumn - 2, lineLength);
                int caretSpan = errorEndColumn - errorStartColumn + 1;
                // 确保 ^ 不超过行尾
                caretSpan = Math.min(caretSpan, lineLength - spaces);
                caretSpan = Math.max(caretSpan, 1); // 至少显示一个 ^

                String indicator = " ".repeat(spaces) + "^".repeat(caretSpan);
                sb.append(indicator).append("\n");
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