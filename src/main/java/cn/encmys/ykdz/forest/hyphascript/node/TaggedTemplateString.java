package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TaggedTemplateString extends ASTNode {
    private final @NotNull ASTNode target;
    private final @Nullable String memberName;
    private final boolean isMemberAccess;
    private final @NotNull TemplateString template;

    public TaggedTemplateString(@NotNull ASTNode target, @NotNull TemplateString template) {
        super(target.getStartToken(), template.getEndToken());
        this.target = target;
        this.memberName = null;
        this.isMemberAccess = false;
        this.template = template;
    }

    public TaggedTemplateString(@NotNull ASTNode target, @NotNull String memberName, @NotNull TemplateString template,
                                @NotNull Token startToken) {
        super(startToken, template.getEndToken());
        this.target = target;
        this.memberName = memberName;
        this.isMemberAccess = true;
        this.template = template;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        Value targetValue = new Value(InternalObjectManager.OBJECT_PROTOTYPE);
        Value functionValue;

        if (isMemberAccess) {
            targetValue = target.evaluate(ctx).getReferredValue();
            functionValue = MemberAccess.findMemberFromTarget(targetValue, memberName, true, this).getReferredValue();
        } else {
            functionValue = target.evaluate(ctx).getReferredValue();
        }

        if (functionValue.getType() != Value.Type.FUNCTION) {
            throw new RuntimeException("Tagged template tag must be a function");
        }

        List<Reference> strings = new ArrayList<>();
        List<Value> values = new ArrayList<>();

        for (ASTNode part : template.getParts()) {
            if (part instanceof Literal && ((Literal) part).isString()) {
                strings.add(target.evaluate(ctx));
            } else {
                values.add(part.evaluate(ctx).getReferredValue());
            }
        }

        Value stringsArray = new Value(strings.toArray());

        List<Value> args = new ArrayList<>();
        args.add(stringsArray);
        args.addAll(values);
        
        return functionValue.getAsFunction().call(targetValue, args, ctx);
    }
}
