package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.BreakNotificationException;
import cn.encmys.ykdz.forest.hyphascript.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.function.Function;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

public class ForOfLoop extends ASTNode {
    @NotNull
    private final ASTNode variable;
    @NotNull
    private final ASTNode target;
    @NotNull
    private final ASTNode body;

    public ForOfLoop(@NotNull ASTNode variable, @NotNull ASTNode target, @NotNull ASTNode body,
            @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.variable = variable;
        this.target = target;
        this.body = body;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value targetValue = target.evaluate(ctx).getReferredValue();

        Reference iteratorMethodRef = MemberAccess.findMemberFromTarget(targetValue, "__iterator__", true, this);
        if (iteratorMethodRef.getReferredValue().getType() != Value.Type.FUNCTION) {
            throw new EvaluateException(this, "Target is not iterable (no __iterator__ method).");
        }

        Function iteratorMethod = iteratorMethodRef.getReferredValue().getAsFunction();
        Value iteratorValue = iteratorMethod.call(targetValue, Collections.emptyList(), ctx).getReferredValue();

        if (iteratorValue.getType() != Value.Type.SCRIPT_OBJECT) {
            throw new EvaluateException(this, "__iterator__ must return an object.");
        }

        ScriptObject iterator = iteratorValue.getAsScriptObject();
        Reference nextMethodRef = iterator.findMember("next");
        if (nextMethodRef.getReferredValue().getType() != Value.Type.FUNCTION) {
            throw new EvaluateException(this, "Iterator must have a next method.");
        }
        Function nextMethod = nextMethodRef.getReferredValue().getAsFunction();

        Context localContext = new Context(ctx);

        String varName = null;
        boolean isDeclaration = false;
        boolean isUnpack = false;

        if (variable instanceof Identifier) {
            varName = ((Identifier) variable).getName();
        } else if (variable instanceof Let) {
            varName = ((Let) variable).getName();
            isDeclaration = true;
        } else if (variable instanceof Unpack) {
            isUnpack = true;
        } else {
            throw new EvaluateException(this,
                    "For-of loop variable must be an identifier, let declaration, or unpack pattern.");
        }

        while (true) {
            Value resultValue = nextMethod.call(new Value(iterator), Collections.emptyList(), ctx).getReferredValue();
            if (resultValue.getType() != Value.Type.SCRIPT_OBJECT) {
                throw new EvaluateException(this, "Iterator.next() must return an object.");
            }
            ScriptObject result = resultValue.getAsScriptObject();

            boolean done = result.findMember("done").getReferredValue().getAsBoolean();
            if (done)
                break;

            Value value = result.findMember("value").getReferredValue();

            if (isUnpack) {
                Unpack unpack = (Unpack) variable;
                unpack.getPattern().apply(this, localContext, value, unpack.isConst());
            } else {
                Reference element = new Reference(value);
                if (isDeclaration) {
                    localContext.declareMember(varName, element);
                } else {
                    try {
                        localContext.putMember(varName, element);
                    } catch (Exception e) {
                        throw new EvaluateException(this, "Variable " + varName + " is not defined.", e);
                    }
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
        return "ForOfLoop{" +
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
        ForOfLoop forLoop = (ForOfLoop) o;
        return Objects.equals(variable, forLoop.variable) &&
                Objects.equals(target, forLoop.target) &&
                Objects.equals(body, forLoop.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, target, body);
    }
}
