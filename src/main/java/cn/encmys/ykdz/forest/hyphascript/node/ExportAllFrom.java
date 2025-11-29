package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExportAllFrom extends ASTNode {
    private @Nullable
    final String as;
    private @NotNull
    final String from;

    public ExportAllFrom(@Nullable String as, @NotNull String from, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.as = as;
        this.from = from;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        final @NotNull String objectName = as == null ? "__USED_BY_EXPORT_ALL_FROM__" : as;

        final ImportAllAs importNode = new ImportAllAs(objectName, from, getStartToken(), getEndToken());
        importNode.evaluate(ctx);

        final Reference ref = ctx.findMember(objectName);

        if (as != null) {
            ctx.putExportedMember(as, ctx.findMember(objectName));
        } else {
            final ScriptObject object = ref.getReferredValue().getAsScriptObject();
            ctx.putAllExportedMembers(object.getLocalMembers());
        }

        ctx.deleteMember(objectName);

        return new Reference();
    }
}
