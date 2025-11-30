package cn.encmys.ykdz.forest.hyphascript.parser.expression.infix;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.node.MemberAccess;
import cn.encmys.ykdz.forest.hyphascript.node.TaggedTemplateString;
import cn.encmys.ykdz.forest.hyphascript.node.TemplateString;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.ExpressionParser;
import cn.encmys.ykdz.forest.hyphascript.parser.expression.prefix.TemplateStringParser;
import org.jetbrains.annotations.NotNull;

public class TaggedTemplateStringParser implements ExpressionParser.Infix {
    @Override
    public @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left) {
        TemplateString template = (TemplateString) new TemplateStringParser().parse(ctx);

        if (left instanceof MemberAccess memberAccess) {
            return new TaggedTemplateString(memberAccess.getTarget(), memberAccess.getMember(), template,
                    left.getStartToken());
        }

        return new TaggedTemplateString(left, template);
    }

    @Override
    public @NotNull PrecedenceTable.Precedence precedence() {
        return PrecedenceTable.Precedence.CALL;
    }
}
