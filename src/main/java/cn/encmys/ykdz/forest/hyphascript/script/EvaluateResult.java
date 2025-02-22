package cn.encmys.ykdz.forest.hyphascript.script;

import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public record EvaluateResult(@NotNull Type type, @NotNull String script, @NotNull Value value, @NotNull String errorMsg, int errorLine, int errorColumn) {
    public enum Type {
        EVALUATE_ERROR,
        PARSER_ERROR,
        SUCCESS,
    }

    @Override
    public String toString() {
        if (type == Type.SUCCESS) {
            return "";
        }

        String[] lines = script.split("\\R"); // 兼容不同系统换行符
        StringBuilder sb = new StringBuilder();
        sb.append(errorMsg).append("\n\n");

        int startLine = Math.max(0, errorLine - 3);
        int endLine = Math.min(lines.length, errorLine + 2);

        for (int i = startLine; i < endLine; i++) {
            sb.append(lines[i]).append("\n");
            if (i == errorLine - 1) {
                sb.append(" ".repeat(errorColumn)).append("^\n");
            }
        }

        return sb.toString();
    }
}
