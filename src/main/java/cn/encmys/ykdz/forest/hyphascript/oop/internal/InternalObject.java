package cn.encmys.ykdz.forest.hyphascript.oop.internal;

import cn.encmys.ykdz.forest.hyphascript.annotions.*;
import cn.encmys.ykdz.forest.hyphascript.function.InternalObjectFunction;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.Literal;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.utils.ReflectionUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class InternalObject {
    public InternalObject() {
        super();
    }

    public @NotNull String getName() {
        ObjectName nameAnno = this.getClass().getAnnotation(ObjectName.class);
        if (nameAnno == null)
            throw new IllegalArgumentException("Internal object class must have ObjectName annotation.");

        return nameAnno.value();
    }

    public @NotNull ScriptObject getAsScriptObject() {
        ScriptObject objectPrototype = new ScriptObject();
        ScriptObject object = new ScriptObject(objectPrototype);

        Arrays.stream(this.getClass().getDeclaredMethods())
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

        return object;
    }
}
