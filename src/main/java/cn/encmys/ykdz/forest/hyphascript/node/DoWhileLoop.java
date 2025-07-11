package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.BreakNotificationException;
import cn.encmys.ykdz.forest.hyphascript.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DoWhileLoop extends ASTNode {
    @NotNull
    private final ASTNode condition;
    @NotNull
    private final ASTNode body;

    public DoWhileLoop(@NotNull ASTNode body, @NotNull ASTNode condition, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Context localContext = new Context(ctx);
        do {
            try {
                body.evaluate(localContext);
            } catch (BreakNotificationException ignored) {
                break;
            } catch (ContinueNotificationException ignored) {
            }
            Value conditionResult = condition.evaluate(localContext).getReferredValue();
            if (!conditionResult.isType(Value.Type.BOOLEAN, Value.Type.NULL)) {
                throw new EvaluateException(this, "Result of while condition must be boolean.");
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
