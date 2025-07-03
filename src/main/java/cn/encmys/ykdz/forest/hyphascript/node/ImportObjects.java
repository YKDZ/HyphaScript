package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.exception.ScriptObjectException;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.script.ScriptManager;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class ImportObjects extends ASTNode {
    @NotNull
    private final Map<String, String> imported;
    @NotNull
    private final String from;

    public ImportObjects(@NotNull Map<String, String> imported, @NotNull String from, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.imported = imported;
        this.from = from;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        if (ScriptManager.hasScript(from)) importScript(ctx);
        else if (InternalObjectManager.hasObject(from)) importInternalObject(ctx);
        else
            throw new EvaluateException(this, "Target namespace \"" + from + "\" is neither a internal object nor a script");

        return new Reference();
    }

    private void importScript(@NotNull Context ctx) {
        Script target = ScriptManager.getScript(from);
        Context targetCtx = target.getContext();
        Map<String, Reference> exported = targetCtx.getExportedMembers();

        for (Map.Entry<String, String> entry : imported.entrySet()) {
            String name = entry.getKey();
            String as = entry.getValue();

            Reference exportedRef = exported.get(name);
            if (exportedRef == null)
                throw new EvaluateException(this, "Imported member \"" + name + "\" does not exist");

            try {
                ctx.declareMember(as, exportedRef);
            } catch (ScriptObjectException e) {
                throw new EvaluateException(this, "Already has member with target name in context");
            }
        }
    }

    private void importInternalObject(@NotNull Context ctx) {
        ScriptObject pack = InternalObjectManager.getObject(from);

        for (Map.Entry<String, String> entry : imported.entrySet()) {
            String name = entry.getKey();
            String as = entry.getValue();

            Reference internalObjectMemberRef = pack.findLocalMemberOrCreateOne(name);

            if (internalObjectMemberRef.getReferredValue().isType(Value.Type.NULL)) {
                throw new EvaluateException(this, "Member \"" + as + "\" imported from \"" + from + "\" does not exist");
            }

            try {
                ctx.declareMember(as, internalObjectMemberRef);
            } catch (ScriptObjectException e) {
                throw new EvaluateException(this, "Already has member with target name: " + as + " in context");
            }
        }
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
