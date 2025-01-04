package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.BreakNotificationException;
import cn.encmys.ykdz.forest.hypha.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WhileLoop extends ASTNode {
    @NotNull
    private final ASTNode condition;
    @NotNull
    private final ASTNode body;

    public WhileLoop(@NotNull ASTNode condition, @NotNull ASTNode body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Context localContext = new Context(Context.Type.LOOP, ctx);
        while (true) {
            Value conditionResult = condition.evaluate(localContext).getReferedValue();
            if (!conditionResult.isType(Value.Type.BOOLEAN)) {
                throw new ScriptException(this, "Result of while condition must be boolean.");
            }
            if (!conditionResult.getAsBoolean()) {
                break;
            }
            try {
                body.evaluate(localContext);
            }
            catch (BreakNotificationException ignored) {
                break;
            } catch (ContinueNotificationException ignored) {
            }
        }

        return new Reference();
    }

    @Override
    public String toString() {
        return "WhileLoop{" +
                "condition=" + condition +
                ", body=" + body +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhileLoop whileLoop = (WhileLoop) o;
        return Objects.equals(condition, whileLoop.condition) && Objects.equals(body, whileLoop.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, body);
    }
}

