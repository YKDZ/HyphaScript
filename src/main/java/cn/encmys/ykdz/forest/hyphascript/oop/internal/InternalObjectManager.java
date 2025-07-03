package cn.encmys.ykdz.forest.hyphascript.oop.internal;

import cn.encmys.ykdz.forest.hyphascript.annotions.*;
import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.function.InternalObjectFunction;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Literal;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.core.*;
import cn.encmys.ykdz.forest.hyphascript.utils.ReflectionUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class InternalObjectManager {
    public static final @NotNull ScriptObject OBJECT;
    public static final @NotNull ScriptObject OBJECT_PROTOTYPE;
    public static final @NotNull ScriptObject FUNCTION;
    public static final @NotNull ScriptObject FUNCTION_PROTOTYPE;
    public static final @NotNull ScriptObject ARRAY;
    public static final @NotNull ScriptObject ARRAY_PROTOTYPE;
    public static final @NotNull ScriptObject NUMBER;
    public static final @NotNull ScriptObject RANDOM;
    public static final @NotNull ScriptObject MATH;
    public static final @NotNull ScriptObject NUMBER_PROTOTYPE;
    public static final @NotNull ScriptObject FUTURE;

    private static final @NotNull Map<String, ScriptObject> objects = new HashMap<>();

    static {
        OBJECT = registerObject(new ObjectObject());
        OBJECT_PROTOTYPE = OBJECT.findMember("prototype").getReferredValue().getAsScriptObject();
        FUNCTION = registerObject(new FunctionObject());
        FUNCTION_PROTOTYPE = FUNCTION.findMember("prototype").getReferredValue().getAsScriptObject();
        ARRAY = registerObject(new ArrayObject());
        ARRAY_PROTOTYPE = ARRAY.findMember("prototype").getReferredValue().getAsScriptObject();
        FUTURE = registerObject(new FutureObject());
        NUMBER = registerObject(new NumberObject());
        NUMBER_PROTOTYPE = NUMBER.findMember("prototype").getReferredValue().getAsScriptObject();
        RANDOM = registerObject(new RandomObject());
        MATH = registerObject(new MathObject());

        // 注册无需调用即可使用的全局对象
        Context.GLOBAL_OBJECT.declareMember("Object", new Reference(new Value(OBJECT), true));
        Context.GLOBAL_OBJECT.declareMember("Function", new Reference(new Value(FUNCTION), true));
        Context.GLOBAL_OBJECT.declareMember("Array", new Reference(new Value(ARRAY), true));
        Context.GLOBAL_OBJECT.declareMember("Future", new Reference(new Value(FUTURE), true));
        Context.GLOBAL_OBJECT.declareMember("Number", new Reference(new Value(NUMBER), true));
        Context.GLOBAL_OBJECT.declareMember("Random", new Reference(new Value(RANDOM), true));
        Context.GLOBAL_OBJECT.declareMember("Math", new Reference(new Value(MATH), true));
    }

    private InternalObjectManager() {
    }

    public static @NotNull ScriptObject registerObject(@NotNull InternalObject object) {
        return registerObject(object, OBJECT_PROTOTYPE);
    }

    public static @NotNull ScriptObject registerObject(@NotNull InternalObject object, @NotNull ScriptObject __proto__) {
        ObjectName nameAnno = object.getClass().getAnnotation(ObjectName.class);
        if (nameAnno == null)
            throw new IllegalArgumentException("Internal object class must have ObjectName annotation.");

        String name = nameAnno.value();

        ScriptObject objectPrototype = new ScriptObject(__proto__);

        Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(method -> method.getDeclaredAnnotation(Function.class) != null)
                .forEach(method -> {
                    if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
                        throw new IllegalArgumentException("Internal object method must be public static.");
                    }

                    Function functionAnnotation = method.getDeclaredAnnotation(Function.class);
                    String functionName = functionAnnotation.value().isEmpty() ? method.getName() : functionAnnotation.value();

                    FunctionParas parasAnnotation = method.getDeclaredAnnotation(FunctionParas.class);

                    LinkedHashMap<String, ASTNode> parameters = new LinkedHashMap<>();
                    if (parasAnnotation != null) {
                        for (String s : parasAnnotation.value()) {
                            parameters.put(s, new Literal(new Value(null)));
                        }
                    }

                    FunctionUncertainPara uncertainParaAnno = method.getDeclaredAnnotation(FunctionUncertainPara.class);
                    String uncertainParameter = uncertainParaAnno != null && uncertainParaAnno.value().isEmpty() ? uncertainParaAnno.value() : "";

                    boolean isStatic = method.isAnnotationPresent(Static.class);

                    InternalObjectFunction func;

                    try {
                        func = new InternalObjectFunction(functionName, parameters, uncertainParameter, ReflectionUtils.getMethodHandle(method));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    if (isStatic) {
                        if (object.hasLocalMember(functionName))
                            throw new RuntimeException("Internal object already has function with the same name: \"" + functionName + "\"");
                        object.declareMember(functionName, new Reference(new Value(func), true));
                        object.setExported(functionName);
                    } else {
                        if (objectPrototype.hasLocalMember(functionName))
                            throw new RuntimeException("Internal object already has function with the same name: \"" + functionName + "\"");
                        objectPrototype.declareMember(functionName, new Reference(new Value(func), true));
                        objectPrototype.setExported(functionName);
                    }
                });

        object.declareMember("prototype", new Value(objectPrototype));

        objects.put(name, object);

        return object;
    }

    public static boolean hasObject(@NotNull String name) {
        return objects.containsKey(name);
    }

    public static ScriptObject getObject(@NotNull String name) {
        return objects.get(name);
    }
}
