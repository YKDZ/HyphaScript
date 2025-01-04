package cn.encmys.ykdz.forest.hypha.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ReflectionUtils {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * 根据包名字符串解析出类名
     * @param fullPath 包名，如 java.lang.String
     * @return 类名，如 String
     */
    public static @NotNull String classNameFromPackage(@NotNull String fullPath) {
        if (fullPath.isEmpty()) {
            throw new IllegalArgumentException("FullPath is empty");
        }

        // 测试环境下也存在没有 . 的包
        int lastDotIndex = fullPath.lastIndexOf('.');
        return fullPath.substring(lastDotIndex + 1);
    }

    public static MethodHandle getMethodHandle(@NotNull Method method) throws IllegalAccessException {
        return LOOKUP.unreflect(method);
    }

    public static MethodHandle @NotNull [] getMethodHandlesByName(@NotNull Class<?> targetClass, @NotNull String methodName) throws Throwable {
        List<MethodHandle> methodHandles = new ArrayList<>();

        Method[] methods = targetClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                methodHandles.add(getMethodHandle(method));
            }
        }

        return methodHandles.toArray(new MethodHandle[0]);
    }

    public static @Nullable MethodHandle selectFirstMatchingConstructor(@NotNull Class<?> targetClass, @NotNull Class<?>[] parameterTypes) throws Throwable {
        Constructor<?>[] constructors = targetClass.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            // 检查参数类型是否匹配
            if (isParametersMatching(constructor.getParameterTypes(), parameterTypes)) {
                // 使用反射来获取构造方法的MethodHandle
                return MethodHandles.lookup().unreflectConstructor(constructor);
            }
        }
        return null; // 没有找到匹配的构造方法
    }

    public static @Nullable MethodHandle selectFirstMatchingMethodHandle(@NotNull MethodHandle[] methodHandles, @NotNull Class<?>[] parameterTypes) {
        Arrays.sort(methodHandles, Comparator.comparingInt(ReflectionUtils::getPriority));

        Class<?>[] typesForStatic = Arrays.copyOfRange(parameterTypes, 1, parameterTypes.length);

        // 排序后按匹配规则选择第一个符合条件的方法
        for (MethodHandle methodHandle : methodHandles) {
            if (isParametersMatching(methodHandle.type().parameterArray(), isStatic(methodHandle) ? typesForStatic : parameterTypes)) {
                return methodHandle;
            }
        }

        return null;
    }

    private static boolean isParametersMatching(@NotNull Class<?>[] constructorParameterTypes, @NotNull Class<?>[] parameterTypes) {
        // 如果参数数量不一致，直接返回false
        if (constructorParameterTypes.length != parameterTypes.length) {
            return false;
        }

        // 遍历每个参数并检查类型是否匹配
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            Class<?> constructorParameterType = constructorParameterTypes[i];

            if (parameterType == BigDecimal.class) {
                // 如果参数类型是BigDecimal，则可以匹配任意数字类型或其包装类
                if (!isNumericType(constructorParameterType)) {
                    return false;
                }
            } else if (!parameterType.isAssignableFrom(constructorParameterType)) {
                // 否则，普通类型必须匹配
                return false;
            }
        }
        return true;
    }

    private static boolean isNumericType(@NotNull Class<?> clazz) {
        return clazz == byte.class || clazz == short.class || clazz == int.class || clazz == long.class || clazz == float.class || clazz == double.class
                || clazz == Byte.class || clazz == Short.class || clazz == Integer.class || clazz == Long.class || clazz == Float.class || clazz == Double.class;
    }

    private static int getPriority(@NotNull MethodHandle methodHandle) {
        Class<?>[] parameterTypes = methodHandle.type().parameterArray();
        int priority = 0;

        for (Class<?> parameterType : parameterTypes) {
            if (parameterType == BigDecimal.class) {
                priority += 3; // BigDecimal最高优先级
            } else if (parameterType == double.class || parameterType == Double.class) {
                priority += 2; // double次高
            } else if (parameterType == float.class || parameterType == Float.class) {
                priority += 1; // float优先级较低
            } else if (parameterType == long.class || parameterType == Long.class) {
                priority += 1; // long
            } else if (parameterType == int.class || parameterType == Integer.class) {
                priority += 0; // int最低
            }
        }

        return priority; // 总优先级根据数字精度计算
    }

    public static Object invokeMethodHandle(@NotNull MethodHandle methodHandle, @NotNull Object[] arguments) throws Throwable {
        if (isStatic(methodHandle) && arguments.length > 1) {
            arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        }
        // 处理参数中的 BigDecimal，将其转换为适当的数字类型
        Object[] transformedArguments = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof BigDecimal) {
                transformedArguments[i] = convertBigDecimal((BigDecimal) arguments[i], methodHandle.type().parameterType(i));
            } else {
                transformedArguments[i] = arguments[i];
            }
        }
        // 使用转换后的参数调用 MethodHandle
        return transformedArguments.length == 0 ? methodHandle.invoke() : methodHandle.invokeWithArguments(transformedArguments);
    }

    private static Object convertBigDecimal(@NotNull BigDecimal bigDecimal, @NotNull Class<?> targetType) {
        if (targetType == byte.class || targetType == Byte.class) {
            return bigDecimal.byteValue();
        } else if (targetType == short.class || targetType == Short.class) {
            return bigDecimal.shortValue();
        } else if (targetType == int.class || targetType == Integer.class) {
            return bigDecimal.intValue();
        } else if (targetType == long.class || targetType == Long.class) {
            return bigDecimal.longValue();
        } else if (targetType == float.class || targetType == Float.class) {
            return bigDecimal.floatValue();
        } else if (targetType == double.class || targetType == Double.class) {
            return bigDecimal.doubleValue();
        } else if (targetType == BigDecimal.class) {
            return bigDecimal;
        }
        throw new IllegalArgumentException("无法将 BigDecimal 转换为目标类型: " + targetType);
    }

    public static boolean isStatic(@NotNull MethodHandle methodHandle) {
        // 获取 MethodHandle 的参数类型
        MethodType methodType = methodHandle.type();

        // 静态方法的参数列表中不会包含隐式的 this 参数
        // 如果 MethodHandle 是绑定到实例的，则不是静态方法
        return !methodHandle.isVarargsCollector() && methodType.parameterCount() == 0 ||
                !methodType.parameterType(0).isAssignableFrom(methodHandle.type().parameterType(0));
    }
}