package cn.encmys.ykdz.forest.hypha.node;

import cn.encmys.ykdz.forest.hypha.context.Context;
import cn.encmys.ykdz.forest.hypha.exception.BreakNotificationException;
import cn.encmys.ykdz.forest.hypha.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hypha.exception.ScriptException;
import cn.encmys.ykdz.forest.hypha.value.Reference;
import cn.encmys.ykdz.forest.hypha.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DoWhileLoop extends ASTNode {
    @NotNull
    private final ASTNode condition;
    @NotNull
    private final ASTNode body;

    public DoWhileLoop(@NotNull ASTNode body, @NotNull ASTNode condition) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Context localContext = new Context(Context.Type.LOOP, ctx);
        do {
            try {
                body.evaluate(localContext);
            }
            catch (BreakNotificationException ignored) {
                break;
            } catch (ContinueNotificationException ignored) {
            }
            Value conditionResult = condition.evaluate(localContext).getReferedValue();
            if (!conditionResult.isType(Value.Type.BOOLEAN)) {
                throw new ScriptException(this, "Result of while condition must be boolean.");
            }
            if (!conditionResult.getAsBoolean()) {
                break;
            }
        } while (true);

        return new Reference();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoWhileLoop that = (DoWhileLoop) o;
        return Objects.equals(condition, that.condition) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, body);
    }
}
