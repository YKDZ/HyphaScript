package cn.encmys.ykdz.forest.hyphascript.pack;

import cn.encmys.ykdz.forest.hyphascript.annotions.Function;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionParas;
import cn.encmys.ykdz.forest.hyphascript.annotions.FunctionReceivers;
import cn.encmys.ykdz.forest.hyphascript.annotions.PackNamespace;
import cn.encmys.ykdz.forest.hyphascript.function.PackFunction;
import cn.encmys.ykdz.forest.hyphascript.utils.ReflectionUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Modifier;
import java.util.*;

public class PackManager {
    private static final @NotNull Map<String, Map<String, Reference>> packs = new HashMap<>();

    private PackManager() {}

    public static void registerPack(@NotNull HyphaPack pack) {
        String namespace = pack.getClass().getAnnotation(PackNamespace.class).value();

        Map<String, Reference> packMap = new HashMap<>();

        Arrays.stream(pack.getClass().getDeclaredMethods())
                // 提取所有包含 @Function 注解的方法
                .filter(method -> method.getDeclaredAnnotation(Function.class) != null)
                .forEach(method -> {
                    if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers()))
                        throw new IllegalArgumentException("Pack method must be public static.");

                    String memberName = method.getName();
                    List<String> parameters = Arrays.stream(method.getAnnotation(FunctionParas.class).value()).toList();
                    List<String> receivers = Arrays.stream(method.getAnnotation(FunctionReceivers.class).value()).toList();
                    try {
                        packMap.put(memberName, new Reference(memberName, new Value(new PackFunction(receivers, parameters, ReflectionUtils.getMethodHandle(method))), true, true));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });

        packs.put(namespace, packMap);
    }

    public static boolean hasPack(@NotNull String namespace) {
        return packs.containsKey(namespace);
    }

    public static Map<String, Reference> getPack(@NotNull String namespace) {
        return packs.get(namespace);
    }
}
