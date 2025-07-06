package cn.encmys.ykdz.forest.hyphascript.script;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.utils.FileUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ScriptManager {
    private final static @NotNull Map<String, Script> scripts = new HashMap<>();
    private static final @NotNull Map<@NotNull String, @NotNull List<String>> memberGlobalScriptImported = new HashMap<>();

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

    public static void removeScript(@NotNull String namespace) {
        scripts.remove(namespace);
    }

    public static void loadAllFrom(@NotNull Path folder) throws IOException {
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            if (!folder.toFile().mkdirs()) {
                throw new RuntimeException("Error when creating scripts folder.");
            }
        }

        try (Stream<Path> paths = Files.walk(folder)) {
            paths.filter(path -> Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".hps"))
                    .forEach(path -> {
                        try {
                            loadScript(path, path.startsWith(folder + "/.global"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    public static void loadScript(@NotNull Path scriptFile, boolean isGlobal) throws IOException {
        loadScript(scriptFile.toFile(), isGlobal);
    }

    public static void loadScript(@NotNull File scriptFile, boolean isGlobal) throws IOException {
        final String content = FileUtils.readFile(scriptFile);

        final Context configContext = new Context();
        if (!checkIfValid(extractConfigAndImportsFromScript(content), configContext)) return;

        // 检查 namespace
        final String id = configContext.findMemberWithPathSafely("Config.namespace", String.class).orElse("");
        final String namespace = !id.isEmpty() ? id : scriptFile.getName().replaceFirst("\\.[^.]+$", "");

        // 已满足所有条件
        final Script registeredScript = new Script(content);
        final EvaluateResult result = registeredScript.evaluate(new Context(null));
        if (result.type() != EvaluateResult.Type.SUCCESS) {
            throw new RuntimeException("Error when evaluate pack: " + scriptFile.getAbsolutePath() + ". Import will not continue. " + result);
        }
        // 注册到脚本管理器
        ScriptManager.putScript(namespace, registeredScript);

        // 检查是否是全局脚本（覆盖 .global 路径的设置）
        final Value isGlobalValue = configContext.findMemberWithPathSafely("Config.isGlobal").orElse(new Value(null));
        if (isGlobalValue.isType(Value.Type.BOOLEAN))
            isGlobal = isGlobalValue.getAsBoolean();

        // 将全局脚本中的导出成员加载到根上下文中
        // 相当于隐式的 import 语句
        // isUnpackImport 为 false 时
        // 导出会被加载到根上下文里一个与命名空间同名的对象中
        // 否则直接作为根上下文的成员存在
        if (isGlobal) {
            memberGlobalScriptImported.putIfAbsent(namespace, new ArrayList<>());

            // 检查是否解包导入
            final Value isUnpackImportValue = configContext.findMemberWithPathSafely("Config.isUnpack").orElse(new Value(null));
            final boolean isUnpackImport = isUnpackImportValue.isType(Value.Type.BOOLEAN) && isUnpackImportValue.getAsBoolean();

            if (isUnpackImport) {
                registeredScript.getContext().getExportedMembers().forEach((name, reference) -> {
                    if (name.equals("Config")) return;
                    memberGlobalScriptImported.get(namespace).add(name);
                    Context.GLOBAL_OBJECT.declareMember(name, reference.getReferredValue());
                });
            } else {
                memberGlobalScriptImported.get(namespace).add(namespace);
                Context.GLOBAL_OBJECT.declareMember(namespace, new Value(registeredScript.getContext().getExportedMembers()));
            }
        }
    }

    public static void unloadAllScripts() {
        List<String> namespaces = new ArrayList<>(scripts.keySet());
        namespaces.forEach(ScriptManager::unloadScript);
    }


    public static void unloadScript(@NotNull String namespace) {
        final List<String> members = memberGlobalScriptImported.get(namespace);
        if (members == null) return;
        members.forEach(Context.GLOBAL_OBJECT::deleteMember);
        ScriptManager.removeScript(namespace);
        memberGlobalScriptImported.remove(namespace);
    }

    private static boolean checkIfValid(@NotNull String config, @NotNull Context context) {
        // 仅处理脚本的 Config 和 import 部分
        // 也有提前加载被导入的包的作用
        final Script script = new Script(config);
        final EvaluateResult result = script.evaluate(context);

        return result.type() == EvaluateResult.Type.SUCCESS;
    }

    private static @NotNull String extractConfigAndImportsFromScript(@NotNull String scriptContent) {
        final StringBuilder result = new StringBuilder();

        final String configRegex = "export\\s+const\\s+Config\\s*=\\s*\\{.*?};";
        final Pattern configPattern = Pattern.compile(configRegex, Pattern.DOTALL);
        final Matcher configMatcher = configPattern.matcher(scriptContent);

        if (configMatcher.find()) {
            result.append(configMatcher.group()).append("\n");
        }

        final String importRegex = "import\\s+.*?;";
        final Pattern importPattern = Pattern.compile(importRegex, Pattern.DOTALL);
        final Matcher importMatcher = importPattern.matcher(scriptContent);

        while (importMatcher.find()) {
            result.append(importMatcher.group()).append("\n");
        }

        return result.toString().trim();
    }
}
