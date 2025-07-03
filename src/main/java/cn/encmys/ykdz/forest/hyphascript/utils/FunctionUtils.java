package cn.encmys.ykdz.forest.hyphascript.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FunctionUtils {
    public static void injectArguments(@NotNull Context ctx, @NotNull LinkedHashMap<@NotNull String, @NotNull ASTNode> definition, @NotNull List<@NotNull Value> arguments, @NotNull String uncertainParameter) {
        int index = 0;
        for (Map.Entry<String, ASTNode> entry : definition.entrySet()) {
            final String argName = entry.getKey();

            if (index > arguments.size() - 1) {
                // 不对上下文有的外部变量进行覆盖
                if (!ctx.hasMember(argName)) ctx.declareMember(argName, entry.getValue().evaluate(ctx));
            } else {
                ctx.declareMember(argName, arguments.get(index));
            }

            index++;
        }
        // 将多余参数放入不定参数数组
        if (!uncertainParameter.isEmpty()) {
            ctx.declareMember(uncertainParameter, new Value(arguments.subList(index, arguments.size()).stream()
                    .map(Reference::new)
                    .toArray(Reference[]::new)));
        }
    }

    public static void injectArguments(@NotNull Context ctx, @NotNull LinkedHashMap<@NotNull String, @NotNull ASTNode> definition, @NotNull Map<@NotNull String, @NotNull Value> arguments, @NotNull String uncertainParameter) {
        for (Map.Entry<String, ASTNode> entry : definition.entrySet()) {
            final String argName = entry.getKey();

            if (!arguments.containsKey(argName)) {
                // 不对上下文有的外部变量进行覆盖
                if (!ctx.hasMember(argName)) ctx.declareMember(argName, entry.getValue().evaluate(ctx));
            } else {
                ctx.declareMember(argName, arguments.get(argName));
            }
        }
        // 将多余参数放入不定参数数组
        if (!uncertainParameter.isEmpty()) {
            ctx.declareMember(uncertainParameter, new Value(arguments.entrySet().stream()
                    .filter(entry -> !definition.containsKey(entry.getKey()))
                    .map(entry -> new Reference(entry.getValue()))
                    .toArray(Reference[]::new)));
        }
    }
}
