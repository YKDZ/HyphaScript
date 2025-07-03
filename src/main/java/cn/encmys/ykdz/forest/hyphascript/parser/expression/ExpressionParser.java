package cn.encmys.ykdz.forest.hyphascript.parser.expression;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import cn.encmys.ykdz.forest.hyphascript.parser.PrecedenceTable;
import org.jetbrains.annotations.NotNull;

public interface ExpressionParser {
    interface Prefix {
        @NotNull ASTNode parse(@NotNull ParseContext ctx);
    }

    interface Infix {
        @NotNull PrecedenceTable.Precedence precedence();

        @NotNull ASTNode parse(@NotNull ParseContext ctx, @NotNull ASTNode left);
    }
}