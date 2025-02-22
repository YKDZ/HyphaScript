package cn.encmys.ykdz.forest.hyphascript.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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

        int lastDotIndex = fullPath.lastIndexOf('.');
        return fullPath.substring(lastDotIndex + 1);
    }

    public static MethodHandle getMethodHandle(@NotNull Method method) throws IllegalAccessException {
        return LOOKUP.unreflect(method);
    }

    public static MethodHandle @NotNull [] getMethodHandlesByName(@NotNull Class<?> targetClass, @NotNull String methodName) {
        List<MethodHandle> methodHandles = new ArrayList<>();

        Method[] methods = targetClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                try {
                    methodHandles.add(getMethodHandle(method));
                } catch (IllegalAccessException ignored) {
                }
            }
        }

        return methodHandles.toArray(new MethodHandle[0]);
    }

    public static @Nullable MethodHandle selectFirstMatchingConstructor(
            @NotNull Class<?> targetClass,
            @Nullable Object @NotNull [] evaluatedArgs
    ) throws Throwable {
        Constructor<?>[] constructors = targetClass.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            // 若为静态方法，则 evaluatedArgs 的第一个元素永远是类本身
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length != evaluatedArgs.length) {
                continue;
            }

            boolean isCompatible = true;
            for (int i = 0; i < evaluatedArgs.length; i++) {
                Object arg = evaluatedArgs[i];
                Class<?> paramType = paramTypes[i];

                if (arg == null) {
                    if (paramType.isPrimitive()) {
                        isCompatible = false;
                        break;
                    }
                } else {
                    Class<?> argType = arg.getClass();
                    if (argType == BigDecimal.class) {
                        if (!isNumericType(paramType)) {
                            isCompatible = false;
                            break;
                        }
                    } else {
                        if (paramType.isPrimitive()) {
                            Class<?> boxedType = getBoxedType(paramType);
                            if (boxedType == null || !boxedType.isAssignableFrom(argType)) {
                                isCompatible = false;
                                break;
                            }
                        } else if (!paramType.isAssignableFrom(argType)) {
                            isCompatible = false;
                            break;
                        }
                    }
                }
            }
            if (isCompatible) {
                return MethodHandles.lookup().unreflectConstructor(constructor);
            }
        }
        return null;
    }

    public static @Nullable MethodHandle selectFirstMatchingMethodHandle(
            @NotNull MethodHandle @NotNull [] methodHandles,
            @Nullable Object @NotNull [] evaluatedArgs
    ) {
        for (MethodHandle handle : methodHandles) {
            // 若为静态方法，则 evaluatedArgs 的第一个元素永远是类本身
            boolean isStatic = isStatic(handle);
            MethodType methodType = handle.type();
            if (methodType.parameterCount() != evaluatedArgs.length - (isStatic ? 1 : 0)) {
                continue;
            }

            boolean isCompatible = true;
            for (int i = isStatic ? 1 : 0; i < evaluatedArgs.length; i++) {
                Object arg = evaluatedArgs[i];
                // 若为静态方法，则 i 从 1 开始，但 parameterType 数组索引从 0 开始，所以要 - 1
                Class<?> paramType = methodType.parameterType(i - (isStatic ? 1 : 0));

                if (arg == null) {
                    if (paramType.isPrimitive()) {
                        isCompatible = false;
                        break;
                    }
                } else {
                    Class<?> argType = arg.getClass();
                    if (argType == BigDecimal.class) {
                        if (!isNumericType(paramType)) {
                            isCompatible = false;
                            break;
                        }
                    } else {
                        if (paramType.isPrimitive()) {
                            Class<?> boxedType = getBoxedType(paramType);
                            if (boxedType == null || !boxedType.isAssignableFrom(argType)) {
                                isCompatible = false;
                                break;
                            }
                        } else if (!paramType.isAssignableFrom(argType)) {
                            isCompatible = false;
                            break;
                        }
                    }
                }
            }
            if (isCompatible) {
                return handle;
            }
        }
        return null;
    }

    @Contract(pure = true)
    private static @Nullable Class<?> getBoxedType(Class<?> primitiveType) {
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == char.class) return Character.class;
        if (primitiveType == short.class) return Short.class;
        if (primitiveType == byte.class) return Byte.class;
        return null;
    }

    private static boolean isNumericType(@NotNull Class<?> type) {
        return type == byte.class || type == Byte.class ||
                type == short.class || type == Short.class ||
                type == int.class || type == Integer.class ||
                type == long.class || type == Long.class ||
                type == float.class || type == Float.class ||
                type == double.class || type == Double.class;
    }

    public static Object invokeMethodHandle(@NotNull MethodHandle methodHandle, @NotNull Object[] arguments) throws Throwable {
        if (isStatic(methodHandle) && arguments.length > 0) {
            arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        }

        Object[] transformedArguments = new Object[arguments.length];
        MethodType methodType = methodHandle.type();
        for (int i = 0; i < arguments.length; i++) {
            Object arg = arguments[i];
            if (arg instanceof BigDecimal) {
                Class<?> targetType = methodType.parameterType(i);
                transformedArguments[i] = convertBigDecimal((BigDecimal) arg, targetType);
            } else {
                transformedArguments[i] = arg;
            }
        }

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

    public static boolean isStatic(@NotNull MethodHandle handler) {
        MethodHandleInfo info = MethodHandles.lookup().revealDirect(handler);
        return Modifier.isStatic(info.getModifiers());
    }
}