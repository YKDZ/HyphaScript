package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.ContextException;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.script.Script;
import cn.encmys.ykdz.forest.hypha.script.ScriptManager;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class ImportObjects extends ASTNode {
    @NotNull
    private final Map<String, String> imported;
    @NotNull
    private final String from;

    public ImportObjects(@NotNull Map<String, String> imported, @NotNull String from) {
        this.imported = imported;
        this.from = from;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Script target = ScriptManager.getScript(from);
        Context targetCtx = target.getContext();
        Map<String, Reference> exported = targetCtx.getExportedMembers();

        for (Map.Entry<String, String> entry : imported.entrySet()) {
            String name = entry.getKey();
            String as = entry.getValue();

            try {
                Reference exportedRef = exported.get(name);
                ctx.putImportedMemberOrigin(exportedRef.hashCode(), from);
                ctx.declareReference(as, exportedRef);
            } catch (ContextException e) {
                throw new ScriptException(this, "Already has member with target name in context");
            }
        }

        return new Reference();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportObjects that = (ImportObjects) o;
        return Objects.equals(imported, that.imported) && Objects.equals(from, that.from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imported, from);
    }
}
