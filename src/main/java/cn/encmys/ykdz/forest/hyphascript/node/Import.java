package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.script.Script;
import cn.encmys.ykdz.forest.hyphascript.script.ScriptManager;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Import extends ASTNode {
    private final @NotNull String target;
    private final @NotNull String alias;
    private final @NotNull String from;

    public Import(@NotNull String target, @NotNull String alias, @NotNull String from, @NotNull Token startToken,
            @NotNull Token endToken) {
        super(startToken, endToken);
        this.target = target;
        this.alias = alias;
        this.from = from;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        if ("java".equals(from)) {
            return importJavaClass(ctx);
        } else {
            return importFromSource(ctx);
        }
    }

    private @NotNull Reference importJavaClass(@NotNull Context ctx) {
        try {
            Class<?> clazz = Class.forName(target);
            ctx.addImportedJavaClasses(target);
            ctx.declareMember(alias, new Value(clazz));
            return new Reference();
        } catch (ClassNotFoundException e) {
            throw new EvaluateException(this, "Error loading java class " + target, e);
        }
    }

    private @NotNull Reference importFromSource(@NotNull Context ctx) {
        // 1. Try Internal Object
        if (InternalObjectManager.hasObject(from)) {
            ScriptObject pack = InternalObjectManager.getObject(from);
            Reference memberRef = pack.findLocalMemberOrCreateOne(target);
            if (memberRef.getReferredValue().isType(Value.Type.NULL)) {
                throw new EvaluateException(this,
                        "Internal object \"" + from + "\" does not have member \"" + target + "\"");
            }
            ctx.declareMember(alias, memberRef);
            return new Reference();
        }

        // 2. Try Script
        if (ScriptManager.hasScript(from)) {
            Script script = ScriptManager.getScript(from);
            Context scriptCtx = script.getContext();
            Map<String, Reference> exported = scriptCtx.getExportedMembers();
            // We need to find the member 'target' in exported members
            if (exported.containsKey(target)) {
                ctx.declareMember(alias, exported.get(target));
                return new Reference();
            } else {
                throw new EvaluateException(this, "Script \"" + from + "\" does not export member \"" + target + "\"");
            }
        }

        throw new EvaluateException(this, "Cannot resolve import source: " + from);
    }
}
