package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.BreakNotificationException;
import cn.encmys.ykdz.forest.hyphascript.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.ScriptArray;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ForInLoop extends ASTNode {
    @NotNull
    private final ASTNode variable;
    @NotNull
    private final ASTNode target;
    @NotNull
    private final ASTNode body;

    public ForInLoop(@NotNull ASTNode variable, @NotNull ASTNode target, @NotNull ASTNode body,
            @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.variable = variable;
        this.target = target;
        this.body = body;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value targetValue = target.evaluate(ctx).getReferredValue();
        List<Value> keys = new ArrayList<>();

        if (targetValue.isType(Value.Type.ARRAY)) {
            ScriptArray array = targetValue.getAsArray();
            for (Integer key : array.keySet()) {
                keys.add(new Value(key));
            }
        } else if (targetValue.isType(Value.Type.SCRIPT_OBJECT) || targetValue.isType(Value.Type.FUNCTION)) {
            ScriptObject obj = targetValue.getAsScriptObject();
            for (String key : obj.getMemberKeys()) {
                keys.add(new Value(key));
            }
        } else {
            throw new EvaluateException(this, "Target of for-in loop must be an array or object.");
        }

        Context localContext = new Context(ctx);
        String varName;
        boolean isDeclaration = false;

        if (variable instanceof Identifier) {
            varName = ((Identifier) variable).getName();
        } else if (variable instanceof Let) {
            varName = ((Let) variable).getName();
            isDeclaration = true;
        } else {
            throw new EvaluateException(this, "For-in loop variable must be an identifier or a let declaration.");
        }

        for (Value key : keys) {
            Reference keyRef = new Reference(key);

            if (isDeclaration) {
                localContext.declareMember(varName, keyRef);
            } else {
                try {
                    localContext.putMember(varName, keyRef);
                } catch (Exception e) {
                    throw new EvaluateException(this, "Variable " + varName + " is not defined.", e);
                }
            }

            try {
                body.evaluate(localContext);
            } catch (BreakNotificationException ignored) {
                break;
            } catch (ContinueNotificationException ignored) {
            }
        }

        return new Reference();
    }

    @Override
    public String toString() {
        return "ForInLoop{" +
                "variable=" + variable +
                ", target=" + target +
                ", body=" + body +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ForInLoop forLoop = (ForInLoop) o;
        return Objects.equals(variable, forLoop.variable) &&
                Objects.equals(target, forLoop.target) &&
                Objects.equals(body, forLoop.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, target, body);
    }
}
