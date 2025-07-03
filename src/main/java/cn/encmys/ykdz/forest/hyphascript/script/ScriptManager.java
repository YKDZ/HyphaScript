package cn.encmys.ykdz.forest.hyphascript.script;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ScriptManager {
    private final static @NotNull Map<String, Script> scripts = new HashMap<>();

    private ScriptManager() {
    }

    /**
     * Registered a script with global context
     *
     * @param namespace Namespace that the script will be registered in
     * @param scriptStr Content of the script
     * @return Created Script
     * @see Context#GLOBAL_OBJECT
     */
    @NotNull
    public static Script createScript(@NotNull String namespace, @NotNull String scriptStr, @NotNull Context context) {
        if (scripts.containsKey(namespace))
            throw new IllegalArgumentException("Script with namespace " + namespace + " already exists");
        Script script = new Script(scriptStr);
        script.evaluate(context);
        scripts.put(namespace, script);
        return script;
    }

    @NotNull
    public static Script getScript(@NotNull String namespace) {
        return scripts.get(namespace);
    }

    @Nullable
    public static Script putScript(@NotNull String namespace, @NotNull Script script) {
        return scripts.put(namespace, script);
    }

    public static boolean hasScript(@NotNull String namespace) {
        return scripts.containsKey(namespace);
    }
}
