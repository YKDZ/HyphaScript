package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ExportFrom extends ASTNode {
    private final @NotNull Map<@NotNull String, @NotNull String> exported;
    private @NotNull
    final String from;

    public ExportFrom(@NotNull Map<String, String> exported, @NotNull String from, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.exported = exported;
        this.from = from;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        final ImportObjects importNode = new ImportObjects(exported, from, getStartToken(), getEndToken());
        importNode.evaluate(ctx);

        exported.forEach((name, as) -> {
            ctx.setExported(name, as);
            ctx.deleteMember(as);
        });

        return new Reference();
    }
}
