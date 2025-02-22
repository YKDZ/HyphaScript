package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.pack.PackManager;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.script.ScriptManager;
import cn.encmys.ykdz.forest.hyphascript.utils.ReflectionUtils;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class ImportNamespace extends ASTNode {
    @NotNull
    private final String namespace;
    @NotNull
    private final String as;

    public ImportNamespace(@NotNull String namespace, @NotNull String as) {
        this.namespace = namespace;
        this.as = as;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        if (ScriptManager.hasScript(namespace)) importScript(ctx);
        else if (PackManager.hasPack(namespace)) importPack(ctx);
        else importJavaClass(ctx);

        return new Reference();
    }

    private void importScript(@NotNull Context ctx) {
        if (ctx.hasMember(as))
            throw new EvaluateException(this, "Already has member with target name in context");

        Script target = ScriptManager.getScript(namespace);
        Context targetCtx = target.getContext();

        Map<String, Reference> exportedMembers = targetCtx.getExportedMembers();
        // 将导入的 Value 的哈希值和其来源命名空间的映射记录到上下文中
        // 以便在调用导入的函数时替换上下文为导入方上下文
        exportedMembers.values().forEach(ref -> ctx.putImportedMemberOrigin(ref.hashCode(), namespace));
        ctx.declareReference(as, new Value(exportedMembers));
    }

    private void importPack(@NotNull Context ctx) {
        ctx.declareReference(as, new Value(PackManager.getPack(namespace)));
    }

    private void importJavaClass(@NotNull Context ctx) {
        try {
            String targetNamespace = ReflectionUtils.classNameFromPackage(as);
            Class<?> clazz = Class.forName(namespace);
            ctx.addImportedJavaClasses(namespace);
            ctx.declareReference(targetNamespace, new Value(clazz));
        } catch (ClassNotFoundException e) {
            throw new EvaluateException(this, "Error loading java class " + namespace, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportNamespace that = (ImportNamespace) o;
        return Objects.equals(namespace, that.namespace) && Objects.equals(as, that.as);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, as);
    }
}
