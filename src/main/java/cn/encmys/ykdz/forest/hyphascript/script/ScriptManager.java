package cn.encmys.ykdz.forest.hyphascript.script;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ScriptManager {
    private final static Map<String, Script> scripts = new HashMap<>();

    private ScriptManager() {}

    /**
     * Registered a script with global context
     * @see Context#GLOBAL_CONTEXT
     * @param namespace Namespace that the script will be registered in
     * @param scriptStr Content of the script
     * @return Created Script
     */
    @NotNull
    public static Script createScript(@NotNull String namespace, @NotNull String scriptStr) {
        if (scripts.containsKey(namespace)) throw new IllegalArgumentException("Script with namespace " + namespace + " already exists");
        Script script = new Script(scriptStr, new Context());
        scripts.put(namespace, script);
        return script;
    }

    /**
     * Registered a script with given context
     * @param namespace ScriptNamespace that the script will be registered in
     * @param scriptStr Content of the script
     * @param context Context that will be used
     * @return Created Script
     */
    @NotNull
    public static Script createScript(@NotNull String namespace, @NotNull String scriptStr, @NotNull Context context) {
        if (scripts.containsKey(namespace)) throw new IllegalArgumentException("Script with namespace " + namespace + " already exists");
        Script script = new Script(scriptStr, context);
        scripts.put(namespace, script);
        return script;
    }

    @NotNull
    public static Script getScript(@NotNull String namespace) {
        Script script = scripts.get(namespace);
        if (!script.isEvaluated()) script.evaluate();
        return script;
    }

    @Nullable
    public static Script putScript(@NotNull String namespace, @NotNull Script script) {
        return scripts.put(namespace, script);
    }

    public static boolean hasScript(@NotNull String namespace) {
        return scripts.containsKey(namespace);
    }
}
