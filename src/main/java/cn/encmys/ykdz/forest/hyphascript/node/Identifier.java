package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public class Identifier extends ASTNode {
    private final @NotNull String name;

    public Identifier(@NotNull String name, @NotNull Token token) {
        super(token, token);
        this.name = name;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        if (name.equals("this") && !ctx.hasMember("this")) return new Reference(new Value(ctx));
        return ctx.findMember(name);
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "name='" + name + '\'' +
                '}';
    }
}
