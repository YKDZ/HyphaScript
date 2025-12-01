package cn.encmys.ykdz.forest.hyphascript.oop.internal;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.core.*;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class InternalObjectManager {
    /**
     * Global object used to register some common utils function
     * or variable to the script.
     */
    @NotNull
    public final static Context GLOBAL_OBJECT = new Context(InternalObjectManager.OBJECT_PROTOTYPE);

    public static final @NotNull ScriptObject OBJECT;
    public static final @NotNull ScriptObject OBJECT_PROTOTYPE;
    public static final @NotNull ScriptObject FUNCTION;
    public static final @NotNull ScriptObject FUNCTION_PROTOTYPE;
    public static final @NotNull ScriptObject ARRAY;
    public static final @NotNull ScriptObject ARRAY_PROTOTYPE;
    public static final @NotNull ScriptObject NUMBER;
    public static final @NotNull ScriptObject NUMBER_PROTOTYPE;
    public static final @NotNull ScriptObject FUTURE;

    private static final @NotNull Map<@NotNull String, @NotNull ScriptObject> objects = new HashMap<>();

    static {
        OBJECT = registerWithPrototype("Object", new ObjectObject());
        OBJECT_PROTOTYPE = getPrototype(OBJECT);

        FUNCTION = registerWithPrototype("Function", new FunctionObject());
        FUNCTION_PROTOTYPE = getPrototype(FUNCTION);

        ARRAY = registerWithPrototype("Array", new ArrayObject());
        ARRAY_PROTOTYPE = getPrototype(ARRAY);

        NUMBER = registerWithPrototype("Number", new NumberObject());
        NUMBER_PROTOTYPE = getPrototype(NUMBER);

        FUTURE = register("Future", new FutureObject());

        registerWithPrototype("Random", new RandomObject());
        registerWithPrototype("Command", new CommandObject());
        registerWithPrototype("Player", new PlayerObject());
        registerWithPrototype("console", new ConsoleObject());
        registerWithPrototype("Server", new ServerObject());
        registerWithPrototype("Math", new MathObject());
        registerWithPrototype("PlaceholderAPI", new PlaceholderAPIObject());
        registerWithPrototype("Date", new DateObject());
        registerWithPrototype("MiniMessage", new MiniMessageObject());

        objects.forEach((key, value) -> GLOBAL_OBJECT.declareMember(
                key, new Reference(new Value(value), true)
        ));
    }

    private InternalObjectManager() {
    }

    public static @NotNull ScriptObject register(@NotNull String name, @NotNull InternalObject obj) {
        ScriptObject so = obj.getAsScriptObject();
        objects.put(name, so);
        return so;
    }

    private static @NotNull ScriptObject registerWithPrototype(@NotNull String name, @NotNull InternalObject obj) {
        return register(name, obj);
    }

    private static @NotNull ScriptObject getPrototype(@NotNull ScriptObject object) {
        return object.findMember("prototype").getReferredValue().getAsScriptObject();
    }

    public static boolean hasObject(@NotNull String name) {
        return objects.containsKey(name);
    }

    public static @NotNull ScriptObject getObject(@NotNull String name) {
        return objects.get(name);
    }
}
