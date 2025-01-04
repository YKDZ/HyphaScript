package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.BreakNotificationException;
import cn.encmys.ykdz.forest.hypha.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ForLoop extends ASTNode {
    @NotNull
    private final ASTNode initialization;
    @NotNull
    private final ASTNode condition;
    @NotNull
    private final ASTNode afterThought;
    @NotNull
    private final ASTNode body;

    public ForLoop(@NotNull ASTNode initialization, @NotNull ASTNode condition, @NotNull ASTNode afterThought, @NotNull ASTNode body) {
        this.initialization = initialization;
        this.condition = condition;
        this.afterThought = afterThought;
        this.body = body;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Context localContext = new Context(Context.Type.LOOP, ctx);
        initialization.evaluate(localContext);

        while (true) {
            Value conditionResult = condition.evaluate(localContext).getReferedValue();
            if (!conditionResult.isType(Value.Type.BOOLEAN)) {
                throw new ScriptException(this, "Result of for-loop condition must be boolean.");
            }
            if (!conditionResult.getAsBoolean()) {
                break;
            }

            try {
                body.evaluate(localContext);
            } catch (BreakNotificationException ignored) {
                break;
            } catch (ContinueNotificationException ignored) {}
            finally {
                afterThought.evaluate(localContext);
            }
        }

        return new Reference();
    }

    @Override
    public String toString() {
        return "ForLoop{" +
                "initialization=" + initialization +
                ", condition=" + condition +
                ", afterThought=" + afterThought +
                ", body=" + body +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForLoop forLoop = (ForLoop) o;
        return Objects.equals(initialization, forLoop.initialization) && Objects.equals(condition, forLoop.condition) && Objects.equals(afterThought, forLoop.afterThought) && Objects.equals(body, forLoop.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialization, condition, afterThought, body);
    }
}
