package cn.encmys.ykdz.forest.hypha.parser;

import cn.encmys.ykdz.forest.hypha.node.ASTNode;

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
