package cn.encmys.ykdz.forest.hyphascript.utils;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class ContextUtils {
    public static @NotNull Context linkContext(@NotNull Context... contexts) {
        if (contexts == null || contexts.length < 2) {
            throw new IllegalArgumentException("At least two Context is required.");
        }

        for (int i = 1; i < contexts.length; i++) {
            contexts[i].setParent(contexts[i - 1]);
        }

        return contexts[contexts.length - 1];
    }

    /**
     * 泛型安全获取
     *
     * @param ctx        上下文对象
     * @param memberName 成员名称
     * @param type       目标类型（带通配符）
     * @return 正确泛型类型的 Optional
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getMember(@NotNull Context ctx, @NotNull String memberName, @NotNull Class<?> type) {
        try {
            return Optional.of(ctx.findMember(memberName))
                    .map(Reference::getReferredValue)
                    .map(Value::value)
                    .filter(type::isInstance)
                    .map(v -> (T) v);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 安全获取整型参数值
     *
     * @param ctx       上下文对象
     * @param paramName 参数名称
     * @return 包装为 OptionalInt 的整数值
     */
    public static @NotNull OptionalInt getIntParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return OptionalInt.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsBigDecimal()
                            .intValue()
            );
        } catch (Exception e) {
            return OptionalInt.empty();
        }
    }

    /**
     * 安全获取整型参数值
     *
     * @param ctx       上下文对象
     * @param paramName 参数名称
     * @return 包装为 OptionalInt 的整数值
     */
    public static @NotNull Optional<Boolean> getBooleanParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsBoolean()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static @NotNull OptionalDouble getDoubleParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return OptionalDouble.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsBigDecimal()
                            .doubleValue()
            );
        } catch (Exception e) {
            return OptionalDouble.empty();
        }
    }

    public static @NotNull Optional<Float> getFloatParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsBigDecimal()
                            .floatValue()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<String> getStringParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsString()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<Component> getComponentParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsAdventureComponent()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<Character> getCharacterParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsChar()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static @NotNull Optional<ScriptObject> getScriptObjectParam(@NotNull Context ctx, @NotNull String paramName) {
        try {
            return Optional.of(
                    ctx.findMember(paramName)
                            .getReferredValue()
                            .getAsScriptObject()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 安全获取玩家对象
     *
     * @param ctx 上下文对象
     * @return 包装为 Optional 的 Player 对象
     */
    public static @NotNull Optional<Player> getPlayer(@NotNull Context ctx) {
        return getMember(ctx, "__player", Player.class);
    }
}
