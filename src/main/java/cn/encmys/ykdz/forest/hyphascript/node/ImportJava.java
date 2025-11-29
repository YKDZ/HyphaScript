package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

public class ImportJava extends ASTNode {
    private final @NotNull String classPath;
    private final @NotNull String as;

    public ImportJava(@NotNull String classPath, @NotNull String as, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.classPath = classPath;
        this.as = as;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        try {
            Class<?> clazz = Class.forName(classPath);
            ctx.addImportedJavaClasses(classPath);
            ctx.declareMember(as, new Value(clazz));
        } catch (ClassNotFoundException e) {
            throw new EvaluateException(this, "Error loading java class " + classPath, e);
        }
        return new Reference();
    }
}
