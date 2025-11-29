package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ExportObject extends ASTNode {
    @NotNull
    private final Map<String, String> exported;

    public ExportObject(@NotNull Map<String, String> exported, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.exported = exported;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        exported.forEach((name, as) -> {
            if (!ctx.hasMember(name)) throw new EvaluateException(this, "Exported member not exists");
            ctx.setExported(name, as);
        });
        return new Reference();
    }
}
