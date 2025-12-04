package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.script.ScriptManager;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ImportAllAs extends ASTNode {
    private final @NotNull String as;
    private final @NotNull String from;

    public ImportAllAs(@NotNull String as, @NotNull String from, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.as = as;
        this.from = from;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        if (InternalObjectManager.hasObject(from))
            importInternalObject(ctx);
        else if (ScriptManager.hasScript(from))
            importScript(ctx);
        else
            throw new EvaluateException(this,
                    "Target namespace \"" + from + "\" is neither a internal object nor a script");
        return new Reference();
    }

    private void importScript(@NotNull Context ctx) {
        final Script target = ScriptManager.getScript(from);
        final Context targetCtx = target.getContext();
        final Map<String, Reference> exported = targetCtx.getExportedMembers();

        final ScriptObject object = new ScriptObject();
        object.declareMember(exported);

        ctx.declareMember(as, new Reference(new Value(object)));
    }

    private void importInternalObject(@NotNull Context ctx) {
        final ScriptObject pack = InternalObjectManager.getObject(from);
        final Map<String, Reference> exported = pack.getExportedMembers();

        final ScriptObject object = new ScriptObject();
        object.declareMember(exported);

        ctx.declareMember(as, new Reference(new Value(object)));
    }
}
