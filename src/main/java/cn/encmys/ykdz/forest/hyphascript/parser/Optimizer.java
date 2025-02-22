package cn.encmys.ykdz.forest.hyphascript.parser;

import cn.encmys.ykdz.forest.hyphascript.node.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class Optimizer {
    private final List<ASTNode> astNodes;

    public Optimizer(List<ASTNode> astNodes) {
        this.astNodes = astNodes;
    }

    public List<ASTNode> optimize() {
        return new ArrayList<>();
    }
}
