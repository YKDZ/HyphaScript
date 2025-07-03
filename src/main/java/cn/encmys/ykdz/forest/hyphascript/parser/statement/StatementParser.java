package cn.encmys.ykdz.forest.hyphascript.parser.statement;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;
import cn.encmys.ykdz.forest.hyphascript.parser.ParseContext;
import org.jetbrains.annotations.NotNull;

public interface StatementParser {
    boolean canParse(@NotNull ParseContext ctx);

    @NotNull ASTNode parse(@NotNull ParseContext ctx);
}