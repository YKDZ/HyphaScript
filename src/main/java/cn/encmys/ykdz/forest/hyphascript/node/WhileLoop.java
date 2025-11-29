package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.BreakNotificationException;
import cn.encmys.ykdz.forest.hyphascript.exception.ContinueNotificationException;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WhileLoop extends ASTNode {
    @NotNull
    private final ASTNode condition;
    @NotNull
    private final ASTNode body;

    public WhileLoop(@NotNull ASTNode condition, @NotNull ASTNode body, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        final Context localContext = new Context(ctx);
        while (true) {
            final Value conditionResult = condition.evaluate(localContext).getReferredValue();
            if (!conditionResult.isType(Value.Type.BOOLEAN, Value.Type.NULL)) {
                throw new EvaluateException(this, "Result of while condition must be boolean.");
            }
            if (!conditionResult.getAsBoolean()) {
                break;
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

