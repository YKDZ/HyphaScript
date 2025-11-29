package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class LiteralScriptObject extends ASTNode {
    @NotNull
    private final Map<String, ASTNode> objects;

    public LiteralScriptObject(@NotNull Map<String, ASTNode> objects, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.objects = objects;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        ScriptObject object = InternalObjectManager.OBJECT.newInstance();
        objects.forEach((key, value) -> {
            Reference ref = value.evaluate(ctx);
            // 维护用于查找父类（构造函数）的 __home_object__ 属性
            // 字面量对象中对象和函数成员的 __home_object__ 指向对象本身
            try {
                ref.getReferredValue().getAsScriptObject().declareMember("__home_object__", new Value(object));
            } catch (Exception ignored) {
            }
            object.declareMember(key, ref);
        });
        return new Reference(new Value(object));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiteralScriptObject that = (LiteralScriptObject) o;
        return Objects.equals(objects, that.objects);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(objects);
    }
}
